package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WaterAvoidingRandomStrollGoal extends RandomStrollGoal {
   public static final float PROBABILITY = 0.001F;
   protected final float probability;

   public WaterAvoidingRandomStrollGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1) {
      this($$0, $$1, 0.001F);
   }

   public WaterAvoidingRandomStrollGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, float $$2) {
      super($$0, $$1);
      this.probability = $$2;
   }

   @Nullable
   @Override
   protected Vec3 getPosition() {
      if (this.mob.isInWater()) {
         Vec3 $$0 = LandRandomPos.getPos(this.mob, 15, 7);
         return $$0 == null ? super.getPosition() : $$0;
      } else {
         return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
      }
   }
}
