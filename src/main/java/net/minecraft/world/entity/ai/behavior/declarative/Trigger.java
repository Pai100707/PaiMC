package net.minecraft.world.entity.ai.behavior.declarative;

import net.minecraft.server.level.ServerLevel;

public interface Trigger<E extends net.minecraft.world.entity.LivingEntity> {
   boolean trigger(ServerLevel var1, E var2, long var3);
}
