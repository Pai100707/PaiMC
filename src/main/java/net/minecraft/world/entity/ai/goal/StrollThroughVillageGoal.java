package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class StrollThroughVillageGoal extends Goal {
   private static final int DISTANCE_THRESHOLD = 10;
   private final net.minecraft.world.entity.PathfinderMob mob;
   private final int interval;
   @Nullable
   private BlockPos wantedPos;

   public StrollThroughVillageGoal(net.minecraft.world.entity.PathfinderMob $$0, int $$1) {
      this.mob = $$0;
      this.interval = reducedTickDelay($$1);
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.mob.hasControllingPassenger()) {
         return false;
      } else if (this.mob.level().isBrightOutside()) {
         return false;
      } else if (this.mob.getRandom().nextInt(this.interval) != 0) {
         return false;
      } else {
         ServerLevel $$0 = (ServerLevel)this.mob.level();
         BlockPos $$1 = this.mob.blockPosition();
         if (!$$0.isCloseToVillage($$1, 6)) {
            return false;
         } else {
            Vec3 $$2 = LandRandomPos.getPos(this.mob, 15, 7, $$1x -> -$$0.sectionsToVillage(SectionPos.of($$1x)));
            this.wantedPos = $$2 == null ? null : BlockPos.containing($$2);
            return this.wantedPos != null;
         }
      }
   }

   @Override
   public boolean canContinueToUse() {
      return this.wantedPos != null && !this.mob.getNavigation().isDone() && this.mob.getNavigation().getTargetPos().equals(this.wantedPos);
   }

   @Override
   public void tick() {
      if (this.wantedPos != null) {
         PathNavigation $$0 = this.mob.getNavigation();
         if ($$0.isDone() && !this.wantedPos.closerToCenterThan(this.mob.position(), 10.0)) {
            Vec3 $$1 = Vec3.atBottomCenterOf(this.wantedPos);
            Vec3 $$2 = this.mob.position();
            Vec3 $$3 = $$2.subtract($$1);
            $$1 = $$3.scale(0.4).add($$1);
            Vec3 $$4 = $$1.subtract($$2).normalize().scale(10.0).add($$2);
            BlockPos $$5 = BlockPos.containing($$4);
            $$5 = this.mob.level().getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, $$5);
            if (!$$0.moveTo($$5.getX(), $$5.getY(), $$5.getZ(), 1.0)) {
               this.moveRandomly();
            }
         }
      }
   }

   private void moveRandomly() {
      RandomSource $$0 = this.mob.getRandom();
      BlockPos $$1 = this.mob
         .level()
         .getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, this.mob.blockPosition().offset(-8 + $$0.nextInt(16), 0, -8 + $$0.nextInt(16)));
      this.mob.getNavigation().moveTo($$1.getX(), $$1.getY(), $$1.getZ(), 1.0);
   }
}
