/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
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
import net.minecraft.advancements.criterion.DamagePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerHurtEntityTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, Entity $$1, DamageSource $$2, float $$3, float $$4, boolean $$5) {
        LootContext $$62 = EntityPredicate.createContext($$0, $$1);
        this.trigger($$0, $$6 -> $$6.matches($$0, $$62, $$2, $$3, $$4, $$5));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DamagePredicate> damage, Optional<ContextAwarePredicate> entity) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)DamagePredicate.CODEC.optionalFieldOf("damage").forGetter(TriggerInstance::damage), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> playerHurtEntity() {
            return CriteriaTriggers.PLAYER_HURT_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> playerHurtEntityWithDamage(Optional<DamagePredicate> $$0) {
            return CriteriaTriggers.PLAYER_HURT_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), $$0, Optional.empty()));
        }

        public static Criterion<TriggerInstance> playerHurtEntityWithDamage(DamagePredicate.Builder $$0) {
            return CriteriaTriggers.PLAYER_HURT_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.of($$0.build()), Optional.empty()));
        }

        public static Criterion<TriggerInstance> playerHurtEntity(Optional<EntityPredicate> $$0) {
            return CriteriaTriggers.PLAYER_HURT_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), EntityPredicate.wrap($$0)));
        }

        public static Criterion<TriggerInstance> playerHurtEntity(Optional<DamagePredicate> $$0, Optional<EntityPredicate> $$1) {
            return CriteriaTriggers.PLAYER_HURT_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), $$0, EntityPredicate.wrap($$1)));
        }

        public static Criterion<TriggerInstance> playerHurtEntity(DamagePredicate.Builder $$0, Optional<EntityPredicate> $$1) {
            return CriteriaTriggers.PLAYER_HURT_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.of($$0.build()), EntityPredicate.wrap($$1)));
        }

        public boolean matches(ServerPlayer $$0, LootContext $$1, DamageSource $$2, float $$3, float $$4, boolean $$5) {
            if (this.damage.isPresent() && !this.damage.get().matches($$0, $$2, $$3, $$4, $$5)) {
                return false;
            }
            return !this.entity.isPresent() || this.entity.get().matches($$1);
        }

        @Override
        public void validate(CriterionValidator $$0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
            $$0.validateEntity(this.entity, "entity");
        }
    }
}

