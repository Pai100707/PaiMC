package net.minecraft.world.ticks;

import net.minecraft.core.BlockPos;

public class BlackholeTickAccess {
   private static final net.minecraft.world.ticks.TickContainerAccess<Object> CONTAINER_BLACKHOLE = new net.minecraft.world.ticks.TickContainerAccess<Object>() {
      @Override
      public void schedule(net.minecraft.world.ticks.ScheduledTick<Object> $$0) {
      }

      @Override
      public boolean hasScheduledTick(BlockPos $$0, Object $$1) {
         return false;
      }

      @Override
      public int count() {
         return 0;
      }
   };
   private static final net.minecraft.world.ticks.LevelTickAccess<Object> LEVEL_BLACKHOLE = new net.minecraft.world.ticks.LevelTickAccess<Object>() {
      @Override
      public void schedule(net.minecraft.world.ticks.ScheduledTick<Object> $$0) {
      }

      @Override
      public boolean hasScheduledTick(BlockPos $$0, Object $$1) {
         return false;
      }

      @Override
      public boolean willTickThisTick(BlockPos $$0, Object $$1) {
         return false;
      }

      @Override
      public int count() {
         return 0;
      }
   };

   public static <T> net.minecraft.world.ticks.TickContainerAccess<T> emptyContainer() {
      return (net.minecraft.world.ticks.TickContainerAccess<T>)CONTAINER_BLACKHOLE;
   }

   public static <T> net.minecraft.world.ticks.LevelTickAccess<T> emptyLevelList() {
      return (net.minecraft.world.ticks.LevelTickAccess<T>)LEVEL_BLACKHOLE;
   }
}
