/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.permissions;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSetUnion;

public interface PermissionSet {
    public static final PermissionSet NO_PERMISSIONS = $$0 -> false;
    public static final PermissionSet ALL_PERMISSIONS = $$0 -> true;

    public boolean hasPermission(Permission var1);

    default public PermissionSet union(PermissionSet $$0) {
        if ($$0 instanceof PermissionSetUnion) {
            return $$0.union(this);
        }
        return new PermissionSetUnion(this, $$0);
    }
}

