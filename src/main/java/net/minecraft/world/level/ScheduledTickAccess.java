package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface ScheduledTickAccess {
   <T> ScheduledTick<T> createTick(BlockPos var1, T var2, int var3, TickPriority var4);

   <T> ScheduledTick<T> createTick(BlockPos var1, T var2, int var3);

   LevelTickAccess<Block> getBlockTicks();

   default void scheduleTick(BlockPos $$0, Block $$1, int $$2, TickPriority $$3) {
      this.getBlockTicks().schedule(this.createTick($$0, $$1, $$2, $$3));
   }

   default void scheduleTick(BlockPos $$0, Block $$1, int $$2) {
      this.getBlockTicks().schedule(this.createTick($$0, $$1, $$2));
   }

   LevelTickAccess<Fluid> getFluidTicks();

   default void scheduleTick(BlockPos $$0, Fluid $$1, int $$2, TickPriority $$3) {
      this.getFluidTicks().schedule(this.createTick($$0, $$1, $$2, $$3));
   }

   default void scheduleTick(BlockPos $$0, Fluid $$1, int $$2) {
      this.getFluidTicks().schedule(this.createTick($$0, $$1, $$2));
   }
}
