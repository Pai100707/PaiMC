package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GoalUtils {
   public static boolean hasGroundPathNavigation(net.minecraft.world.entity.Mob $$0) {
      return $$0.getNavigation().canNavigateGround();
   }

   public static boolean mobRestricted(net.minecraft.world.entity.PathfinderMob $$0, double $$1) {
      return $$0.hasHome() && $$0.getHomePosition().closerToCenterThan($$0.position(), $$0.getHomeRadius() + $$1 + 1.0);
   }

   public static boolean isOutsideLimits(BlockPos $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      return $$1.level().isOutsideBuildHeight($$0.getY());
   }

   public static boolean isRestricted(boolean $$0, net.minecraft.world.entity.PathfinderMob $$1, BlockPos $$2) {
      return $$0 && !$$1.isWithinHome($$2);
   }

   public static boolean isRestricted(boolean $$0, net.minecraft.world.entity.PathfinderMob $$1, Vec3 $$2) {
      return $$0 && !$$1.isWithinHome($$2);
   }

   public static boolean isNotStable(PathNavigation $$0, BlockPos $$1) {
      return !$$0.isStableDestination($$1);
   }

   public static boolean isWater(net.minecraft.world.entity.PathfinderMob $$0, BlockPos $$1) {
      return $$0.level().getFluidState($$1).is(FluidTags.WATER);
   }

   public static boolean hasMalus(net.minecraft.world.entity.PathfinderMob $$0, BlockPos $$1) {
      return $$0.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic($$0, $$1)) != 0.0F;
   }

   public static boolean isSolid(net.minecraft.world.entity.PathfinderMob $$0, BlockPos $$1) {
      return $$0.level().getBlockState($$1).isSolid();
   }
}
