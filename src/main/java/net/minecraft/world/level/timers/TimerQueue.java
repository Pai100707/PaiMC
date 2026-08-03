package net.minecraft.world.level.timers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.UnsignedLong;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;

public class TimerQueue<T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String CALLBACK_DATA_TAG = "Callback";
   private static final String TIMER_NAME_TAG = "Name";
   private static final String TIMER_TRIGGER_TIME_TAG = "TriggerTime";
   private final TimerCallbacks<T> callbacksRegistry;
   private final Queue<TimerQueue.Event<T>> queue = new PriorityQueue<>(createComparator());
   private UnsignedLong sequentialId = UnsignedLong.ZERO;
   private final Table<String, Long, TimerQueue.Event<T>> events = HashBasedTable.create();

   private static <T> Comparator<TimerQueue.Event<T>> createComparator() {
      return Comparator.<TimerQueue.Event<T>>comparingLong($$0 -> $$0.triggerTime).thenComparing($$0 -> $$0.sequentialId);
   }

   public TimerQueue(TimerCallbacks<T> $$0, Stream<? extends Dynamic<?>> $$1) {
      this($$0);
      this.queue.clear();
      this.events.clear();
      this.sequentialId = UnsignedLong.ZERO;
      $$1.forEach($$0x -> {
         Tag $$1x = (Tag)$$0x.convert(NbtOps.INSTANCE).getValue();
         if ($$1x instanceof CompoundTag $$2) {
            this.loadEvent($$2);
         } else {
            LOGGER.warn("Invalid format of events: {}", $$1x);
         }
      });
   }

   public TimerQueue(TimerCallbacks<T> $$0) {
      this.callbacksRegistry = $$0;
   }

   public void tick(T $$0, long $$1) {
      while (true) {
         TimerQueue.Event<T> $$2 = this.queue.peek();
         if ($$2 == null || $$2.triggerTime > $$1) {
            return;
         }

         this.queue.remove();
         this.events.remove($$2.id, $$1);
         $$2.callback.handle($$0, this, $$1);
      }
   }

   public void schedule(String $$0, long $$1, TimerCallback<T> $$2) {
      if (!this.events.contains($$0, $$1)) {
         this.sequentialId = this.sequentialId.plus(UnsignedLong.ONE);
         TimerQueue.Event<T> $$3 = new TimerQueue.Event<>($$1, this.sequentialId, $$0, $$2);
         this.events.put($$0, $$1, $$3);
         this.queue.add($$3);
      }
   }

   public int remove(String $$0) {
      Collection<TimerQueue.Event<T>> $$1 = this.events.row($$0).values();
      $$1.forEach(this.queue::remove);
      int $$2 = $$1.size();
      $$1.clear();
      return $$2;
   }

   public Set<String> getEventsIds() {
      return Collections.unmodifiableSet(this.events.rowKeySet());
   }

   private void loadEvent(CompoundTag $$0) {
      TimerCallback<T> $$1 = (TimerCallback<T>)$$0.read("Callback", this.callbacksRegistry.codec()).orElse(null);
      if ($$1 != null) {
         String $$2 = $$0.getStringOr("Name", "");
         long $$3 = $$0.getLongOr("TriggerTime", 0L);
         this.schedule($$2, $$3, $$1);
      }
   }

   private CompoundTag storeEvent(TimerQueue.Event<T> $$0) {
      CompoundTag $$1 = new CompoundTag();
      $$1.putString("Name", $$0.id);
      $$1.putLong("TriggerTime", $$0.triggerTime);
      $$1.store("Callback", this.callbacksRegistry.codec(), $$0.callback);
      return $$1;
   }

   public ListTag store() {
      ListTag $$0 = new ListTag();
      this.queue.stream().sorted(createComparator()).map(this::storeEvent).forEach($$0::add);
      return $$0;
   }

   public static class Event<T> {
      public final long triggerTime;
      public final UnsignedLong sequentialId;
      public final String id;
      public final TimerCallback<T> callback;

      Event(long $$0, UnsignedLong $$1, String $$2, TimerCallback<T> $$3) {
         this.triggerTime = $$0;
         this.sequentialId = $$1;
         this.id = $$2;
         this.callback = $$3;
      }
   }
}
