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
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, Entity $$1, DamageSource $$2) {
        LootContext $$32 = EntityPredicate.createContext($$0, $$1);
        this.trigger($$0, $$3 -> $$3.matches($$0, $$32, $$2));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entityPredicate, Optional<DamageSourcePredicate> killingBlow) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entityPredicate), (App)DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(TriggerInstance::killingBlow)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> playerKilledEntity(Optional<EntityPredicate> $$0) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), Optional.empty()));
        }

        public static Criterion<TriggerInstance> playerKilledEntity(EntityPredicate.Builder $$0) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), Optional.empty()));
        }

        public static Criterion<TriggerInstance> playerKilledEntity() {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> playerKilledEntity(Optional<EntityPredicate> $$0, Optional<DamageSourcePredicate> $$1) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), $$1));
        }

        public static Criterion<TriggerInstance> playerKilledEntity(EntityPredicate.Builder $$0, Optional<DamageSourcePredicate> $$1) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), $$1));
        }

        public static Criterion<TriggerInstance> playerKilledEntity(Optional<EntityPredicate> $$0, DamageSourcePredicate.Builder $$1) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), Optional.of($$1.build())));
        }

        public static Criterion<TriggerInstance> playerKilledEntity(EntityPredicate.Builder $$0, DamageSourcePredicate.Builder $$1) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), Optional.of($$1.build())));
        }

        public static Criterion<TriggerInstance> playerKilledEntityNearSculkCatalyst() {
            return CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> $$0) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), Optional.empty()));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(EntityPredicate.Builder $$0) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), Optional.empty()));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer() {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> $$0, Optional<DamageSourcePredicate> $$1) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), $$1));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(EntityPredicate.Builder $$0, Optional<DamageSourcePredicate> $$1) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), $$1));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> $$0, DamageSourcePredicate.Builder $$1) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), Optional.of($$1.build())));
        }

        public static Criterion<TriggerInstance> entityKilledPlayer(EntityPredicate.Builder $$0, DamageSourcePredicate.Builder $$1) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), Optional.of($$1.build())));
        }

        public boolean matches(ServerPlayer $$0, LootContext $$1, DamageSource $$2) {
            if (this.killingBlow.isPresent() && !this.killingBlow.get().matches($$0, $$2)) {
                return false;
            }
            return this.entityPredicate.isEmpty() || this.entityPredicate.get().matches($$1);
        }

        @Override
        public void validate(CriterionValidator $$0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
            $$0.validateEntity(this.entityPredicate, "entity");
        }
    }
}

