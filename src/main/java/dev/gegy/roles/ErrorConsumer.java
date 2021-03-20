package dev.gegy.roles;

import com.mojang.serialization.DataResult;

public interface ErrorConsumer {
    void report(String message);

    default void report(String message, DataResult.PartialResult<?> error) {
        this.report(message + ": " + error.message());
    }

    default void report(String message, Throwable throwable) {
        String throwableMessage = throwable.getMessage();
        if (throwableMessage != null) {
            this.report(message + ": " + throwableMessage);
        } else {
            this.report(message);
        }
    }
}
