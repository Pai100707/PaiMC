package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;

public record DamagePredicate(Ints durability, Ints damage) implements DataComponentPredicate {
   public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Ints.CODEC.optionalFieldOf("durability", Ints.ANY).forGetter(DamagePredicate::durability),
            Ints.CODEC.optionalFieldOf("damage", Ints.ANY).forGetter(DamagePredicate::damage)
         )
         .apply($$0, DamagePredicate::new)
   );

   @Override
   public boolean matches(DataComponentGetter $$0) {
      Integer $$1 = $$0.get(DataComponents.DAMAGE);
      if ($$1 == null) {
         return false;
      } else {
         int $$2 = $$0.getOrDefault(DataComponents.MAX_DAMAGE, 0);
         return !this.durability.matches($$2 - $$1) ? false : this.damage.matches($$1);
      }
   }

   public static DamagePredicate durability(Ints $$0) {
      return new DamagePredicate($$0, Ints.ANY);
   }
}
