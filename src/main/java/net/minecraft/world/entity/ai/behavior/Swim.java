package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;

public class Swim<T extends net.minecraft.world.entity.Mob> extends Behavior<T> {
   private final float chance;

   public Swim(float $$0) {
      super(ImmutableMap.of());
      this.chance = $$0;
   }

   public static <T extends net.minecraft.world.entity.Mob> boolean shouldSwim(T $$0) {
      return $$0.isInWater() && $$0.getFluidHeight(FluidTags.WATER) > $$0.getFluidJumpThreshold() || $$0.isInLava();
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, net.minecraft.world.entity.Mob $$1) {
      return shouldSwim($$1);
   }

   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      return this.checkExtraStartConditions($$0, $$1);
   }

   protected void tick(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      if ($$1.getRandom().nextFloat() < this.chance) {
         $$1.getJumpControl().jump();
      }
   }
}
