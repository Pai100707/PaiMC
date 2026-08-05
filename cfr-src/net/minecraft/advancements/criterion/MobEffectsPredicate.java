/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public record MobEffectsPredicate(Map<Holder<MobEffect>, MobEffectInstancePredicate> effectMap) {
    public static final Codec<MobEffectsPredicate> CODEC = Codec.unboundedMap(MobEffect.CODEC, MobEffectInstancePredicate.CODEC).xmap(MobEffectsPredicate::new, MobEffectsPredicate::effectMap);

    public boolean matches(Entity $$0) {
        LivingEntity $$1;
        return $$0 instanceof LivingEntity && this.matches(($$1 = (LivingEntity)$$0).getActiveEffectsMap());
    }

    public boolean matches(LivingEntity $$0) {
        return this.matches($$0.getActiveEffectsMap());
    }

    public boolean matches(Map<Holder<MobEffect>, MobEffectInstance> $$0) {
        for (Map.Entry<Holder<MobEffect>, MobEffectInstancePredicate> $$1 : this.effectMap.entrySet()) {
            MobEffectInstance $$2 = $$0.get($$1.getKey());
            if ($$1.getValue().matches($$2)) continue;
            return false;
        }
        return true;
    }

    public record MobEffectInstancePredicate(MinMaxBounds.Ints amplifier, MinMaxBounds.Ints duration, Optional<Boolean> ambient, Optional<Boolean> visible) {
        public static final Codec<MobEffectInstancePredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("amplifier", (Object)MinMaxBounds.Ints.ANY).forGetter(MobEffectInstancePredicate::amplifier), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("duration", (Object)MinMaxBounds.Ints.ANY).forGetter(MobEffectInstancePredicate::duration), (App)Codec.BOOL.optionalFieldOf("ambient").forGetter(MobEffectInstancePredicate::ambient), (App)Codec.BOOL.optionalFieldOf("visible").forGetter(MobEffectInstancePredicate::visible)).apply((Applicative)$$0, MobEffectInstancePredicate::new));

        public MobEffectInstancePredicate() {
            this(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty());
        }

        public boolean matches(@Nullable MobEffectInstance $$0) {
            if ($$0 == null) {
                return false;
            }
            if (!this.amplifier.matches($$0.getAmplifier())) {
                return false;
            }
            if (!this.duration.matches($$0.getDuration())) {
                return false;
            }
            if (this.ambient.isPresent() && this.ambient.get().booleanValue() != $$0.isAmbient()) {
                return false;
            }
            return !this.visible.isPresent() || this.visible.get().booleanValue() == $$0.isVisible();
        }
    }

    public static class Builder {
        private final ImmutableMap.Builder<Holder<MobEffect>, MobEffectInstancePredicate> effectMap = ImmutableMap.builder();

        public static Builder effects() {
            return new Builder();
        }

        public Builder and(Holder<MobEffect> $$0) {
            this.effectMap.put($$0, (Object)new MobEffectInstancePredicate());
            return this;
        }

        public Builder and(Holder<MobEffect> $$0, MobEffectInstancePredicate $$1) {
            this.effectMap.put($$0, (Object)$$1);
            return this;
        }

        public Optional<MobEffectsPredicate> build() {
            return Optional.of(new MobEffectsPredicate((Map<Holder<MobEffect>, MobEffectInstancePredicate>)this.effectMap.build()));
        }
    }
}

