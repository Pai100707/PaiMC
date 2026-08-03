package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.FireworkExplosion.Shape;

public record FireworkExplosionPredicate(FireworkExplosionPredicate.FireworkPredicate predicate) implements SingleComponentItemPredicate<FireworkExplosion> {
   public static final Codec<FireworkExplosionPredicate> CODEC = FireworkExplosionPredicate.FireworkPredicate.CODEC
      .xmap(FireworkExplosionPredicate::new, FireworkExplosionPredicate::predicate);

   public DataComponentType<FireworkExplosion> componentType() {
      return DataComponents.FIREWORK_EXPLOSION;
   }

   public boolean matches(FireworkExplosion $$0) {
      return this.predicate.test($$0);
   }

   public record FireworkPredicate(Optional<Shape> shape, Optional<Boolean> twinkle, Optional<Boolean> trail) implements Predicate<FireworkExplosion> {
      public static final Codec<FireworkExplosionPredicate.FireworkPredicate> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Shape.CODEC.optionalFieldOf("shape").forGetter(FireworkExplosionPredicate.FireworkPredicate::shape),
               Codec.BOOL.optionalFieldOf("has_twinkle").forGetter(FireworkExplosionPredicate.FireworkPredicate::twinkle),
               Codec.BOOL.optionalFieldOf("has_trail").forGetter(FireworkExplosionPredicate.FireworkPredicate::trail)
            )
            .apply($$0, FireworkExplosionPredicate.FireworkPredicate::new)
      );

      public boolean test(FireworkExplosion $$0) {
         if (this.shape.isPresent() && this.shape.get() != $$0.shape()) {
            return false;
         } else {
            return this.twinkle.isPresent() && this.twinkle.get() != $$0.hasTwinkle() ? false : !this.trail.isPresent() || this.trail.get() == $$0.hasTrail();
         }
      }
   }
}
