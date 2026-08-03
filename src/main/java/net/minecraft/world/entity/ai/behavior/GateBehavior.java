package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GateBehavior<E extends net.minecraft.world.entity.LivingEntity> implements BehaviorControl<E> {
   private final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
   private final Set<MemoryModuleType<?>> exitErasedMemories;
   private final GateBehavior.OrderPolicy orderPolicy;
   private final GateBehavior.RunningPolicy runningPolicy;
   private final ShufflingList<BehaviorControl<? super E>> behaviors = new ShufflingList<>();
   private Behavior.Status status = Behavior.Status.STOPPED;

   public GateBehavior(
      Map<MemoryModuleType<?>, MemoryStatus> $$0,
      Set<MemoryModuleType<?>> $$1,
      GateBehavior.OrderPolicy $$2,
      GateBehavior.RunningPolicy $$3,
      List<Pair<? extends BehaviorControl<? super E>, Integer>> $$4
   ) {
      this.entryCondition = $$0;
      this.exitErasedMemories = $$1;
      this.orderPolicy = $$2;
      this.runningPolicy = $$3;
      $$4.forEach($$0x -> this.behaviors.add((BehaviorControl)$$0x.getFirst(), (Integer)$$0x.getSecond()));
   }

   @Override
   public Behavior.Status getStatus() {
      return this.status;
   }

   private boolean hasRequiredMemories(E $$0) {
      for (Entry<MemoryModuleType<?>, MemoryStatus> $$1 : this.entryCondition.entrySet()) {
         MemoryModuleType<?> $$2 = $$1.getKey();
         MemoryStatus $$3 = $$1.getValue();
         if (!$$0.getBrain().checkMemory($$2, $$3)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public final boolean tryStart(ServerLevel $$0, E $$1, long $$2) {
      if (this.hasRequiredMemories($$1)) {
         this.status = Behavior.Status.RUNNING;
         this.orderPolicy.apply(this.behaviors);
         this.runningPolicy.apply(this.behaviors.stream(), $$0, $$1, $$2);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public final void tickOrStop(ServerLevel $$0, E $$1, long $$2) {
      this.behaviors.stream().filter($$0x -> $$0x.getStatus() == Behavior.Status.RUNNING).forEach($$3 -> $$3.tickOrStop($$0, $$1, $$2));
      if (this.behaviors.stream().noneMatch($$0x -> $$0x.getStatus() == Behavior.Status.RUNNING)) {
         this.doStop($$0, $$1, $$2);
      }
   }

   @Override
   public final void doStop(ServerLevel $$0, E $$1, long $$2) {
      this.status = Behavior.Status.STOPPED;
      this.behaviors.stream().filter($$0x -> $$0x.getStatus() == Behavior.Status.RUNNING).forEach($$3 -> $$3.doStop($$0, $$1, $$2));
      this.exitErasedMemories.forEach($$1.getBrain()::eraseMemory);
   }

   @Override
   public String debugString() {
      return this.getClass().getSimpleName();
   }

   @Override
   public String toString() {
      Set<? extends BehaviorControl<? super E>> $$0 = this.behaviors
         .stream()
         .filter($$0x -> $$0x.getStatus() == Behavior.Status.RUNNING)
         .collect(Collectors.toSet());
      return "(" + this.getClass().getSimpleName() + "): " + $$0;
   }

   public static enum OrderPolicy {
      ORDERED($$0 -> {}),
      SHUFFLED(ShufflingList::shuffle);

      private final Consumer<ShufflingList<?>> consumer;

      private OrderPolicy(final Consumer<ShufflingList<?>> $$0) {
         this.consumer = $$0;
      }

      public void apply(ShufflingList<?> $$0) {
         this.consumer.accept($$0);
      }
   }

   public static enum RunningPolicy {
      RUN_ONE {
         @Override
         public <E extends net.minecraft.world.entity.LivingEntity> void apply(Stream<BehaviorControl<? super E>> $$0, ServerLevel $$1, E $$2, long $$3) {
            $$0.filter($$0x -> $$0x.getStatus() == Behavior.Status.STOPPED).filter($$3x -> $$3x.tryStart($$1, $$2, $$3)).findFirst();
         }
      },
      TRY_ALL {
         @Override
         public <E extends net.minecraft.world.entity.LivingEntity> void apply(Stream<BehaviorControl<? super E>> $$0, ServerLevel $$1, E $$2, long $$3) {
            $$0.filter($$0x -> $$0x.getStatus() == Behavior.Status.STOPPED).forEach($$3x -> $$3x.tryStart($$1, $$2, $$3));
         }
      };

      public abstract <E extends net.minecraft.world.entity.LivingEntity> void apply(
         Stream<BehaviorControl<? super E>> var1, ServerLevel var2, E var3, long var4
      );
   }
}
