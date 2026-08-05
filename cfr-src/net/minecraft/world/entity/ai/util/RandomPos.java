/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RandomPos {
    private static final int RANDOM_POS_ATTEMPTS = 10;

    public static BlockPos generateRandomDirection(RandomSource $$0, int $$1, int $$2) {
        int $$3 = $$0.nextInt(2 * $$1 + 1) - $$1;
        int $$4 = $$0.nextInt(2 * $$2 + 1) - $$2;
        int $$5 = $$0.nextInt(2 * $$1 + 1) - $$1;
        return new BlockPos($$3, $$4, $$5);
    }

    public static @Nullable BlockPos generateRandomDirectionWithinRadians(RandomSource $$0, double $$1, double $$2, int $$3, int $$4, double $$5, double $$6, double $$7) {
        double $$8 = Mth.atan2($$6, $$5) - 1.5707963705062866;
        double $$9 = $$8 + (double)(2.0f * $$0.nextFloat() - 1.0f) * $$7;
        double $$10 = Mth.lerp(Math.sqrt($$0.nextDouble()), $$1, $$2) * (double)Mth.SQRT_OF_TWO;
        double $$11 = -$$10 * Math.sin($$9);
        double $$12 = $$10 * Math.cos($$9);
        if (Math.abs($$11) > $$2 || Math.abs($$12) > $$2) {
            return null;
        }
        int $$13 = $$0.nextInt(2 * $$3 + 1) - $$3 + $$4;
        return BlockPos.containing($$11, $$13, $$12);
    }

    @VisibleForTesting
    public static BlockPos moveUpOutOfSolid(BlockPos $$0, int $$1, Predicate<BlockPos> $$2) {
        if ($$2.test($$0)) {
            BlockPos.MutableBlockPos $$3 = $$0.mutable().move(Direction.UP);
            while ($$3.getY() <= $$1 && $$2.test($$3)) {
                $$3.move(Direction.UP);
            }
            return $$3.immutable();
        }
        return $$0;
    }

    @VisibleForTesting
    public static BlockPos moveUpToAboveSolid(BlockPos $$0, int $$1, int $$2, Predicate<BlockPos> $$3) {
        if ($$1 < 0) {
            throw new IllegalArgumentException("aboveSolidAmount was " + $$1 + ", expected >= 0");
        }
        if ($$3.test($$0)) {
            BlockPos.MutableBlockPos $$4 = $$0.mutable().move(Direction.UP);
            while ($$4.getY() <= $$2 && $$3.test($$4)) {
                $$4.move(Direction.UP);
            }
            int $$5 = $$4.getY();
            while ($$4.getY() <= $$2 && $$4.getY() - $$5 < $$1) {
                $$4.move(Direction.UP);
                if (!$$3.test($$4)) continue;
                $$4.move(Direction.DOWN);
                break;
            }
            return $$4.immutable();
        }
        return $$0;
    }

    public static @Nullable Vec3 generateRandomPos(PathfinderMob $$0, Supplier<@Nullable BlockPos> $$1) {
        return RandomPos.generateRandomPos($$1, $$0::getWalkTargetValue);
    }

    public static @Nullable Vec3 generateRandomPos(Supplier<@Nullable BlockPos> $$0, ToDoubleFunction<BlockPos> $$1) {
        double $$2 = Double.NEGATIVE_INFINITY;
        BlockPos $$3 = null;
        for (int $$4 = 0; $$4 < 10; ++$$4) {
            double $$6;
            BlockPos $$5 = $$0.get();
            if ($$5 == null || !(($$6 = $$1.applyAsDouble($$5)) > $$2)) continue;
            $$2 = $$6;
            $$3 = $$5;
        }
        return $$3 != null ? Vec3.atBottomCenterOf($$3) : null;
    }

    public static BlockPos generateRandomPosTowardDirection(PathfinderMob $$0, double $$1, RandomSource $$2, BlockPos $$3) {
        double $$4 = $$3.getX();
        double $$5 = $$3.getZ();
        if ($$0.hasHome() && $$1 > 1.0) {
            BlockPos $$6 = $$0.getHomePosition();
            $$4 = $$0.getX() > (double)$$6.getX() ? ($$4 -= $$2.nextDouble() * $$1 / 2.0) : ($$4 += $$2.nextDouble() * $$1 / 2.0);
            $$5 = $$0.getZ() > (double)$$6.getZ() ? ($$5 -= $$2.nextDouble() * $$1 / 2.0) : ($$5 += $$2.nextDouble() * $$1 / 2.0);
        }
        return BlockPos.containing($$4 + $$0.getX(), (double)$$3.getY() + $$0.getY(), $$5 + $$0.getZ());
    }
}

