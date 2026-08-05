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
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, Vec3 $$1, int $$2) {
        this.trigger($$0, $$3 -> $$3.matches($$0, $$1, $$2));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DistancePredicate> distance, MinMaxBounds.Ints duration) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(TriggerInstance::distance), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("duration", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::duration)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> levitated(DistancePredicate $$0) {
            return CriteriaTriggers.LEVITATION.createCriterion(new TriggerInstance(Optional.empty(), Optional.of($$0), MinMaxBounds.Ints.ANY));
        }

        public boolean matches(ServerPlayer $$0, Vec3 $$1, int $$2) {
            if (this.distance.isPresent() && !this.distance.get().matches($$1.x, $$1.y, $$1.z, $$0.getX(), $$0.getY(), $$0.getZ())) {
                return false;
            }
            return this.duration.matches($$2);
        }
    }
}

