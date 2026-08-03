package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;

public class ForceUnmount extends Behavior<net.minecraft.world.entity.LivingEntity> {
   public ForceUnmount() {
      super(ImmutableMap.of());
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      return $$1.isPassenger();
   }

   @Override
   protected void start(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, long $$2) {
      $$1.unRide();
   }
}
