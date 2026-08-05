package net.minecraft.world.entity;


@FunctionalInterface
public interface EntityProcessor {
   net.minecraft.world.entity.EntityProcessor NOP = $$0 -> $$0;

   
   net.minecraft.world.entity.Entity process(net.minecraft.world.entity.Entity var1);
}
