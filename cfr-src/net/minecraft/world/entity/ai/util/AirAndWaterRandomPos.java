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

public class AirAndWaterRandomPos {
    public static @Nullable Vec3 getPos(PathfinderMob $$0, int $$1, int $$2, int $$3, double $$4, double $$5, double $$6) {
        boolean $$7 = GoalUtils.mobRestricted($$0, $$1);
        return RandomPos.generateRandomPos($$0, () -> AirAndWaterRandomPos.generateRandomPos($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7));
    }

    public static @Nullable BlockPos generateRandomPos(PathfinderMob $$0, int $$12, int $$2, int $$3, double $$4, double $$5, double $$6, boolean $$7) {
        BlockPos $$8 = RandomPos.generateRandomDirectionWithinRadians($$0.getRandom(), 0.0, $$12, $$2, $$3, $$4, $$5, $$6);
        if ($$8 == null) {
            return null;
        }
        BlockPos $$9 = RandomPos.generateRandomPosTowardDirection($$0, $$12, $$0.getRandom(), $$8);
        if (GoalUtils.isOutsideLimits($$9, $$0) || GoalUtils.isRestricted($$7, $$0, $$9)) {
            return null;
        }
        if (GoalUtils.hasMalus($$0, $$9 = RandomPos.moveUpOutOfSolid($$9, $$0.level().getMaxY(), $$1 -> GoalUtils.isSolid($$0, $$1)))) {
            return null;
        }
        return $$9;
    }
}

