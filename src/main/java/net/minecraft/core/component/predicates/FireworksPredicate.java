package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

public record FireworksPredicate(Optional<CollectionPredicate<FireworkExplosion, FireworkExplosionPredicate.FireworkPredicate>> explosions, Ints flightDuration)
   implements SingleComponentItemPredicate<Fireworks> {
   public static final Codec<FireworksPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            CollectionPredicate.codec(FireworkExplosionPredicate.FireworkPredicate.CODEC)
               .optionalFieldOf("explosions")
               .forGetter(FireworksPredicate::explosions),
            Ints.CODEC.optionalFieldOf("flight_duration", Ints.ANY).forGetter(FireworksPredicate::flightDuration)
         )
         .apply($$0, FireworksPredicate::new)
   );

   public DataComponentType<Fireworks> componentType() {
      return DataComponents.FIREWORKS;
   }

   public boolean matches(Fireworks $$0) {
      return this.explosions.isPresent() && !this.explosions.get().test($$0.explosions()) ? false : this.flightDuration.matches($$0.flightDuration());
   }
}
