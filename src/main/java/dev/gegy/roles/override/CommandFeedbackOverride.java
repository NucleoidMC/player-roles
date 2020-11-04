package dev.gegy.roles.override;

public final class CommandFeedbackOverride implements RoleOverride {
    private final boolean feedback;

    public CommandFeedbackOverride(boolean feedback) {
        this.feedback = feedback;
    }

    public boolean shouldReceiveFeedback() {
        return this.feedback;
    }
}
