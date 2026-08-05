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
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jspecify.annotations.Nullable;

public class BredAnimalsTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, Animal $$1, Animal $$2, @Nullable AgeableMob $$32) {
        LootContext $$4 = EntityPredicate.createContext($$0, $$1);
        LootContext $$5 = EntityPredicate.createContext($$0, $$2);
        LootContext $$6 = $$32 != null ? EntityPredicate.createContext($$0, $$32) : null;
        this.trigger($$0, $$3 -> $$3.matches($$4, $$5, $$6));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> parent, Optional<ContextAwarePredicate> partner, Optional<ContextAwarePredicate> child) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("parent").forGetter(TriggerInstance::parent), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("partner").forGetter(TriggerInstance::partner), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("child").forGetter(TriggerInstance::child)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> bredAnimals() {
            return CriteriaTriggers.BRED_ANIMALS.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> bredAnimals(EntityPredicate.Builder $$0) {
            return CriteriaTriggers.BRED_ANIMALS.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap($$0))));
        }

        public static Criterion<TriggerInstance> bredAnimals(Optional<EntityPredicate> $$0, Optional<EntityPredicate> $$1, Optional<EntityPredicate> $$2) {
            return CriteriaTriggers.BRED_ANIMALS.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), EntityPredicate.wrap($$1), EntityPredicate.wrap($$2)));
        }

        public boolean matches(LootContext $$0, LootContext $$1, @Nullable LootContext $$2) {
            if (this.child.isPresent() && ($$2 == null || !this.child.get().matches($$2))) {
                return false;
            }
            return TriggerInstance.matches(this.parent, $$0) && TriggerInstance.matches(this.partner, $$1) || TriggerInstance.matches(this.parent, $$1) && TriggerInstance.matches(this.partner, $$0);
        }

        private static boolean matches(Optional<ContextAwarePredicate> $$0, LootContext $$1) {
            return $$0.isEmpty() || $$0.get().matches($$1);
        }

        @Override
        public void validate(CriterionValidator $$0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
            $$0.validateEntity(this.parent, "parent");
            $$0.validateEntity(this.partner, "partner");
            $$0.validateEntity(this.child, "child");
        }
    }
}

