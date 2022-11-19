package com.bethibande.web.examples.permission;

import java.util.HashMap;

@SuppressWarnings("unused")
public class PermissionScope {

    public static final byte ACCESS_READ = 1;
    public static final byte ACCESS_WRITE = 2;

    private final HashMap<Integer, Byte> permissions = new HashMap<>();

    public void grantPermission(int permission, byte access) {
        permissions.remove(permission);
        permissions.put(permission, access);
    }

    public PermissionScope withPermission(int permission, byte access) {
        grantPermission(permission, access);
        return this;
    }

    public void grantPermission(Permissions permission, byte access) {
        permissions.remove(permission.getId());
        permissions.put(permission.getId(), access);
    }

    public PermissionScope withPermission(Permissions permission, byte access) {
        grantPermission(permission, access);
        return this;
    }

    public boolean can(int permission, byte access) {
        if(!permissions.containsKey(permission)) return false;
        byte _access = permissions.get(permission);
        return (_access & access) == access;
    }

    public boolean canRead(int permission) {
        return can(permission, ACCESS_READ);
    }

    public boolean canWrite(int permission) {
        return can(permission, ACCESS_WRITE);
    }

    public boolean canRead(Permissions permission) {
        return can(permission.getId(), ACCESS_READ);
    }

    public boolean canWrite(Permissions permission) {
        return can(permission.getId(), ACCESS_WRITE);
    }

}
