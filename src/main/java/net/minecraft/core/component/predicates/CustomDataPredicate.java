package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.core.component.DataComponentGetter;

public record CustomDataPredicate(NbtPredicate value) implements DataComponentPredicate {
   public static final Codec<CustomDataPredicate> CODEC = NbtPredicate.CODEC.xmap(CustomDataPredicate::new, CustomDataPredicate::value);

   @Override
   public boolean matches(DataComponentGetter $$0) {
      return this.value.matches($$0);
   }

   public static CustomDataPredicate customData(NbtPredicate $$0) {
      return new CustomDataPredicate($$0);
   }
}
