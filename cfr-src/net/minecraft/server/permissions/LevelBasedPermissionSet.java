/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.permissions;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

public interface LevelBasedPermissionSet
extends PermissionSet {
    @Deprecated
    public static final LevelBasedPermissionSet ALL = LevelBasedPermissionSet.create(PermissionLevel.ALL);
    public static final LevelBasedPermissionSet MODERATOR = LevelBasedPermissionSet.create(PermissionLevel.MODERATORS);
    public static final LevelBasedPermissionSet GAMEMASTER = LevelBasedPermissionSet.create(PermissionLevel.GAMEMASTERS);
    public static final LevelBasedPermissionSet ADMIN = LevelBasedPermissionSet.create(PermissionLevel.ADMINS);
    public static final LevelBasedPermissionSet OWNER = LevelBasedPermissionSet.create(PermissionLevel.OWNERS);

    public PermissionLevel level();

    @Override
    default public boolean hasPermission(Permission $$0) {
        if ($$0 instanceof Permission.HasCommandLevel) {
            Permission.HasCommandLevel $$1 = (Permission.HasCommandLevel)$$0;
            return this.level().isEqualOrHigherThan($$1.level());
        }
        if ($$0.equals(Permissions.COMMANDS_ENTITY_SELECTORS)) {
            return this.level().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS);
        }
        return false;
    }

    @Override
    default public PermissionSet union(PermissionSet $$0) {
        if ($$0 instanceof LevelBasedPermissionSet) {
            LevelBasedPermissionSet $$1 = (LevelBasedPermissionSet)$$0;
            if (this.level().isEqualOrHigherThan($$1.level())) {
                return $$1;
            }
            return this;
        }
        return PermissionSet.super.union($$0);
    }

    public static LevelBasedPermissionSet forLevel(PermissionLevel $$0) {
        return switch ($$0) {
            default -> throw new MatchException(null, null);
            case PermissionLevel.ALL -> ALL;
            case PermissionLevel.MODERATORS -> MODERATOR;
            case PermissionLevel.GAMEMASTERS -> GAMEMASTER;
            case PermissionLevel.ADMINS -> ADMIN;
            case PermissionLevel.OWNERS -> OWNER;
        };
    }

    private static LevelBasedPermissionSet create(final PermissionLevel $$0) {
        return new LevelBasedPermissionSet(){

            @Override
            public PermissionLevel level() {
                return $$0;
            }

            public String toString() {
                return "permission level: " + $$0.name();
            }
        };
    }
}

