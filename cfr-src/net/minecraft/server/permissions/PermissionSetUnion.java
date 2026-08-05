/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  it.unimi.dsi.fastutil.objects.ReferenceSet
 */
package net.minecraft.server.permissions;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;

public class PermissionSetUnion
implements PermissionSet {
    private final ReferenceSet<PermissionSet> permissions = new ReferenceArraySet();

    PermissionSetUnion(PermissionSet $$0, PermissionSet $$1) {
        this.permissions.add((Object)$$0);
        this.permissions.add((Object)$$1);
        this.ensureNoUnionsWithinUnions();
    }

    private PermissionSetUnion(ReferenceSet<PermissionSet> $$0, PermissionSet $$1) {
        this.permissions.addAll($$0);
        this.permissions.add((Object)$$1);
        this.ensureNoUnionsWithinUnions();
    }

    private PermissionSetUnion(ReferenceSet<PermissionSet> $$0, ReferenceSet<PermissionSet> $$1) {
        this.permissions.addAll($$0);
        this.permissions.addAll($$1);
        this.ensureNoUnionsWithinUnions();
    }

    @Override
    public boolean hasPermission(Permission $$0) {
        for (PermissionSet $$1 : this.permissions) {
            if (!$$1.hasPermission($$0)) continue;
            return true;
        }
        return false;
    }

    @Override
    public PermissionSet union(PermissionSet $$0) {
        if ($$0 instanceof PermissionSetUnion) {
            PermissionSetUnion $$1 = (PermissionSetUnion)$$0;
            return new PermissionSetUnion(this.permissions, $$1.permissions);
        }
        return new PermissionSetUnion(this.permissions, $$0);
    }

    @VisibleForTesting
    public ReferenceSet<PermissionSet> getPermissions() {
        return new ReferenceArraySet(this.permissions);
    }

    private void ensureNoUnionsWithinUnions() {
        for (PermissionSet $$0 : this.permissions) {
            if (!($$0 instanceof PermissionSetUnion)) continue;
            throw new IllegalArgumentException("Cannot have PermissionSetUnion within another PermissionSetUnion");
        }
    }
}

