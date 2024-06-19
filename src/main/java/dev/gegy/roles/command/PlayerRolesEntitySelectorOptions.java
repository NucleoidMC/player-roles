package dev.gegy.roles.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.fabricmc.fabric.api.command.v2.EntitySelectorOptionRegistry;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class PlayerRolesEntitySelectorOptions {
    private static final Identifier ID = PlayerRoles.identifier("role");
    private static final Text DESCRIPTION = Text.literal("Player Role");

    public static void register() {
        EntitySelectorOptionRegistry.register(ID, DESCRIPTION, PlayerRolesEntitySelectorOptions::handle, PlayerRolesEntitySelectorOptions::canUse);
    }

    private static void handle(EntitySelectorReader reader) throws CommandSyntaxException {
        boolean isNegated = reader.readNegationCharacter();
        String roleName = reader.getReader().readUnquotedString();
        reader.addPredicate(entity -> {
            var role = PlayerRolesApi.provider().get(roleName);
            return (role != null && PlayerRolesApi.lookup().byEntity(entity).has(role)) != isNegated;
        });
        if (!isNegated) {
            reader.setCustomFlag(ID, true);
        }
    }

    private static boolean canUse(EntitySelectorReader reader) {
        return !reader.getCustomFlag(ID);
    }
}
