package dev.gegy.roles.config;

import com.mojang.serialization.DataResult;

public interface ConfigErrorConsumer {
    void report(String message);

    default void report(String message, DataResult.Error<?> error) {
        this.report(message + ": " + error.message());
    }

    default void report(String message, Throwable throwable) {
        var throwableMessage = throwable.getMessage();
        if (throwableMessage != null) {
            this.report(message + ": " + throwableMessage);
        } else {
            this.report(message);
        }
    }
}
