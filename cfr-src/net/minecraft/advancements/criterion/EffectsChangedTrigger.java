/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.MobEffectsPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jspecify.annotations.Nullable;

public class EffectsChangedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, @Nullable Entity $$1) {
        LootContext $$22 = $$1 != null ? EntityPredicate.createContext($$0, $$1) : null;
        this.trigger($$0, (T $$2) -> $$2.matches($$0, $$22));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<MobEffectsPredicate> effects, Optional<ContextAwarePredicate> source) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(TriggerInstance::effects), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("source").forGetter(TriggerInstance::source)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> hasEffects(MobEffectsPredicate.Builder $$0) {
            return CriteriaTriggers.EFFECTS_CHANGED.createCriterion(new TriggerInstance(Optional.empty(), $$0.build(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> gotEffectsFrom(EntityPredicate.Builder $$0) {
            return CriteriaTriggers.EFFECTS_CHANGED.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap($$0.build()))));
        }

        public boolean matches(ServerPlayer $$0, @Nullable LootContext $$1) {
            if (this.effects.isPresent() && !this.effects.get().matches($$0)) {
                return false;
            }
            return !this.source.isPresent() || $$1 != null && this.source.get().matches($$1);
        }

        @Override
        public void validate(CriterionValidator $$0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
            $$0.validateEntity(this.source, "source");
        }
    }
}

