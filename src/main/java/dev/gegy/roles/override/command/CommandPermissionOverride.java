package dev.gegy.roles.override.command;

import com.mojang.serialization.Codec;
import dev.gegy.roles.api.PermissionResult;
import dev.gegy.roles.api.RoleOwner;
import dev.gegy.roles.override.RoleChangeListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class CommandPermissionOverride implements RoleChangeListener {
    public static final Codec<CommandPermissionOverride> CODEC = CommandPermissionRules.CODEC.xmap(
            CommandPermissionOverride::new,
            override -> override.rules
    );

    private final CommandPermissionRules rules;

    public CommandPermissionOverride(CommandPermissionRules rules) {
        this.rules = rules;
    }

    public PermissionResult test(MatchableCommand command) {
        return this.rules.test(command);
    }

    @Override
    public void notifyChange(@Nullable RoleOwner owner) {
        if (owner instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) owner;
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.getCommandManager().sendCommandTree(player);
            }
        }
    }

    @Override
    public String toString() {
        return "CommandPermissionOverride[" + this.rules.toString() + "]";
    }
}
