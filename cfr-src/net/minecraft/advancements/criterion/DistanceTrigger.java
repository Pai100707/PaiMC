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
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, Vec3 $$1) {
        Vec3 $$2 = $$0.position();
        this.trigger($$0, (T $$3) -> $$3.matches($$0.level(), $$1, $$2));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)LocationPredicate.CODEC.optionalFieldOf("start_position").forGetter(TriggerInstance::startPosition), (App)DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(TriggerInstance::distance)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> fallFromHeight(EntityPredicate.Builder $$0, DistancePredicate $$1, LocationPredicate.Builder $$2) {
            return CriteriaTriggers.FALL_FROM_HEIGHT.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap($$0)), Optional.of($$2.build()), Optional.of($$1)));
        }

        public static Criterion<TriggerInstance> rideEntityInLava(EntityPredicate.Builder $$0, DistancePredicate $$1) {
            return CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap($$0)), Optional.empty(), Optional.of($$1)));
        }

        public static Criterion<TriggerInstance> travelledThroughNether(DistancePredicate $$0) {
            return CriteriaTriggers.NETHER_TRAVEL.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.of($$0)));
        }

        public boolean matches(ServerLevel $$0, Vec3 $$1, Vec3 $$2) {
            if (this.startPosition.isPresent() && !this.startPosition.get().matches($$0, $$1.x, $$1.y, $$1.z)) {
                return false;
            }
            return !this.distance.isPresent() || this.distance.get().matches($$1.x, $$1.y, $$1.z, $$2.x, $$2.y, $$2.z);
        }
    }
}

