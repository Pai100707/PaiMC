package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface DispensibleContainerItem {
   default void checkExtraContent(LivingEntity $$0, Level $$1, net.minecraft.world.item.ItemStack $$2, BlockPos $$3) {
   }

   boolean emptyContents(LivingEntity var1, Level var2, BlockPos var3, BlockHitResult var4);
}
