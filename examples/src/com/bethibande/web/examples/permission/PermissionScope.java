package com.bethibande.web.examples.permission;

import java.util.HashMap;

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

    public boolean canRead(int permission) {
        if(!permissions.containsKey(permission)) return false;
        byte access = permissions.get(permission);
        return (access & ACCESS_READ) == ACCESS_READ;
    }

    public boolean canWrite(int permission) {
        if(!permissions.containsKey(permission)) return false;
        byte access = permissions.get(permission);
        return (access & ACCESS_WRITE) == ACCESS_WRITE;
    }

    public boolean canRead(Permissions permission) {
        if(!permissions.containsKey(permission.getId())) return false;
        byte access = permissions.get(permission.getId());
        return (access & ACCESS_READ) == ACCESS_READ;
    }

    public boolean canWrite(Permissions permission) {
        if(!permissions.containsKey(permission.getId())) return false;
        byte access = permissions.get(permission.getId());
        return (access & ACCESS_WRITE) == ACCESS_WRITE;
    }

}
