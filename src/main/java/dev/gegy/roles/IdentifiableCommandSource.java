package dev.gegy.roles;

public interface IdentifiableCommandSource {
    void player_roles$setIdentityType(Type type);

    Type player_roles$getIdentityType();

    enum Type {
        UNKNOWN,
        COMMAND_BLOCK,
        FUNCTION
    }
}
