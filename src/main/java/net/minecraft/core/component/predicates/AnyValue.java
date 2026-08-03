package net.minecraft.core.component.predicates;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;

public record AnyValue(DataComponentType<?> type) implements DataComponentPredicate {
   @Override
   public boolean matches(DataComponentGetter $$0) {
      return $$0.get(this.type) != null;
   }
}
