package dev.gegy.roles;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.api.RoleLookup;
import dev.gegy.roles.api.RoleOwner;
import dev.gegy.roles.api.RoleReader;
import dev.gegy.roles.api.override.RoleOverrideType;
import dev.gegy.roles.command.PlayerRolesEntitySelectorOptions;
import dev.gegy.roles.command.RoleCommand;
import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.override.ChatTypeOverride;
import dev.gegy.roles.override.NameDecorationOverride;
import dev.gegy.roles.override.command.CommandOverride;
import dev.gegy.roles.override.permission.PermissionKeyOverride;
import dev.gegy.roles.store.PlayerRoleManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.UUID;

public final class PlayerRoles implements ModInitializer {
    public static final String ID = "player_roles";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String EVERYONE = "everyone";

    public static final RoleOverrideType<CommandOverride> COMMANDS = registerOverride("commands", CommandOverride.CODEC)
            .withChangeListener(player -> {
                var server = player.level().getServer();
                server.getCommands().sendCommands(player);
            });

    public static final RoleOverrideType<ChatTypeOverride> CHAT_TYPE = registerOverride("chat_type", ChatTypeOverride.CODEC);
    public static final RoleOverrideType<NameDecorationOverride> NAME_DECORATION = registerOverride("name_decoration", NameDecorationOverride.CODEC)
            .withChangeListener(player -> {
                var packet = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player);
                player.level().getServer().getPlayerList().broadcastAll(packet);
            });
    public static final RoleOverrideType<Boolean> COMMAND_FEEDBACK = registerOverride("command_feedback", Codec.BOOL);
    public static final RoleOverrideType<Boolean> MUTE = registerOverride("mute", Codec.BOOL);
    public static final RoleOverrideType<PermissionLevel> PERMISSION_LEVEL = registerOverride("permission_level", PermissionLevel.INT_CODEC);
    public static final RoleOverrideType<Boolean> ENTITY_SELECTORS = registerOverride("entity_selectors", Codec.BOOL);
    public static final RoleOverrideType<Boolean> BYPASS_PLAYER_LIMIT = registerOverride("bypass_player_limit", Codec.BOOL);

    private static <T> RoleOverrideType<T> registerOverride(String id, Codec<T> codec) {
        return RoleOverrideType.register(PlayerRoles.identifier(id), codec);
    }

    static {
        PlayerRolesApi.setRoleLookup(new RoleLookup() {
            @Override
            @NotNull
            public RoleReader byEntity(Entity entity) {
                var onlineRoles = PlayerRoleManager.get().getOnlinePlayerRoles(entity);
                if (onlineRoles != null) {
                    return onlineRoles;
                }

                if (entity instanceof RoleOwner roleOwner) {
                    return roleOwner.getRoles();
                }

                return RoleReader.EMPTY;
            }

            @Override
            @NotNull
            public RoleReader bySource(CommandSourceStack source) {
                var entity = source.getEntity();
                if (entity != null) {
                    return this.byEntity(entity);
                }

                if (source instanceof RoleOwner roleOwner) {
                    return roleOwner.getRoles();
                }

                if (source instanceof IdentifiableCommandSource identifiable) {
                    return switch (identifiable.player_roles$getIdentityType()) {
                        case COMMAND_BLOCK -> PlayerRolesConfig.get().getCommandBlockRoles();
                        case FUNCTION -> PlayerRolesConfig.get().getFunctionRoles();
                        default -> RoleReader.EMPTY;
                    };
                }

                return RoleReader.EMPTY;
            }
        });
    }

    @Override
    public void onInitialize() {
        registerModIntegrations();

        var errors = PlayerRolesConfig.setup();
        if (!errors.isEmpty()) {
            LOGGER.warn("Failed to load player-roles config! ({} errors)", errors.size());
            for (var error : errors) {
                LOGGER.warn(" - {}", error);
            }
        }

        PlayerRoleManager.setup();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RoleCommand.register(dispatcher);
        });

        PlayerRolesEntitySelectorOptions.register();

        CommandOverride.initialize();

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> trySendChat(sender));
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> trySendChat(source));
    }

    private static void registerModIntegrations() {
        if (FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) {
            registerPermissionKeyOverride();
        }
    }

    private static void registerPermissionKeyOverride() {
        PermissionKeyOverride.register();
    }

    public static boolean trySendChat(CommandSourceStack source) {
        final ServerPlayer player = source.getPlayer();
        return player == null || trySendChat(player);
    }

    public static boolean trySendChat(ServerPlayer player) {
        var roles = PlayerRolesApi.lookup().byPlayer(player);
        if (roles.overrides().test(PlayerRoles.MUTE)) {
            player.displayClientMessage(Component.literal("You are muted!").withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
    }

    public static boolean canBypassPlayerLimit(MinecraftServer server, UUID playerUuid) {
        return PlayerRoleManager.get().peekRoles(server, playerUuid).overrides().test(PlayerRoles.BYPASS_PLAYER_LIMIT);
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}
