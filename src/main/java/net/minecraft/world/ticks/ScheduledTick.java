package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash.Strategy;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

public record ScheduledTick<T>(T type, BlockPos pos, long triggerTick, net.minecraft.world.ticks.TickPriority priority, long subTickOrder) {
   public static final Comparator<net.minecraft.world.ticks.ScheduledTick<?>> DRAIN_ORDER = ($$0, $$1) -> {
      int $$2 = Long.compare($$0.triggerTick, $$1.triggerTick);
      if ($$2 != 0) {
         return $$2;
      } else {
         $$2 = $$0.priority.compareTo($$1.priority);
         return $$2 != 0 ? $$2 : Long.compare($$0.subTickOrder, $$1.subTickOrder);
      }
   };
   public static final Comparator<net.minecraft.world.ticks.ScheduledTick<?>> INTRA_TICK_DRAIN_ORDER = ($$0, $$1) -> {
      int $$2 = $$0.priority.compareTo($$1.priority);
      return $$2 != 0 ? $$2 : Long.compare($$0.subTickOrder, $$1.subTickOrder);
   };
   public static final Strategy<net.minecraft.world.ticks.ScheduledTick<?>> UNIQUE_TICK_HASH = new Strategy<net.minecraft.world.ticks.ScheduledTick<?>>() {
      public int hashCode(net.minecraft.world.ticks.ScheduledTick<?> $$0) {
         return 31 * $$0.pos().hashCode() + $$0.type().hashCode();
      }

      public boolean equals(@Nullable net.minecraft.world.ticks.ScheduledTick<?> $$0, @Nullable net.minecraft.world.ticks.ScheduledTick<?> $$1) {
         if ($$0 == $$1) {
            return true;
         } else {
            return $$0 != null && $$1 != null ? $$0.type() == $$1.type() && $$0.pos().equals($$1.pos()) : false;
         }
      }
   };

   public ScheduledTick(T $$0, BlockPos $$1, long $$2, long $$3) {
      this($$0, $$1, $$2, net.minecraft.world.ticks.TickPriority.NORMAL, $$3);
   }

   public ScheduledTick(T type, BlockPos pos, long triggerTick, net.minecraft.world.ticks.TickPriority priority, long subTickOrder) {
      pos = pos.immutable();
      this.type = type;
      this.pos = pos;
      this.triggerTick = triggerTick;
      this.priority = priority;
      this.subTickOrder = subTickOrder;
   }

   public static <T> net.minecraft.world.ticks.ScheduledTick<T> probe(T $$0, BlockPos $$1) {
      return new net.minecraft.world.ticks.ScheduledTick<>($$0, $$1, 0L, net.minecraft.world.ticks.TickPriority.NORMAL, 0L);
   }

   public net.minecraft.world.ticks.SavedTick<T> toSavedTick(long $$0) {
      return new net.minecraft.world.ticks.SavedTick<>(this.type, this.pos, (int)(this.triggerTick - $$0), this.priority);
   }
}
