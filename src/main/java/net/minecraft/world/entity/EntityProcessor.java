package net.minecraft.world.entity;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface EntityProcessor {
   net.minecraft.world.entity.EntityProcessor NOP = $$0 -> $$0;

   @Nullable
   net.minecraft.world.entity.Entity process(net.minecraft.world.entity.Entity var1);
}
