package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class LevelTicks<T> implements net.minecraft.world.ticks.LevelTickAccess<T> {
   private static final Comparator<net.minecraft.world.ticks.LevelChunkTicks<?>> CONTAINER_DRAIN_ORDER = ($$0, $$1) -> net.minecraft.world.ticks.ScheduledTick.INTRA_TICK_DRAIN_ORDER
      .compare($$0.peek(), $$1.peek());
   private final LongPredicate tickCheck;
   private final Long2ObjectMap<net.minecraft.world.ticks.LevelChunkTicks<T>> allContainers = new Long2ObjectOpenHashMap();
   private final Long2LongMap nextTickForContainer = (Long2LongMap)Util.make(new Long2LongOpenHashMap(), $$0x -> $$0x.defaultReturnValue(Long.MAX_VALUE));
   private final Queue<net.minecraft.world.ticks.LevelChunkTicks<T>> containersToTick = new PriorityQueue<>(CONTAINER_DRAIN_ORDER);
   private final Queue<net.minecraft.world.ticks.ScheduledTick<T>> toRunThisTick = new ArrayDeque<>();
   private final List<net.minecraft.world.ticks.ScheduledTick<T>> alreadyRunThisTick = new ArrayList<>();
   private final Set<net.minecraft.world.ticks.ScheduledTick<?>> toRunThisTickSet = new ObjectOpenCustomHashSet(
      net.minecraft.world.ticks.ScheduledTick.UNIQUE_TICK_HASH
   );
   private final BiConsumer<net.minecraft.world.ticks.LevelChunkTicks<T>, net.minecraft.world.ticks.ScheduledTick<T>> chunkScheduleUpdater = ($$0x, $$1) -> {
      if ($$1.equals($$0x.peek())) {
         this.updateContainerScheduling($$1);
      }
   };

   public LevelTicks(LongPredicate $$0) {
      this.tickCheck = $$0;
   }

   public void addContainer(ChunkPos $$0, net.minecraft.world.ticks.LevelChunkTicks<T> $$1) {
      long $$2 = $$0.toLong();
      this.allContainers.put($$2, $$1);
      net.minecraft.world.ticks.ScheduledTick<T> $$3 = $$1.peek();
      if ($$3 != null) {
         this.nextTickForContainer.put($$2, $$3.triggerTick());
      }

      $$1.setOnTickAdded(this.chunkScheduleUpdater);
   }

   public void removeContainer(ChunkPos $$0) {
      long $$1 = $$0.toLong();
      net.minecraft.world.ticks.LevelChunkTicks<T> $$2 = (net.minecraft.world.ticks.LevelChunkTicks<T>)this.allContainers.remove($$1);
      this.nextTickForContainer.remove($$1);
      if ($$2 != null) {
         $$2.setOnTickAdded(null);
      }
   }

   @Override
   public void schedule(net.minecraft.world.ticks.ScheduledTick<T> $$0) {
      long $$1 = ChunkPos.asLong($$0.pos());
      net.minecraft.world.ticks.LevelChunkTicks<T> $$2 = (net.minecraft.world.ticks.LevelChunkTicks<T>)this.allContainers.get($$1);
      if ($$2 == null) {
         Util.logAndPauseIfInIde("Trying to schedule tick in not loaded position " + $$0.pos());
      } else {
         $$2.schedule($$0);
      }
   }

   public void tick(long $$0, int $$1, BiConsumer<BlockPos, T> $$2) {
      ProfilerFiller $$3 = Profiler.get();
      $$3.push("collect");
      this.collectTicks($$0, $$1, $$3);
      $$3.popPush("run");
      $$3.incrementCounter("ticksToRun", this.toRunThisTick.size());
      this.runCollectedTicks($$2);
      $$3.popPush("cleanup");
      this.cleanupAfterTick();
      $$3.pop();
   }

   private void collectTicks(long $$0, int $$1, ProfilerFiller $$2) {
      this.sortContainersToTick($$0);
      $$2.incrementCounter("containersToTick", this.containersToTick.size());
      this.drainContainers($$0, $$1);
      this.rescheduleLeftoverContainers();
   }

   private void sortContainersToTick(long $$0) {
      ObjectIterator<Entry> $$1 = Long2LongMaps.fastIterator(this.nextTickForContainer);

      while ($$1.hasNext()) {
         Entry $$2 = (Entry)$$1.next();
         long $$3 = $$2.getLongKey();
         long $$4 = $$2.getLongValue();
         if ($$4 <= $$0) {
            net.minecraft.world.ticks.LevelChunkTicks<T> $$5 = (net.minecraft.world.ticks.LevelChunkTicks<T>)this.allContainers.get($$3);
            if ($$5 == null) {
               $$1.remove();
            } else {
               net.minecraft.world.ticks.ScheduledTick<T> $$6 = $$5.peek();
               if ($$6 == null) {
                  $$1.remove();
               } else if ($$6.triggerTick() > $$0) {
                  $$2.setValue($$6.triggerTick());
               } else if (this.tickCheck.test($$3)) {
                  $$1.remove();
                  this.containersToTick.add($$5);
               }
            }
         }
      }
   }

   private void drainContainers(long $$0, int $$1) {
      net.minecraft.world.ticks.LevelChunkTicks<T> $$2;
      while (this.canScheduleMoreTicks($$1) && ($$2 = this.containersToTick.poll()) != null) {
         net.minecraft.world.ticks.ScheduledTick<T> $$3 = $$2.poll();
         this.scheduleForThisTick($$3);
         this.drainFromCurrentContainer(this.containersToTick, $$2, $$0, $$1);
         net.minecraft.world.ticks.ScheduledTick<T> $$4 = $$2.peek();
         if ($$4 != null) {
            if ($$4.triggerTick() <= $$0 && this.canScheduleMoreTicks($$1)) {
               this.containersToTick.add($$2);
            } else {
               this.updateContainerScheduling($$4);
            }
         }
      }
   }

   private void rescheduleLeftoverContainers() {
      for (net.minecraft.world.ticks.LevelChunkTicks<T> $$0 : this.containersToTick) {
         this.updateContainerScheduling($$0.peek());
      }
   }

   private void updateContainerScheduling(net.minecraft.world.ticks.ScheduledTick<T> $$0) {
      this.nextTickForContainer.put(ChunkPos.asLong($$0.pos()), $$0.triggerTick());
   }

   private void drainFromCurrentContainer(
      Queue<net.minecraft.world.ticks.LevelChunkTicks<T>> $$0, net.minecraft.world.ticks.LevelChunkTicks<T> $$1, long $$2, int $$3
   ) {
      if (this.canScheduleMoreTicks($$3)) {
         net.minecraft.world.ticks.LevelChunkTicks<T> $$4 = $$0.peek();
         net.minecraft.world.ticks.ScheduledTick<T> $$5 = $$4 != null ? $$4.peek() : null;

         while (this.canScheduleMoreTicks($$3)) {
            net.minecraft.world.ticks.ScheduledTick<T> $$6 = $$1.peek();
            if ($$6 == null || $$6.triggerTick() > $$2 || $$5 != null && net.minecraft.world.ticks.ScheduledTick.INTRA_TICK_DRAIN_ORDER.compare($$6, $$5) > 0) {
               break;
            }

            $$1.poll();
            this.scheduleForThisTick($$6);
         }
      }
   }

   private void scheduleForThisTick(net.minecraft.world.ticks.ScheduledTick<T> $$0) {
      this.toRunThisTick.add($$0);
   }

   private boolean canScheduleMoreTicks(int $$0) {
      return this.toRunThisTick.size() < $$0;
   }

   private void runCollectedTicks(BiConsumer<BlockPos, T> $$0) {
      while (!this.toRunThisTick.isEmpty()) {
         net.minecraft.world.ticks.ScheduledTick<T> $$1 = this.toRunThisTick.poll();
         if (!this.toRunThisTickSet.isEmpty()) {
            this.toRunThisTickSet.remove($$1);
         }

         this.alreadyRunThisTick.add($$1);
         $$0.accept($$1.pos(), $$1.type());
      }
   }

   private void cleanupAfterTick() {
      this.toRunThisTick.clear();
      this.containersToTick.clear();
      this.alreadyRunThisTick.clear();
      this.toRunThisTickSet.clear();
   }

   @Override
   public boolean hasScheduledTick(BlockPos $$0, T $$1) {
      net.minecraft.world.ticks.LevelChunkTicks<T> $$2 = (net.minecraft.world.ticks.LevelChunkTicks<T>)this.allContainers.get(ChunkPos.asLong($$0));
      return $$2 != null && $$2.hasScheduledTick($$0, $$1);
   }

   @Override
   public boolean willTickThisTick(BlockPos $$0, T $$1) {
      this.calculateTickSetIfNeeded();
      return this.toRunThisTickSet.contains(net.minecraft.world.ticks.ScheduledTick.probe($$1, $$0));
   }

   private void calculateTickSetIfNeeded() {
      if (this.toRunThisTickSet.isEmpty() && !this.toRunThisTick.isEmpty()) {
         this.toRunThisTickSet.addAll(this.toRunThisTick);
      }
   }

   private void forContainersInArea(BoundingBox $$0, net.minecraft.world.ticks.LevelTicks.PosAndContainerConsumer<T> $$1) {
      int $$2 = SectionPos.posToSectionCoord($$0.minX());
      int $$3 = SectionPos.posToSectionCoord($$0.minZ());
      int $$4 = SectionPos.posToSectionCoord($$0.maxX());
      int $$5 = SectionPos.posToSectionCoord($$0.maxZ());

      for (int $$6 = $$2; $$6 <= $$4; $$6++) {
         for (int $$7 = $$3; $$7 <= $$5; $$7++) {
            long $$8 = ChunkPos.asLong($$6, $$7);
            net.minecraft.world.ticks.LevelChunkTicks<T> $$9 = (net.minecraft.world.ticks.LevelChunkTicks<T>)this.allContainers.get($$8);
            if ($$9 != null) {
               $$1.accept($$8, $$9);
            }
         }
      }
   }

   public void clearArea(BoundingBox $$0) {
      Predicate<net.minecraft.world.ticks.ScheduledTick<T>> $$1 = $$1x -> $$0.isInside($$1x.pos());
      this.forContainersInArea($$0, ($$1x, $$2) -> {
         net.minecraft.world.ticks.ScheduledTick<T> $$3 = $$2.peek();
         $$2.removeIf($$1);
         net.minecraft.world.ticks.ScheduledTick<T> $$4 = $$2.peek();
         if ($$4 != $$3) {
            if ($$4 != null) {
               this.updateContainerScheduling($$4);
            } else {
               this.nextTickForContainer.remove($$1x);
            }
         }
      });
      this.alreadyRunThisTick.removeIf($$1);
      this.toRunThisTick.removeIf($$1);
   }

   public void copyArea(BoundingBox $$0, Vec3i $$1) {
      this.copyAreaFrom(this, $$0, $$1);
   }

   public void copyAreaFrom(net.minecraft.world.ticks.LevelTicks<T> $$0, BoundingBox $$1, Vec3i $$2) {
      List<net.minecraft.world.ticks.ScheduledTick<T>> $$3 = new ArrayList<>();
      Predicate<net.minecraft.world.ticks.ScheduledTick<T>> $$4 = $$1x -> $$1.isInside($$1x.pos());
      $$0.alreadyRunThisTick.stream().filter($$4).forEach($$3::add);
      $$0.toRunThisTick.stream().filter($$4).forEach($$3::add);
      $$0.forContainersInArea($$1, ($$2x, $$3x) -> $$3x.getAll().filter($$4).forEach($$3::add));
      LongSummaryStatistics $$5 = $$3.stream().mapToLong(net.minecraft.world.ticks.ScheduledTick::subTickOrder).summaryStatistics();
      long $$6 = $$5.getMin();
      long $$7 = $$5.getMax();
      $$3.forEach(
         $$3x -> this.schedule(
            new net.minecraft.world.ticks.ScheduledTick<>(
               (T)$$3x.type(), $$3x.pos().offset($$2), $$3x.triggerTick(), $$3x.priority(), $$3x.subTickOrder() - $$6 + $$7 + 1L
            )
         )
      );
   }

   @Override
   public int count() {
      return this.allContainers.values().stream().mapToInt(net.minecraft.world.ticks.TickAccess::count).sum();
   }

   @FunctionalInterface
   interface PosAndContainerConsumer<T> {
      void accept(long var1, net.minecraft.world.ticks.LevelChunkTicks<T> var3);
   }
}
