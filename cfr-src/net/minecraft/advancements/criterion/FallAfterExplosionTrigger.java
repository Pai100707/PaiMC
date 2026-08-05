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
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FallAfterExplosionTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, Vec3 $$1, @Nullable Entity $$2) {
        Vec3 $$3 = $$0.position();
        LootContext $$42 = $$2 != null ? EntityPredicate.createContext($$0, $$2) : null;
        this.trigger($$0, $$4 -> $$4.matches($$0.level(), $$1, $$3, $$42));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance, Optional<ContextAwarePredicate> cause) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)LocationPredicate.CODEC.optionalFieldOf("start_position").forGetter(TriggerInstance::startPosition), (App)DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(TriggerInstance::distance), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("cause").forGetter(TriggerInstance::cause)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> fallAfterExplosion(DistancePredicate $$0, EntityPredicate.Builder $$1) {
            return CriteriaTriggers.FALL_AFTER_EXPLOSION.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of($$0), Optional.of(EntityPredicate.wrap($$1))));
        }

        @Override
        public void validate(CriterionValidator $$0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
            $$0.validateEntity(this.cause(), "cause");
        }

        public boolean matches(ServerLevel $$0, Vec3 $$1, Vec3 $$2, @Nullable LootContext $$3) {
            if (this.startPosition.isPresent() && !this.startPosition.get().matches($$0, $$1.x, $$1.y, $$1.z)) {
                return false;
            }
            if (this.distance.isPresent() && !this.distance.get().matches($$1.x, $$1.y, $$1.z, $$2.x, $$2.y, $$2.z)) {
                return false;
            }
            return !this.cause.isPresent() || $$3 != null && this.cause.get().matches($$3);
        }
    }
}

