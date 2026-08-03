package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public abstract class OneShot<E extends net.minecraft.world.entity.LivingEntity> implements BehaviorControl<E>, Trigger<E> {
   private Behavior.Status status = Behavior.Status.STOPPED;

   @Override
   public final Behavior.Status getStatus() {
      return this.status;
   }

   @Override
   public final boolean tryStart(ServerLevel $$0, E $$1, long $$2) {
      if (this.trigger($$0, $$1, $$2)) {
         this.status = Behavior.Status.RUNNING;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public final void tickOrStop(ServerLevel $$0, E $$1, long $$2) {
      this.doStop($$0, $$1, $$2);
   }

   @Override
   public final void doStop(ServerLevel $$0, E $$1, long $$2) {
      this.status = Behavior.Status.STOPPED;
   }

   @Override
   public String debugString() {
      return this.getClass().getSimpleName();
   }
}
