package dev.gegy.roles.api;

public final class PlayerRolesApi {
    public static final String ID = "player_roles";

    private static RoleProvider provider = id -> null;
    private static RoleLookup lookup = RoleLookup.EMPTY;

    public static void setRoleProvider(RoleProvider provider) {
        PlayerRolesApi.provider = provider;
    }

    public static void setRoleLookup(RoleLookup lookup) {
        PlayerRolesApi.lookup = lookup;
    }

    public static RoleLookup lookup() {
        return PlayerRolesApi.lookup;
    }

    public static RoleProvider provider() {
        return PlayerRolesApi.provider;
    }
}
