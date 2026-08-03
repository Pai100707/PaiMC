package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class CopyMemoryWithExpiry {
   public static <E extends net.minecraft.world.entity.LivingEntity, T> BehaviorControl<E> create(
      Predicate<E> $$0, MemoryModuleType<? extends T> $$1, MemoryModuleType<T> $$2, UniformInt $$3
   ) {
      return BehaviorBuilder.create($$4 -> $$4.group($$4.present($$1), $$4.absent($$2)).apply($$4, ($$3xx, $$4x) -> ($$5, $$6, $$7) -> {
         if (!$$0.test((E)$$6)) {
            return false;
         } else {
            $$4x.setWithExpiry($$4.get($$3xx), $$3.sample($$5.random));
            return true;
         }
      }));
   }
}
