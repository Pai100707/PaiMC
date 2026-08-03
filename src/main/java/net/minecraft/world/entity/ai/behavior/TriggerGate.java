package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public class TriggerGate {
   public static <E extends net.minecraft.world.entity.LivingEntity> OneShot<E> triggerOneShuffled(List<Pair<? extends Trigger<? super E>, Integer>> $$0) {
      return triggerGate($$0, GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE);
   }

   public static <E extends net.minecraft.world.entity.LivingEntity> OneShot<E> triggerGate(
      List<Pair<? extends Trigger<? super E>, Integer>> $$0, GateBehavior.OrderPolicy $$1, GateBehavior.RunningPolicy $$2
   ) {
      ShufflingList<Trigger<? super E>> $$3 = new ShufflingList<>();
      $$0.forEach($$1x -> $$3.add((Trigger)$$1x.getFirst(), (Integer)$$1x.getSecond()));
      return BehaviorBuilder.create($$3x -> $$3x.point(($$3xx, $$4, $$5) -> {
         if ($$1 == GateBehavior.OrderPolicy.SHUFFLED) {
            $$3.shuffle();
         }

         for (Trigger<? super E> $$6 : $$3) {
            if ($$6.trigger($$3xx, $$4, $$5) && $$2 == GateBehavior.RunningPolicy.RUN_ONE) {
               break;
            }
         }

         return true;
      }));
   }
}
