package dev.gegy.roles.override.command;

public final class CommandTestContext {
    private static final ThreadLocal<Boolean> SUGGESTING = new ThreadLocal<>();

    public static void startSuggesting() {
        SUGGESTING.set(Boolean.TRUE);
    }

    public static void stopSuggesting() {
        SUGGESTING.remove();
    }

    public static boolean isSuggesting() {
        return SUGGESTING.get() == Boolean.TRUE;
    }
}
