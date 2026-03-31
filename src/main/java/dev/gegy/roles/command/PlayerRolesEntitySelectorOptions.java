package dev.gegy.roles.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.fabricmc.fabric.api.command.v2.EntitySelectorOptionRegistry;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class PlayerRolesEntitySelectorOptions {
    private static final Identifier ID = PlayerRoles.identifier("role");
    private static final Component DESCRIPTION = Component.literal("Player Role");

    public static void register() {
        EntitySelectorOptionRegistry.register(ID, DESCRIPTION, PlayerRolesEntitySelectorOptions::handle, PlayerRolesEntitySelectorOptions::canUse);
    }

    private static void handle(EntitySelectorParser reader) throws CommandSyntaxException {
        boolean isNegated = reader.shouldInvertValue();
        String roleName = reader.getReader().readUnquotedString();
        reader.addPredicate(entity -> {
            var role = PlayerRolesApi.provider().get(roleName);
            return (role != null && PlayerRolesApi.lookup().byEntity(entity).has(role)) != isNegated;
        });
        if (!isNegated) {
            reader.setCustomFlag(ID, true);
        }
    }

    private static boolean canUse(EntitySelectorParser reader) {
        return !reader.getCustomFlag(ID);
    }
}
