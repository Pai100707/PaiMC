package net.minecraft.world.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Brain<E extends net.minecraft.world.entity.LivingEntity> {
   static final Logger LOGGER = LogUtils.getLogger();
   private final Supplier<Codec<Brain<E>>> codec;
   private static final int SCHEDULE_UPDATE_DELAY = 20;
   private final Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories = Maps.newHashMap();
   private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
   private final Map<Integer, Map<Activity, Set<BehaviorControl<? super E>>>> availableBehaviorsByPriority = Maps.newTreeMap();
   @Nullable
   private EnvironmentAttribute<Activity> schedule;
   private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Maps.newHashMap();
   private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Maps.newHashMap();
   private Set<Activity> coreActivities = Sets.newHashSet();
   private final Set<Activity> activeActivities = Sets.newHashSet();
   private Activity defaultActivity = Activity.IDLE;
   private long lastScheduleUpdate = -9999L;

   public static <E extends net.minecraft.world.entity.LivingEntity> Brain.Provider<E> provider(
      Collection<? extends MemoryModuleType<?>> $$0, Collection<? extends SensorType<? extends Sensor<? super E>>> $$1
   ) {
      return new Brain.Provider<>($$0, $$1);
   }

   public static <E extends net.minecraft.world.entity.LivingEntity> Codec<Brain<E>> codec(
      final Collection<? extends MemoryModuleType<?>> $$0, final Collection<? extends SensorType<? extends Sensor<? super E>>> $$1
   ) {
      final MutableObject<Codec<Brain<E>>> $$2 = new MutableObject();
      $$2.setValue(
         (new MapCodec<Brain<E>>() {
               public <T> Stream<T> keys(DynamicOps<T> $$0x) {
                  return $$0.stream()
                     .flatMap($$0xx -> $$0xx.getCodec().map($$1xxx -> BuiltInRegistries.MEMORY_MODULE_TYPE.getKey($$0xx)).stream())
                     .map($$1xx -> (T)$$0.createString($$1xx.toString()));
               }

               public <T> DataResult<Brain<E>> decode(DynamicOps<T> $$0x, MapLike<T> $$1x) {
                  MutableObject<DataResult<Builder<Brain.MemoryValue<?>>>> $$2x = new MutableObject(DataResult.success(ImmutableList.builder()));
                  $$1.entries().forEach($$2xxx -> {
                     DataResult<MemoryModuleType<?>> $$3x = BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().parse($$0, $$2xxx.getFirst());
                     DataResult<? extends Brain.MemoryValue<?>> $$4 = $$3x.flatMap($$2xxxxx -> this.captureRead($$2xxxxx, $$0, (T)$$2xxx.getSecond()));
                     $$2.setValue(((DataResult)$$2.get()).apply2(Builder::add, $$4));
                  });
                  ImmutableList<Brain.MemoryValue<?>> $$3 = ((DataResult)$$2x.get())
                     .resultOrPartial(Brain.LOGGER::error)
                     .<ImmutableList<Brain.MemoryValue<?>>>map(Builder::build)
                     .orElseGet(ImmutableList::of);
                  return DataResult.success(new Brain<>($$0, $$1, $$3, $$2));
               }

               private <T, U> DataResult<Brain.MemoryValue<U>> captureRead(MemoryModuleType<U> $$0x, DynamicOps<T> $$1x, T $$2x) {
                  return $$0.getCodec()
                     .<DataResult>map(DataResult::success)
                     .orElseGet(() -> DataResult.error(() -> "No codec for memory: " + $$0))
                     .flatMap($$2xxx -> $$2xxx.parse($$1, $$2))
                     .map($$1xxx -> new Brain.MemoryValue<>($$0, Optional.of($$1xxx)));
               }

               public <T> RecordBuilder<T> encode(Brain<E> $$0x, DynamicOps<T> $$1x, RecordBuilder<T> $$2x) {
                  $$0.memories().forEach($$2xxx -> $$2xxx.serialize($$1, $$2));
                  return $$2;
               }
            })
            .fieldOf("memories")
            .codec()
      );
      return (Codec<Brain<E>>)$$2.get();
   }

   public Brain(
      Collection<? extends MemoryModuleType<?>> $$0,
      Collection<? extends SensorType<? extends Sensor<? super E>>> $$1,
      ImmutableList<Brain.MemoryValue<?>> $$2,
      Supplier<Codec<Brain<E>>> $$3
   ) {
      this.codec = $$3;

      for (MemoryModuleType<?> $$4 : $$0) {
         this.memories.put($$4, Optional.empty());
      }

      for (SensorType<? extends Sensor<? super E>> $$5 : $$1) {
         this.sensors.put($$5, (Sensor<? super E>)$$5.create());
      }

      for (Sensor<? super E> $$6 : this.sensors.values()) {
         for (MemoryModuleType<?> $$7 : $$6.requires()) {
            this.memories.put($$7, Optional.empty());
         }
      }

      UnmodifiableIterator var11 = $$2.iterator();

      while (var11.hasNext()) {
         Brain.MemoryValue<?> $$8 = (Brain.MemoryValue<?>)var11.next();
         $$8.setMemoryInternal(this);
      }
   }

   public <T> DataResult<T> serializeStart(DynamicOps<T> $$0) {
      return this.codec.get().encodeStart($$0, this);
   }

   Stream<Brain.MemoryValue<?>> memories() {
      return this.memories.entrySet().stream().map($$0 -> Brain.MemoryValue.createUnchecked($$0.getKey(), $$0.getValue()));
   }

   public boolean hasMemoryValue(MemoryModuleType<?> $$0) {
      return this.checkMemory($$0, MemoryStatus.VALUE_PRESENT);
   }

   public void clearMemories() {
      this.memories.keySet().forEach($$0 -> this.memories.put((MemoryModuleType<?>)$$0, Optional.empty()));
   }

   public <U> void eraseMemory(MemoryModuleType<U> $$0) {
      this.setMemory($$0, Optional.empty());
   }

   public <U> void setMemory(MemoryModuleType<U> $$0, @Nullable U $$1) {
      this.setMemory($$0, Optional.ofNullable($$1));
   }

   public <U> void setMemoryWithExpiry(MemoryModuleType<U> $$0, U $$1, long $$2) {
      this.setMemoryInternal($$0, Optional.of(ExpirableValue.of($$1, $$2)));
   }

   public <U> void setMemory(MemoryModuleType<U> $$0, Optional<? extends U> $$1) {
      this.setMemoryInternal($$0, $$1.map(ExpirableValue::of));
   }

   <U> void setMemoryInternal(MemoryModuleType<U> $$0, Optional<? extends ExpirableValue<?>> $$1) {
      if (this.memories.containsKey($$0)) {
         if ($$1.isPresent() && this.isEmptyCollection($$1.get().getValue())) {
            this.eraseMemory($$0);
         } else {
            this.memories.put($$0, $$1);
         }
      }
   }

   public <U> Optional<U> getMemory(MemoryModuleType<U> $$0) {
      Optional<? extends ExpirableValue<?>> $$1 = this.memories.get($$0);
      if ($$1 == null) {
         throw new IllegalStateException("Unregistered memory fetched: " + $$0);
      } else {
         return $$1.map(ExpirableValue::getValue);
      }
   }

   @Nullable
   public <U> Optional<U> getMemoryInternal(MemoryModuleType<U> $$0) {
      Optional<? extends ExpirableValue<?>> $$1 = this.memories.get($$0);
      return $$1 == null ? null : $$1.map(ExpirableValue::getValue);
   }

   public <U> long getTimeUntilExpiry(MemoryModuleType<U> $$0) {
      Optional<? extends ExpirableValue<?>> $$1 = this.memories.get($$0);
      return $$1.map(ExpirableValue::getTimeToLive).orElse(0L);
   }

   @Deprecated
   @VisibleForDebug
   public Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories() {
      return this.memories;
   }

   public <U> boolean isMemoryValue(MemoryModuleType<U> $$0, U $$1) {
      return !this.hasMemoryValue($$0) ? false : this.getMemory($$0).filter($$1x -> $$1x.equals($$1)).isPresent();
   }

   public boolean checkMemory(MemoryModuleType<?> $$0, MemoryStatus $$1) {
      Optional<? extends ExpirableValue<?>> $$2 = this.memories.get($$0);
      return $$2 == null
         ? false
         : $$1 == MemoryStatus.REGISTERED || $$1 == MemoryStatus.VALUE_PRESENT && $$2.isPresent() || $$1 == MemoryStatus.VALUE_ABSENT && $$2.isEmpty();
   }

   public void setSchedule(EnvironmentAttribute<Activity> $$0) {
      this.schedule = $$0;
   }

   public void setCoreActivities(Set<Activity> $$0) {
      this.coreActivities = $$0;
   }

   @Deprecated
   @VisibleForDebug
   public Set<Activity> getActiveActivities() {
      return this.activeActivities;
   }

   @Deprecated
   @VisibleForDebug
   public List<BehaviorControl<? super E>> getRunningBehaviors() {
      List<BehaviorControl<? super E>> $$0 = new ObjectArrayList();

      for (Map<Activity, Set<BehaviorControl<? super E>>> $$1 : this.availableBehaviorsByPriority.values()) {
         for (Set<BehaviorControl<? super E>> $$2 : $$1.values()) {
            for (BehaviorControl<? super E> $$3 : $$2) {
               if ($$3.getStatus() == Behavior.Status.RUNNING) {
                  $$0.add($$3);
               }
            }
         }
      }

      return $$0;
   }

   public void useDefaultActivity() {
      this.setActiveActivity(this.defaultActivity);
   }

   public Optional<Activity> getActiveNonCoreActivity() {
      for (Activity $$0 : this.activeActivities) {
         if (!this.coreActivities.contains($$0)) {
            return Optional.of($$0);
         }
      }

      return Optional.empty();
   }

   public void setActiveActivityIfPossible(Activity $$0) {
      if (this.activityRequirementsAreMet($$0)) {
         this.setActiveActivity($$0);
      } else {
         this.useDefaultActivity();
      }
   }

   private void setActiveActivity(Activity $$0) {
      if (!this.isActive($$0)) {
         this.eraseMemoriesForOtherActivitesThan($$0);
         this.activeActivities.clear();
         this.activeActivities.addAll(this.coreActivities);
         this.activeActivities.add($$0);
      }
   }

   private void eraseMemoriesForOtherActivitesThan(Activity $$0) {
      for (Activity $$1 : this.activeActivities) {
         if ($$1 != $$0) {
            Set<MemoryModuleType<?>> $$2 = this.activityMemoriesToEraseWhenStopped.get($$1);
            if ($$2 != null) {
               for (MemoryModuleType<?> $$3 : $$2) {
                  this.eraseMemory($$3);
               }
            }
         }
      }
   }

   public void updateActivityFromSchedule(EnvironmentAttributeSystem $$0, long $$1, Vec3 $$2) {
      if ($$1 - this.lastScheduleUpdate > 20L) {
         this.lastScheduleUpdate = $$1;
         Activity $$3 = this.schedule != null ? (Activity)$$0.getValue(this.schedule, $$2) : Activity.IDLE;
         if (!this.activeActivities.contains($$3)) {
            this.setActiveActivityIfPossible($$3);
         }
      }
   }

   public void setActiveActivityToFirstValid(List<Activity> $$0) {
      for (Activity $$1 : $$0) {
         if (this.activityRequirementsAreMet($$1)) {
            this.setActiveActivity($$1);
            break;
         }
      }
   }

   public void setDefaultActivity(Activity $$0) {
      this.defaultActivity = $$0;
   }

   public void addActivity(Activity $$0, int $$1, ImmutableList<? extends BehaviorControl<? super E>> $$2) {
      this.addActivity($$0, this.createPriorityPairs($$1, $$2));
   }

   public void addActivityAndRemoveMemoryWhenStopped(Activity $$0, int $$1, ImmutableList<? extends BehaviorControl<? super E>> $$2, MemoryModuleType<?> $$3) {
      Set<Pair<MemoryModuleType<?>, MemoryStatus>> $$4 = ImmutableSet.of(Pair.of($$3, MemoryStatus.VALUE_PRESENT));
      Set<MemoryModuleType<?>> $$5 = ImmutableSet.of($$3);
      this.addActivityAndRemoveMemoriesWhenStopped($$0, this.createPriorityPairs($$1, $$2), $$4, $$5);
   }

   public void addActivity(Activity $$0, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> $$1) {
      this.addActivityAndRemoveMemoriesWhenStopped($$0, $$1, ImmutableSet.of(), Sets.newHashSet());
   }

   public void addActivityWithConditions(
      Activity $$0, int $$1, ImmutableList<? extends BehaviorControl<? super E>> $$2, Set<Pair<MemoryModuleType<?>, MemoryStatus>> $$3
   ) {
      this.addActivityWithConditions($$0, this.createPriorityPairs($$1, $$2), $$3);
   }

   public void addActivityWithConditions(
      Activity $$0, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> $$1, Set<Pair<MemoryModuleType<?>, MemoryStatus>> $$2
   ) {
      this.addActivityAndRemoveMemoriesWhenStopped($$0, $$1, $$2, Sets.newHashSet());
   }

   public void addActivityAndRemoveMemoriesWhenStopped(
      Activity $$0,
      ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> $$1,
      Set<Pair<MemoryModuleType<?>, MemoryStatus>> $$2,
      Set<MemoryModuleType<?>> $$3
   ) {
      this.activityRequirements.put($$0, $$2);
      if (!$$3.isEmpty()) {
         this.activityMemoriesToEraseWhenStopped.put($$0, $$3);
      }

      UnmodifiableIterator var5 = $$1.iterator();

      while (var5.hasNext()) {
         Pair<Integer, ? extends BehaviorControl<? super E>> $$4 = (Pair<Integer, ? extends BehaviorControl<? super E>>)var5.next();
         this.availableBehaviorsByPriority
            .computeIfAbsent((Integer)$$4.getFirst(), $$0x -> Maps.newHashMap())
            .computeIfAbsent($$0, $$0x -> Sets.newLinkedHashSet())
            .add((BehaviorControl<? super E>)$$4.getSecond());
      }
   }

   @VisibleForTesting
   public void removeAllBehaviors() {
      this.availableBehaviorsByPriority.clear();
   }

   public boolean isActive(Activity $$0) {
      return this.activeActivities.contains($$0);
   }

   public Brain<E> copyWithoutBehaviors() {
      Brain<E> $$0 = new Brain<>(this.memories.keySet(), this.sensors.keySet(), ImmutableList.of(), this.codec);

      for (Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> $$1 : this.memories.entrySet()) {
         MemoryModuleType<?> $$2 = $$1.getKey();
         if ($$1.getValue().isPresent()) {
            $$0.memories.put($$2, $$1.getValue());
         }
      }

      return $$0;
   }

   public void tick(ServerLevel $$0, E $$1) {
      this.forgetOutdatedMemories();
      this.tickSensors($$0, $$1);
      this.startEachNonRunningBehavior($$0, $$1);
      this.tickEachRunningBehavior($$0, $$1);
   }

   private void tickSensors(ServerLevel $$0, E $$1) {
      for (Sensor<? super E> $$2 : this.sensors.values()) {
         $$2.tick($$0, $$1);
      }
   }

   private void forgetOutdatedMemories() {
      for (Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> $$0 : this.memories.entrySet()) {
         if ($$0.getValue().isPresent()) {
            ExpirableValue<?> $$1 = (ExpirableValue<?>)$$0.getValue().get();
            if ($$1.hasExpired()) {
               this.eraseMemory($$0.getKey());
            }

            $$1.tick();
         }
      }
   }

   public void stopAll(ServerLevel $$0, E $$1) {
      long $$2 = $$1.level().getGameTime();

      for (BehaviorControl<? super E> $$3 : this.getRunningBehaviors()) {
         $$3.doStop($$0, $$1, $$2);
      }
   }

   private void startEachNonRunningBehavior(ServerLevel $$0, E $$1) {
      long $$2 = $$0.getGameTime();

      for (Map<Activity, Set<BehaviorControl<? super E>>> $$3 : this.availableBehaviorsByPriority.values()) {
         for (Entry<Activity, Set<BehaviorControl<? super E>>> $$4 : $$3.entrySet()) {
            Activity $$5 = $$4.getKey();
            if (this.activeActivities.contains($$5)) {
               for (BehaviorControl<? super E> $$7 : $$4.getValue()) {
                  if ($$7.getStatus() == Behavior.Status.STOPPED) {
                     $$7.tryStart($$0, $$1, $$2);
                  }
               }
            }
         }
      }
   }

   private void tickEachRunningBehavior(ServerLevel $$0, E $$1) {
      long $$2 = $$0.getGameTime();

      for (BehaviorControl<? super E> $$3 : this.getRunningBehaviors()) {
         $$3.tickOrStop($$0, $$1, $$2);
      }
   }

   private boolean activityRequirementsAreMet(Activity $$0) {
      if (!this.activityRequirements.containsKey($$0)) {
         return false;
      } else {
         for (Pair<MemoryModuleType<?>, MemoryStatus> $$1 : this.activityRequirements.get($$0)) {
            MemoryModuleType<?> $$2 = (MemoryModuleType<?>)$$1.getFirst();
            MemoryStatus $$3 = (MemoryStatus)$$1.getSecond();
            if (!this.checkMemory($$2, $$3)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean isEmptyCollection(Object $$0) {
      return $$0 instanceof Collection && ((Collection)$$0).isEmpty();
   }

   ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> createPriorityPairs(
      int $$0, ImmutableList<? extends BehaviorControl<? super E>> $$1
   ) {
      int $$2 = $$0;
      Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> $$3 = ImmutableList.builder();
      UnmodifiableIterator var5 = $$1.iterator();

      while (var5.hasNext()) {
         BehaviorControl<? super E> $$4 = (BehaviorControl<? super E>)var5.next();
         $$3.add(Pair.of($$2++, $$4));
      }

      return $$3.build();
   }

   public boolean isBrainDead() {
      return this.memories.isEmpty() && this.sensors.isEmpty() && this.availableBehaviorsByPriority.isEmpty();
   }

   static final class MemoryValue<U> {
      private final MemoryModuleType<U> type;
      private final Optional<? extends ExpirableValue<U>> value;

      static <U> Brain.MemoryValue<U> createUnchecked(MemoryModuleType<U> $$0, Optional<? extends ExpirableValue<?>> $$1) {
         return new Brain.MemoryValue<>($$0, (Optional<? extends ExpirableValue<U>>)$$1);
      }

      MemoryValue(MemoryModuleType<U> $$0, Optional<? extends ExpirableValue<U>> $$1) {
         this.type = $$0;
         this.value = $$1;
      }

      void setMemoryInternal(Brain<?> $$0) {
         $$0.setMemoryInternal(this.type, this.value);
      }

      public <T> void serialize(DynamicOps<T> $$0, RecordBuilder<T> $$1) {
         this.type
            .getCodec()
            .ifPresent(
               $$2 -> this.value
                  .ifPresent($$3 -> $$1.add(BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().encodeStart($$0, this.type), $$2.encodeStart($$0, $$3)))
            );
      }
   }

   public static final class Provider<E extends net.minecraft.world.entity.LivingEntity> {
      private final Collection<? extends MemoryModuleType<?>> memoryTypes;
      private final Collection<? extends SensorType<? extends Sensor<? super E>>> sensorTypes;
      private final Codec<Brain<E>> codec;

      Provider(Collection<? extends MemoryModuleType<?>> $$0, Collection<? extends SensorType<? extends Sensor<? super E>>> $$1) {
         this.memoryTypes = $$0;
         this.sensorTypes = $$1;
         this.codec = Brain.codec($$0, $$1);
      }

      public Brain<E> makeBrain(Dynamic<?> $$0) {
         return this.codec
            .parse($$0)
            .resultOrPartial(Brain.LOGGER::error)
            .orElseGet(() -> new Brain<>(this.memoryTypes, this.sensorTypes, ImmutableList.of(), () -> this.codec));
      }
   }
}
