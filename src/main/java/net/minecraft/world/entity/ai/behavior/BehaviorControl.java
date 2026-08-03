package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;

public interface BehaviorControl<E extends net.minecraft.world.entity.LivingEntity> {
   Behavior.Status getStatus();

   boolean tryStart(ServerLevel var1, E var2, long var3);

   void tickOrStop(ServerLevel var1, E var2, long var3);

   void doStop(ServerLevel var1, E var2, long var3);

   String debugString();
}
