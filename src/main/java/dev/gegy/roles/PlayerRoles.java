package dev.gegy.roles;

import com.mojang.serialization.Codec;
import dev.gegy.roles.api.*;
import dev.gegy.roles.api.override.RoleOverrideType;
import dev.gegy.roles.command.RoleCommand;
import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.override.ChatFormatOverride;
import dev.gegy.roles.override.NameStyleOverride;
import dev.gegy.roles.override.command.CommandOverride;
import dev.gegy.roles.override.permission.PermissionKeyOverride;
import dev.gegy.roles.store.PlayerRoleManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class PlayerRoles implements ModInitializer {
    public static final String ID = "player_roles";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final String EVERYONE = "everyone";

    public static final RoleOverrideType<CommandOverride> COMMANDS = registerOverride("commands", CommandOverride.CODEC)
            .withChangeListener(player -> {
                var server = player.getServer();
                if (server != null) {
                    server.getCommandManager().sendCommandTree(player);
                }
            });

    public static final RoleOverrideType<ChatFormatOverride> CHAT_FORMAT = registerOverride("chat_format", ChatFormatOverride.CODEC);
    public static final RoleOverrideType<NameStyleOverride> NAME_FORMAT = registerOverride("name_style", NameStyleOverride.CODEC);
    public static final RoleOverrideType<Boolean> COMMAND_FEEDBACK = registerOverride("command_feedback", Codec.BOOL);
    public static final RoleOverrideType<Boolean> MUTE = registerOverride("mute", Codec.BOOL);
    public static final RoleOverrideType<Integer> PERMISSION_LEVEL = registerOverride("permission_level", Codec.intRange(0, 4));
    public static final RoleOverrideType<Boolean> ENTITY_SELECTORS = registerOverride("entity_selectors", Codec.BOOL);

    private static <T> RoleOverrideType<T> registerOverride(String id, Codec<T> codec) {
        return RoleOverrideType.register(PlayerRoles.identifier(id), codec);
    }

    static {
        PlayerRolesApi.setRoleLookup(new RoleLookup() {
            @Override
            @NotNull
            public RoleReader byEntity(Entity entity) {
                if (entity instanceof PlayerWithRoles player) {
                    return player.getPlayerRoleSet();
                }

                if (entity instanceof RoleOwner roleOwner) {
                    return roleOwner.getRoles();
                }

                return RoleReader.EMPTY;
            }

            @Override
            @NotNull
            public RoleReader bySource(ServerCommandSource source) {
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

        CommandOverride.initialize();
    }

    private static void registerModIntegrations() {
        if (FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) {
            registerPermissionKeyOverride();
        }
    }

    private static void registerPermissionKeyOverride() {
        PermissionKeyOverride.register();
    }

    public static void sendMuteFeedback(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("You are muted!").formatted(Formatting.RED), true);
    }

    public static Identifier identifier(String path) {
        return new Identifier(ID, path);
    }
}
