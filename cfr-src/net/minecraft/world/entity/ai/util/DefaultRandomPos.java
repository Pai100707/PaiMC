/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DefaultRandomPos {
    public static @Nullable Vec3 getPos(PathfinderMob $$0, int $$1, int $$2) {
        boolean $$3 = GoalUtils.mobRestricted($$0, $$1);
        return RandomPos.generateRandomPos($$0, () -> {
            BlockPos $$4 = RandomPos.generateRandomDirection($$0.getRandom(), $$1, $$2);
            return DefaultRandomPos.generateRandomPosTowardDirection($$0, $$1, $$3, $$4);
        });
    }

    public static @Nullable Vec3 getPosTowards(PathfinderMob $$0, int $$1, int $$2, Vec3 $$3, double $$4) {
        Vec3 $$5 = $$3.subtract($$0.getX(), $$0.getY(), $$0.getZ());
        boolean $$6 = GoalUtils.mobRestricted($$0, $$1);
        return RandomPos.generateRandomPos($$0, () -> {
            BlockPos $$6 = RandomPos.generateRandomDirectionWithinRadians($$0.getRandom(), 0.0, $$1, $$2, 0, $$3.x, $$3.z, $$4);
            if ($$6 == null) {
                return null;
            }
            return DefaultRandomPos.generateRandomPosTowardDirection($$0, $$1, $$6, $$6);
        });
    }

    public static @Nullable Vec3 getPosAway(PathfinderMob $$0, int $$1, int $$2, Vec3 $$3) {
        Vec3 $$4 = $$0.position().subtract($$3);
        boolean $$5 = GoalUtils.mobRestricted($$0, $$1);
        return RandomPos.generateRandomPos($$0, () -> {
            BlockPos $$5 = RandomPos.generateRandomDirectionWithinRadians($$0.getRandom(), 0.0, $$1, $$2, 0, $$3.x, $$3.z, 1.5707963705062866);
            if ($$5 == null) {
                return null;
            }
            return DefaultRandomPos.generateRandomPosTowardDirection($$0, $$1, $$5, $$5);
        });
    }

    private static @Nullable BlockPos generateRandomPosTowardDirection(PathfinderMob $$0, int $$1, boolean $$2, BlockPos $$3) {
        BlockPos $$4 = RandomPos.generateRandomPosTowardDirection($$0, $$1, $$0.getRandom(), $$3);
        if (GoalUtils.isOutsideLimits($$4, $$0) || GoalUtils.isRestricted($$2, $$0, $$4) || GoalUtils.isNotStable($$0.getNavigation(), $$4) || GoalUtils.hasMalus($$0, $$4)) {
            return null;
        }
        return $$4;
    }
}

