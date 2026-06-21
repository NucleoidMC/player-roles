package dev.gegy.roles.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTeam.class)
public interface TeamAccessor {

}
