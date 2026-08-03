package net.minecraft.world.ticks;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;

public class ProtoChunkTicks<T> implements net.minecraft.world.ticks.SerializableTickContainer<T>, net.minecraft.world.ticks.TickContainerAccess<T> {
   private final List<net.minecraft.world.ticks.SavedTick<T>> ticks = Lists.newArrayList();
   private final Set<net.minecraft.world.ticks.SavedTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet(
      net.minecraft.world.ticks.SavedTick.UNIQUE_TICK_HASH
   );

   @Override
   public void schedule(net.minecraft.world.ticks.ScheduledTick<T> $$0) {
      net.minecraft.world.ticks.SavedTick<T> $$1 = new net.minecraft.world.ticks.SavedTick<>($$0.type(), $$0.pos(), 0, $$0.priority());
      this.schedule($$1);
   }

   private void schedule(net.minecraft.world.ticks.SavedTick<T> $$0) {
      if (this.ticksPerPosition.add($$0)) {
         this.ticks.add($$0);
      }
   }

   @Override
   public boolean hasScheduledTick(BlockPos $$0, T $$1) {
      return this.ticksPerPosition.contains(net.minecraft.world.ticks.SavedTick.probe($$1, $$0));
   }

   @Override
   public int count() {
      return this.ticks.size();
   }

   @Override
   public List<net.minecraft.world.ticks.SavedTick<T>> pack(long $$0) {
      return this.ticks;
   }

   public List<net.minecraft.world.ticks.SavedTick<T>> scheduledTicks() {
      return List.copyOf(this.ticks);
   }

   public static <T> net.minecraft.world.ticks.ProtoChunkTicks<T> load(List<net.minecraft.world.ticks.SavedTick<T>> $$0) {
      net.minecraft.world.ticks.ProtoChunkTicks<T> $$1 = new net.minecraft.world.ticks.ProtoChunkTicks<>();
      $$0.forEach($$1::schedule);
      return $$1;
   }
}
