package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class LevelChunkTicks<T> implements net.minecraft.world.ticks.SerializableTickContainer<T>, net.minecraft.world.ticks.TickContainerAccess<T> {
   private final Queue<net.minecraft.world.ticks.ScheduledTick<T>> tickQueue = new PriorityQueue<>(net.minecraft.world.ticks.ScheduledTick.DRAIN_ORDER);
   
   private List<net.minecraft.world.ticks.SavedTick<T>> pendingTicks;
   private final Set<net.minecraft.world.ticks.ScheduledTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet(
      net.minecraft.world.ticks.ScheduledTick.UNIQUE_TICK_HASH
   );
   
   private BiConsumer<net.minecraft.world.ticks.LevelChunkTicks<T>, net.minecraft.world.ticks.ScheduledTick<T>> onTickAdded;

   public LevelChunkTicks() {
   }

   public LevelChunkTicks(List<net.minecraft.world.ticks.SavedTick<T>> $$0) {
      this.pendingTicks = $$0;

      for (net.minecraft.world.ticks.SavedTick<T> $$1 : $$0) {
         this.ticksPerPosition.add(net.minecraft.world.ticks.ScheduledTick.probe($$1.type(), $$1.pos()));
      }
   }

   public void setOnTickAdded(BiConsumer<net.minecraft.world.ticks.LevelChunkTicks<T>, net.minecraft.world.ticks.ScheduledTick<T>> $$0) {
      this.onTickAdded = $$0;
   }

   
   public net.minecraft.world.ticks.ScheduledTick<T> peek() {
      return this.tickQueue.peek();
   }

   
   public net.minecraft.world.ticks.ScheduledTick<T> poll() {
      net.minecraft.world.ticks.ScheduledTick<T> $$0 = this.tickQueue.poll();
      if ($$0 != null) {
         this.ticksPerPosition.remove($$0);
      }

      return $$0;
   }

   @Override
   public void schedule(net.minecraft.world.ticks.ScheduledTick<T> $$0) {
      if (this.ticksPerPosition.add($$0)) {
         this.scheduleUnchecked($$0);
      }
   }

   private void scheduleUnchecked(net.minecraft.world.ticks.ScheduledTick<T> $$0) {
      this.tickQueue.add($$0);
      if (this.onTickAdded != null) {
         this.onTickAdded.accept(this, $$0);
      }
   }

   @Override
   public boolean hasScheduledTick(BlockPos $$0, T $$1) {
      return this.ticksPerPosition.contains(net.minecraft.world.ticks.ScheduledTick.probe($$1, $$0));
   }

   public void removeIf(Predicate<net.minecraft.world.ticks.ScheduledTick<T>> $$0) {
      Iterator<net.minecraft.world.ticks.ScheduledTick<T>> $$1 = this.tickQueue.iterator();

      while ($$1.hasNext()) {
         net.minecraft.world.ticks.ScheduledTick<T> $$2 = $$1.next();
         if ($$0.test($$2)) {
            $$1.remove();
            this.ticksPerPosition.remove($$2);
         }
      }
   }

   public Stream<net.minecraft.world.ticks.ScheduledTick<T>> getAll() {
      return this.tickQueue.stream();
   }

   @Override
   public int count() {
      return this.tickQueue.size() + (this.pendingTicks != null ? this.pendingTicks.size() : 0);
   }

   @Override
   public List<net.minecraft.world.ticks.SavedTick<T>> pack(long $$0) {
      List<net.minecraft.world.ticks.SavedTick<T>> $$1 = new ArrayList<>(this.tickQueue.size());
      if (this.pendingTicks != null) {
         $$1.addAll(this.pendingTicks);
      }

      for (net.minecraft.world.ticks.ScheduledTick<T> $$2 : this.tickQueue) {
         $$1.add($$2.toSavedTick($$0));
      }

      return $$1;
   }

   public void unpack(long $$0) {
      if (this.pendingTicks != null) {
         int $$1 = -this.pendingTicks.size();

         for (net.minecraft.world.ticks.SavedTick<T> $$2 : this.pendingTicks) {
            this.scheduleUnchecked($$2.unpack($$0, $$1++));
         }
      }

      this.pendingTicks = null;
   }
}
