package net.minecraft.world.entity.ai.behavior;

import java.util.function.BiPredicate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class DismountOrSkipMounting {
   public static <E extends net.minecraft.world.entity.LivingEntity> BehaviorControl<E> create(int $$0, BiPredicate<E, net.minecraft.world.entity.Entity> $$1) {
      return BehaviorBuilder.create($$2 -> $$2.group($$2.registered(MemoryModuleType.RIDE_TARGET)).apply($$2, $$3 -> ($$4, $$5, $$6) -> {
         net.minecraft.world.entity.Entity $$7 = $$5.getVehicle();
         net.minecraft.world.entity.Entity $$8 = $$2.<net.minecraft.world.entity.Entity>tryGet($$3).orElse(null);
         if ($$7 == null && $$8 == null) {
            return false;
         } else {
            net.minecraft.world.entity.Entity $$9 = $$7 == null ? $$8 : $$7;
            if (isVehicleValid($$5, $$9, $$0) && !$$1.test((E)$$5, $$9)) {
               return false;
            } else {
               $$5.stopRiding();
               $$3.erase();
               return true;
            }
         }
      }));
   }

   private static boolean isVehicleValid(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.Entity $$1, int $$2) {
      return $$1.isAlive() && $$1.closerThan($$0, $$2) && $$1.level() == $$0.level();
   }
}
