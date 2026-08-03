package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.K1;
import java.util.function.Predicate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;

public class GoToWantedItem {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(float $$0, boolean $$1, int $$2) {
      return create($$0x -> true, $$0, $$1, $$2);
   }

   public static <E extends net.minecraft.world.entity.LivingEntity> BehaviorControl<E> create(Predicate<E> $$0, float $$1, boolean $$2, int $$3) {
      return BehaviorBuilder.create(
         $$4 -> {
            BehaviorBuilder<E, ? extends MemoryAccessor<? extends K1, WalkTarget>> $$5 = $$2
               ? $$4.registered(MemoryModuleType.WALK_TARGET)
               : $$4.absent(MemoryModuleType.WALK_TARGET);
            return $$4.group(
                  $$4.registered(MemoryModuleType.LOOK_TARGET),
                  $$5,
                  $$4.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
                  $$4.registered(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)
               )
               .apply(
                  $$4,
                  ($$4x, $$5x, $$6, $$7) -> ($$8, $$9, $$10) -> {
                     ItemEntity $$11 = $$4.get($$6);
                     if ($$4.tryGet($$7).isEmpty()
                        && $$0.test((E)$$9)
                        && $$11.closerThan($$9, $$3)
                        && $$9.level().getWorldBorder().isWithinBounds($$11.blockPosition())
                        && $$9.canPickUpLoot()) {
                        WalkTarget $$12 = new WalkTarget(new EntityTracker($$11, false), $$1, 0);
                        $$4x.set(new EntityTracker($$11, true));
                        $$5x.set($$12);
                        return true;
                     } else {
                        return false;
                     }
                  }
               );
         }
      );
   }
}
