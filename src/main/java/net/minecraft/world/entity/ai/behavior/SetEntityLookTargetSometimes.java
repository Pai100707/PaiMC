package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

@Deprecated
public class SetEntityLookTargetSometimes {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(float $$0, UniformInt $$1) {
      return create($$0, $$1, $$0x -> true);
   }

   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(net.minecraft.world.entity.EntityType<?> $$0, float $$1, UniformInt $$2) {
      return create($$1, $$2, $$1x -> $$0.equals($$1x.getType()));
   }

   private static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(
      float $$0, UniformInt $$1, Predicate<net.minecraft.world.entity.LivingEntity> $$2
   ) {
      float $$3 = $$0 * $$0;
      SetEntityLookTargetSometimes.Ticker $$4 = new SetEntityLookTargetSometimes.Ticker($$1);
      return BehaviorBuilder.create(
         $$3x -> $$3x.group($$3x.absent(MemoryModuleType.LOOK_TARGET), $$3x.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
            .apply(
               $$3x,
               ($$4x, $$5) -> ($$6, $$7, $$8) -> {
                  Optional<net.minecraft.world.entity.LivingEntity> $$9 = $$3x.<NearestVisibleLivingEntities>get($$5)
                     .findClosest($$2.and($$2xxxx -> $$2xxxx.distanceToSqr($$7) <= $$3));
                  if ($$9.isEmpty()) {
                     return false;
                  } else if (!$$4.tickDownAndCheck($$6.random)) {
                     return false;
                  } else {
                     $$4x.set(new EntityTracker($$9.get(), true));
                     return true;
                  }
               }
            )
      );
   }

   public static final class Ticker {
      private final UniformInt interval;
      private int ticksUntilNextStart;

      public Ticker(UniformInt $$0) {
         if ($$0.getMinValue() <= 1) {
            throw new IllegalArgumentException();
         } else {
            this.interval = $$0;
         }
      }

      public boolean tickDownAndCheck(RandomSource $$0) {
         if (this.ticksUntilNextStart == 0) {
            this.ticksUntilNextStart = this.interval.sample($$0) - 1;
            return false;
         } else {
            return --this.ticksUntilNextStart == 0;
         }
      }
   }
}
