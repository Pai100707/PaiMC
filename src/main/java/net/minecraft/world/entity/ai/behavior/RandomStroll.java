package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStroll {
   private static final int MAX_XZ_DIST = 10;
   private static final int MAX_Y_DIST = 7;
   private static final int[][] SWIM_XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

   public static OneShot<net.minecraft.world.entity.PathfinderMob> stroll(float $$0) {
      return stroll($$0, true);
   }

   public static OneShot<net.minecraft.world.entity.PathfinderMob> stroll(float $$0, boolean $$1) {
      return strollFlyOrSwim($$0, $$0x -> LandRandomPos.getPos($$0x, 10, 7), $$1 ? $$0x -> true : $$0x -> !$$0x.isInWater());
   }

   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> stroll(float $$0, int $$1, int $$2) {
      return strollFlyOrSwim($$0, $$2x -> LandRandomPos.getPos($$2x, $$1, $$2), $$0x -> true);
   }

   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> fly(float $$0) {
      return strollFlyOrSwim($$0, $$0x -> getTargetFlyPos($$0x, 10, 7), $$0x -> true);
   }

   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> swim(float $$0) {
      return strollFlyOrSwim($$0, RandomStroll::getTargetSwimPos, net.minecraft.world.entity.Entity::isInWater);
   }

   private static OneShot<net.minecraft.world.entity.PathfinderMob> strollFlyOrSwim(
      float $$0, Function<net.minecraft.world.entity.PathfinderMob, Vec3> $$1, Predicate<net.minecraft.world.entity.PathfinderMob> $$2
   ) {
      return BehaviorBuilder.create($$3 -> $$3.group($$3.absent(MemoryModuleType.WALK_TARGET)).apply($$3, $$3x -> ($$4, $$5, $$6) -> {
         if (!$$2.test($$5)) {
            return false;
         } else {
            Optional<Vec3> $$7 = Optional.ofNullable($$1.apply($$5));
            $$3x.setOrErase($$7.map($$1xxxx -> new WalkTarget($$1xxxx, $$0, 0)));
            return true;
         }
      }));
   }

   
   private static Vec3 getTargetSwimPos(net.minecraft.world.entity.PathfinderMob $$0) {
      Vec3 $$1 = null;
      Vec3 $$2 = null;

      for (int[] $$3 : SWIM_XY_DISTANCE_TIERS) {
         if ($$1 == null) {
            $$2 = BehaviorUtils.getRandomSwimmablePos($$0, $$3[0], $$3[1]);
         } else {
            $$2 = $$0.position().add($$0.position().vectorTo($$1).normalize().multiply($$3[0], $$3[1], $$3[0]));
         }

         boolean $$4 = GoalUtils.mobRestricted($$0, $$3[0]);
         if ($$2 == null || $$0.level().getFluidState(BlockPos.containing($$2)).isEmpty() || GoalUtils.isRestricted($$4, $$0, $$2)) {
            return $$1;
         }

         $$1 = $$2;
      }

      return $$2;
   }

   
   private static Vec3 getTargetFlyPos(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2) {
      Vec3 $$3 = $$0.getViewVector(0.0F);
      return AirAndWaterRandomPos.getPos($$0, $$1, $$2, -2, $$3.x, $$3.z, (float) (Math.PI / 2));
   }
}
