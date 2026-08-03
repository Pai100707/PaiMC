package net.minecraft.world.entity;

import org.jspecify.annotations.Nullable;

public interface Targeting {
   @Nullable
   net.minecraft.world.entity.LivingEntity getTarget();
}
