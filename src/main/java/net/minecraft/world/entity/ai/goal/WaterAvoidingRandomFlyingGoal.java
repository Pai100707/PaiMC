package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WaterAvoidingRandomFlyingGoal extends WaterAvoidingRandomStrollGoal {
   public WaterAvoidingRandomFlyingGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1) {
      super($$0, $$1);
   }

   @Nullable
   @Override
   protected Vec3 getPosition() {
      Vec3 $$0 = this.mob.getViewVector(0.0F);
      int $$1 = 8;
      Vec3 $$2 = HoverRandomPos.getPos(this.mob, 8, 7, $$0.x, $$0.z, (float) (Math.PI / 2), 3, 1);
      return $$2 != null ? $$2 : AirAndWaterRandomPos.getPos(this.mob, 8, 4, -2, $$0.x, $$0.z, (float) (Math.PI / 2));
   }
}
