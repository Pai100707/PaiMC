package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public record MobEffectsPredicate(Map<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> effectMap) {
   public static final Codec<MobEffectsPredicate> CODEC = Codec.unboundedMap(MobEffect.CODEC, MobEffectsPredicate.MobEffectInstancePredicate.CODEC)
      .xmap(MobEffectsPredicate::new, MobEffectsPredicate::effectMap);

   public boolean matches(Entity $$0) {
      return $$0 instanceof LivingEntity $$1 && this.matches($$1.getActiveEffectsMap());
   }

   public boolean matches(LivingEntity $$0) {
      return this.matches($$0.getActiveEffectsMap());
   }

   public boolean matches(Map<Holder<MobEffect>, MobEffectInstance> $$0) {
      for (Entry<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> $$1 : this.effectMap.entrySet()) {
         MobEffectInstance $$2 = $$0.get($$1.getKey());
         if (!$$1.getValue().matches($$2)) {
            return false;
         }
      }

      return true;
   }

   public static class Builder {
      private final com.google.common.collect.ImmutableMap.Builder<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> effectMap = ImmutableMap.builder();

      public static MobEffectsPredicate.Builder effects() {
         return new MobEffectsPredicate.Builder();
      }

      public MobEffectsPredicate.Builder and(Holder<MobEffect> $$0) {
         this.effectMap.put($$0, new MobEffectsPredicate.MobEffectInstancePredicate());
         return this;
      }

      public MobEffectsPredicate.Builder and(Holder<MobEffect> $$0, MobEffectsPredicate.MobEffectInstancePredicate $$1) {
         this.effectMap.put($$0, $$1);
         return this;
      }

      public Optional<MobEffectsPredicate> build() {
         return Optional.of(new MobEffectsPredicate(this.effectMap.build()));
      }
   }

   public record MobEffectInstancePredicate(MinMaxBounds.Ints amplifier, MinMaxBounds.Ints duration, Optional<Boolean> ambient, Optional<Boolean> visible) {
      public static final Codec<MobEffectsPredicate.MobEffectInstancePredicate> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               MinMaxBounds.Ints.CODEC.optionalFieldOf("amplifier", MinMaxBounds.Ints.ANY).forGetter(MobEffectsPredicate.MobEffectInstancePredicate::amplifier),
               MinMaxBounds.Ints.CODEC.optionalFieldOf("duration", MinMaxBounds.Ints.ANY).forGetter(MobEffectsPredicate.MobEffectInstancePredicate::duration),
               Codec.BOOL.optionalFieldOf("ambient").forGetter(MobEffectsPredicate.MobEffectInstancePredicate::ambient),
               Codec.BOOL.optionalFieldOf("visible").forGetter(MobEffectsPredicate.MobEffectInstancePredicate::visible)
            )
            .apply($$0, MobEffectsPredicate.MobEffectInstancePredicate::new)
      );

      public MobEffectInstancePredicate() {
         this(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty());
      }

      public boolean matches(@Nullable MobEffectInstance $$0) {
         if ($$0 == null) {
            return false;
         } else if (!this.amplifier.matches($$0.getAmplifier())) {
            return false;
         } else if (!this.duration.matches($$0.getDuration())) {
            return false;
         } else {
            return this.ambient.isPresent() && this.ambient.get() != $$0.isAmbient()
               ? false
               : !this.visible.isPresent() || this.visible.get() == $$0.isVisible();
         }
      }
   }
}
