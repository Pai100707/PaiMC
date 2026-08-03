package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.tags.FluidTags;

public class FloatGoal extends Goal {
   private final net.minecraft.world.entity.Mob mob;

   public FloatGoal(net.minecraft.world.entity.Mob $$0) {
      this.mob = $$0;
      this.setFlags(EnumSet.of(Goal.Flag.JUMP));
      $$0.getNavigation().setCanFloat(true);
   }

   @Override
   public boolean canUse() {
      return this.mob.isInWater() && this.mob.getFluidHeight(FluidTags.WATER) > this.mob.getFluidJumpThreshold() || this.mob.isInLava();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.mob.getRandom().nextFloat() < 0.8F) {
         this.mob.getJumpControl().jump();
      }
   }
}
