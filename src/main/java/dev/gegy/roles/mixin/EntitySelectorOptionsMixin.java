package dev.gegy.roles.mixin;

import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.command.ExtendedEntitySelectorReader;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public abstract class EntitySelectorOptionsMixin {
    @Shadow private static void putOption(String id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
    }

    @Inject(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/EntitySelectorOptions;putOption(Ljava/lang/String;Lnet/minecraft/command/EntitySelectorOptions$SelectorHandler;Ljava/util/function/Predicate;Lnet/minecraft/text/Text;)V", ordinal = 0))
    private static void registerRoleSelector(CallbackInfo ci) {
        putOption("role",
                reader -> {
                    boolean isNegated = reader.readNegationCharacter();
                    String roleName = reader.getReader().readUnquotedString();
                    reader.setPredicate(entity -> {
                        var role = PlayerRolesApi.provider().get(roleName);
                        return (role != null && PlayerRolesApi.lookup().byEntity(entity).has(role)) != isNegated;
                    });
                    if (!isNegated) {
                        ((ExtendedEntitySelectorReader)reader).setSelectsRole(true);
                    }
                },
                reader -> !((ExtendedEntitySelectorReader)reader).selectsRole(),
                Text.literal("Player Role")
        );
    }
}
