package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MoveBackToVillageGoal extends RandomStrollGoal {
   private static final int MAX_XZ_DIST = 10;
   private static final int MAX_Y_DIST = 7;

   public MoveBackToVillageGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, boolean $$2) {
      super($$0, $$1, 10, $$2);
   }

   @Override
   public boolean canUse() {
      ServerLevel $$0 = (ServerLevel)this.mob.level();
      BlockPos $$1 = this.mob.blockPosition();
      return $$0.isVillage($$1) ? false : super.canUse();
   }

   @Nullable
   @Override
   protected Vec3 getPosition() {
      ServerLevel $$0 = (ServerLevel)this.mob.level();
      BlockPos $$1 = this.mob.blockPosition();
      SectionPos $$2 = SectionPos.of($$1);
      SectionPos $$3 = BehaviorUtils.findSectionClosestToVillage($$0, $$2, 2);
      return $$3 != $$2 ? DefaultRandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf($$3.center()), (float) (Math.PI / 2)) : null;
   }
}
