package net.minecraft.world.ticks;

import net.minecraft.core.BlockPos;

public interface LevelTickAccess<T> extends net.minecraft.world.ticks.TickAccess<T> {
   boolean willTickThisTick(BlockPos var1, T var2);
}
