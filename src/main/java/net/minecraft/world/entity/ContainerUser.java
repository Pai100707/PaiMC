package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;

public interface ContainerUser {
   boolean hasContainerOpen(ContainerOpenersCounter var1, BlockPos var2);

   double getContainerInteractionRange();

   default net.minecraft.world.entity.LivingEntity getLivingEntity() {
      if (this instanceof net.minecraft.world.entity.LivingEntity) {
         return (net.minecraft.world.entity.LivingEntity)this;
      } else {
         throw new IllegalStateException("A container user must be a LivingEntity");
      }
   }
}
