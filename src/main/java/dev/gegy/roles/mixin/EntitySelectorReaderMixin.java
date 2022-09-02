package dev.gegy.roles.mixin;

import dev.gegy.roles.command.ExtendedEntitySelectorReader;
import net.minecraft.command.EntitySelectorReader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntitySelectorReader.class)
public class EntitySelectorReaderMixin implements ExtendedEntitySelectorReader {
    private boolean selectsRole = false;

    @Override
    public boolean selectsRole() {
        return this.selectsRole;
    }

    @Override
    public void setSelectsRole(boolean value) {
        this.selectsRole = value;
    }
}
