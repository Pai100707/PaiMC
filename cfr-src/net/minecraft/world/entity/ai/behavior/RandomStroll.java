/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RandomStroll {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;
    private static final int[][] SWIM_XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

    public static OneShot<PathfinderMob> stroll(float $$0) {
        return RandomStroll.stroll($$0, true);
    }

    public static OneShot<PathfinderMob> stroll(float $$02, boolean $$1) {
        return RandomStroll.strollFlyOrSwim($$02, $$0 -> LandRandomPos.getPos($$0, 10, 7), $$1 ? $$0 -> true : $$0 -> !$$0.isInWater());
    }

    public static BehaviorControl<PathfinderMob> stroll(float $$02, int $$1, int $$22) {
        return RandomStroll.strollFlyOrSwim($$02, $$2 -> LandRandomPos.getPos($$2, $$1, $$22), $$0 -> true);
    }

    public static BehaviorControl<PathfinderMob> fly(float $$02) {
        return RandomStroll.strollFlyOrSwim($$02, $$0 -> RandomStroll.getTargetFlyPos($$0, 10, 7), $$0 -> true);
    }

    public static BehaviorControl<PathfinderMob> swim(float $$0) {
        return RandomStroll.strollFlyOrSwim($$0, RandomStroll::getTargetSwimPos, Entity::isInWater);
    }

    private static OneShot<PathfinderMob> strollFlyOrSwim(float $$0, Function<PathfinderMob, Vec3> $$1, Predicate<PathfinderMob> $$2) {
        return BehaviorBuilder.create($$32 -> $$32.group($$32.absent(MemoryModuleType.WALK_TARGET)).apply((Applicative)$$32, $$3 -> ($$4, $$5, $$6) -> {
            if (!$$2.test((PathfinderMob)$$5)) {
                return false;
            }
            Optional<Vec3> $$7 = Optional.ofNullable((Vec3)$$1.apply((PathfinderMob)$$5));
            $$3.setOrErase($$7.map($$1 -> new WalkTarget((Vec3)$$1, $$0, 0)));
            return true;
        }));
    }

    private static @Nullable Vec3 getTargetSwimPos(PathfinderMob $$0) {
        Vec3 $$1 = null;
        Vec3 $$2 = null;
        for (int[] $$3 : SWIM_XY_DISTANCE_TIERS) {
            $$2 = $$1 == null ? BehaviorUtils.getRandomSwimmablePos($$0, $$3[0], $$3[1]) : $$0.position().add($$0.position().vectorTo($$1).normalize().multiply($$3[0], $$3[1], $$3[0]));
            boolean $$4 = GoalUtils.mobRestricted($$0, $$3[0]);
            if ($$2 == null || $$0.level().getFluidState(BlockPos.containing($$2)).isEmpty() || GoalUtils.isRestricted($$4, $$0, $$2)) {
                return $$1;
            }
            $$1 = $$2;
        }
        return $$2;
    }

    private static @Nullable Vec3 getTargetFlyPos(PathfinderMob $$0, int $$1, int $$2) {
        Vec3 $$3 = $$0.getViewVector(0.0f);
        return AirAndWaterRandomPos.getPos($$0, $$1, $$2, -2, $$3.x, $$3.z, 1.5707963705062866);
    }
}

