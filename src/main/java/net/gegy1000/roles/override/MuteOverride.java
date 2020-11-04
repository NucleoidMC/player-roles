package net.gegy1000.roles.override;

public final class MuteOverride implements RoleOverride {
    private final boolean muted;

    public MuteOverride(boolean muted) {
        this.muted = muted;
    }

    public boolean isMuted() {
        return this.muted;
    }
}
