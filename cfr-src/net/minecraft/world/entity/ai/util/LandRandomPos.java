/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.util;

import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LandRandomPos {
    public static @Nullable Vec3 getPos(PathfinderMob $$0, int $$1, int $$2) {
        return LandRandomPos.getPos($$0, $$1, $$2, $$0::getWalkTargetValue);
    }

    public static @Nullable Vec3 getPos(PathfinderMob $$0, int $$1, int $$2, ToDoubleFunction<BlockPos> $$3) {
        boolean $$4 = GoalUtils.mobRestricted($$0, $$1);
        return RandomPos.generateRandomPos(() -> {
            BlockPos $$4 = RandomPos.generateRandomDirection($$0.getRandom(), $$1, $$2);
            BlockPos $$5 = LandRandomPos.generateRandomPosTowardDirection($$0, $$1, $$4, $$4);
            if ($$5 == null) {
                return null;
            }
            return LandRandomPos.movePosUpOutOfSolid($$0, $$5);
        }, $$3);
    }

    public static @Nullable Vec3 getPosTowards(PathfinderMob $$0, int $$1, int $$2, Vec3 $$3) {
        Vec3 $$4 = $$3.subtract($$0.getX(), $$0.getY(), $$0.getZ());
        boolean $$5 = GoalUtils.mobRestricted($$0, $$1);
        return LandRandomPos.getPosInDirection($$0, 0.0, $$1, $$2, $$4, $$5);
    }

    public static @Nullable Vec3 getPosAway(PathfinderMob $$0, int $$1, int $$2, Vec3 $$3) {
        return LandRandomPos.getPosAway($$0, 0.0, $$1, $$2, $$3);
    }

    public static @Nullable Vec3 getPosAway(PathfinderMob $$0, double $$1, double $$2, int $$3, Vec3 $$4) {
        Vec3 $$5 = $$0.position().subtract($$4);
        if ($$5.length() == 0.0) {
            $$5 = new Vec3($$0.getRandom().nextDouble() - 0.5, 0.0, $$0.getRandom().nextDouble() - 0.5);
        }
        boolean $$6 = GoalUtils.mobRestricted($$0, $$2);
        return LandRandomPos.getPosInDirection($$0, $$1, $$2, $$3, $$5, $$6);
    }

    private static @Nullable Vec3 getPosInDirection(PathfinderMob $$0, double $$1, double $$2, int $$3, Vec3 $$4, boolean $$5) {
        return RandomPos.generateRandomPos($$0, () -> {
            BlockPos $$6 = RandomPos.generateRandomDirectionWithinRadians($$0.getRandom(), $$1, $$2, $$3, 0, $$4.x, $$4.z, 1.5707963705062866);
            if ($$6 == null) {
                return null;
            }
            BlockPos $$7 = LandRandomPos.generateRandomPosTowardDirection($$0, $$2, $$5, $$6);
            if ($$7 == null) {
                return null;
            }
            return LandRandomPos.movePosUpOutOfSolid($$0, $$7);
        });
    }

    public static @Nullable BlockPos movePosUpOutOfSolid(PathfinderMob $$0, BlockPos $$12) {
        if (GoalUtils.isWater($$0, $$12 = RandomPos.moveUpOutOfSolid($$12, $$0.level().getMaxY(), $$1 -> GoalUtils.isSolid($$0, $$1))) || GoalUtils.hasMalus($$0, $$12)) {
            return null;
        }
        return $$12;
    }

    public static @Nullable BlockPos generateRandomPosTowardDirection(PathfinderMob $$0, double $$1, boolean $$2, BlockPos $$3) {
        BlockPos $$4 = RandomPos.generateRandomPosTowardDirection($$0, $$1, $$0.getRandom(), $$3);
        if (GoalUtils.isOutsideLimits($$4, $$0) || GoalUtils.isRestricted($$2, $$0, $$4) || GoalUtils.isNotStable($$0.getNavigation(), $$4)) {
            return null;
        }
        return $$4;
    }
}

