/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MobEffectsPredicate;
import net.minecraft.advancements.criterion.MovementPredicate;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.advancements.criterion.SlotsPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jspecify.annotations.Nullable;

public record EntityPredicate(Optional<EntityTypePredicate> entityType, Optional<DistancePredicate> distanceToPlayer, Optional<MovementPredicate> movement, LocationWrapper location, Optional<MobEffectsPredicate> effects, Optional<NbtPredicate> nbt, Optional<EntityFlagsPredicate> flags, Optional<EntityEquipmentPredicate> equipment, Optional<EntitySubPredicate> subPredicate, Optional<Integer> periodicTick, Optional<EntityPredicate> vehicle, Optional<EntityPredicate> passenger, Optional<EntityPredicate> targetedEntity, Optional<String> team, Optional<SlotsPredicate> slots, DataComponentMatchers components) {
    public static final Codec<EntityPredicate> CODEC = Codec.recursive((String)"EntityPredicate", $$0 -> RecordCodecBuilder.create($$1 -> $$1.group((App)EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(EntityPredicate::entityType), (App)DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(EntityPredicate::distanceToPlayer), (App)MovementPredicate.CODEC.optionalFieldOf("movement").forGetter(EntityPredicate::movement), (App)LocationWrapper.CODEC.forGetter(EntityPredicate::location), (App)MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(EntityPredicate::effects), (App)NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(EntityPredicate::nbt), (App)EntityFlagsPredicate.CODEC.optionalFieldOf("flags").forGetter(EntityPredicate::flags), (App)EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(EntityPredicate::equipment), (App)EntitySubPredicate.CODEC.optionalFieldOf("type_specific").forGetter(EntityPredicate::subPredicate), (App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("periodic_tick").forGetter(EntityPredicate::periodicTick), (App)$$0.optionalFieldOf("vehicle").forGetter(EntityPredicate::vehicle), (App)$$0.optionalFieldOf("passenger").forGetter(EntityPredicate::passenger), (App)$$0.optionalFieldOf("targeted_entity").forGetter(EntityPredicate::targetedEntity), (App)Codec.STRING.optionalFieldOf("team").forGetter(EntityPredicate::team), (App)SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(EntityPredicate::slots), (App)DataComponentMatchers.CODEC.forGetter(EntityPredicate::components)).apply((Applicative)$$1, EntityPredicate::new)));
    public static final Codec<ContextAwarePredicate> ADVANCEMENT_CODEC = Codec.withAlternative(ContextAwarePredicate.CODEC, CODEC, EntityPredicate::wrap);

    public static ContextAwarePredicate wrap(Builder $$0) {
        return EntityPredicate.wrap($$0.build());
    }

    public static Optional<ContextAwarePredicate> wrap(Optional<EntityPredicate> $$0) {
        return $$0.map(EntityPredicate::wrap);
    }

    public static List<ContextAwarePredicate> wrap(Builder ... $$0) {
        return Stream.of($$0).map(EntityPredicate::wrap).toList();
    }

    public static ContextAwarePredicate wrap(EntityPredicate $$0) {
        LootItemCondition $$1 = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, $$0).build();
        return new ContextAwarePredicate(List.of($$1));
    }

    public boolean matches(ServerPlayer $$0, @Nullable Entity $$1) {
        return this.matches($$0.level(), $$0.position(), $$1);
    }

    public boolean matches(ServerLevel $$0, @Nullable Vec3 $$1, @Nullable Entity $$22) {
        PlayerTeam $$7;
        if ($$22 == null) {
            return false;
        }
        if (this.entityType.isPresent() && !this.entityType.get().matches($$22.getType())) {
            return false;
        }
        if ($$1 == null ? this.distanceToPlayer.isPresent() : this.distanceToPlayer.isPresent() && !this.distanceToPlayer.get().matches($$1.x, $$1.y, $$1.z, $$22.getX(), $$22.getY(), $$22.getZ())) {
            return false;
        }
        if (this.movement.isPresent()) {
            Vec3 $$3 = $$22.getKnownMovement();
            Vec3 $$4 = $$3.scale(20.0);
            if (!this.movement.get().matches($$4.x, $$4.y, $$4.z, $$22.fallDistance)) {
                return false;
            }
        }
        if (this.location.located.isPresent() && !this.location.located.get().matches($$0, $$22.getX(), $$22.getY(), $$22.getZ())) {
            return false;
        }
        if (this.location.steppingOn.isPresent()) {
            Vec3 $$5 = Vec3.atCenterOf($$22.getOnPos());
            if (!$$22.onGround() || !this.location.steppingOn.get().matches($$0, $$5.x(), $$5.y(), $$5.z())) {
                return false;
            }
        }
        if (this.location.affectsMovement.isPresent()) {
            Vec3 $$6 = Vec3.atCenterOf($$22.getBlockPosBelowThatAffectsMyMovement());
            if (!this.location.affectsMovement.get().matches($$0, $$6.x(), $$6.y(), $$6.z())) {
                return false;
            }
        }
        if (this.effects.isPresent() && !this.effects.get().matches($$22)) {
            return false;
        }
        if (this.flags.isPresent() && !this.flags.get().matches($$22)) {
            return false;
        }
        if (this.equipment.isPresent() && !this.equipment.get().matches($$22)) {
            return false;
        }
        if (this.subPredicate.isPresent() && !this.subPredicate.get().matches($$22, $$0, $$1)) {
            return false;
        }
        if (this.vehicle.isPresent() && !this.vehicle.get().matches($$0, $$1, $$22.getVehicle())) {
            return false;
        }
        if (this.passenger.isPresent() && $$22.getPassengers().stream().noneMatch($$2 -> this.passenger.get().matches($$0, $$1, (Entity)$$2))) {
            return false;
        }
        if (this.targetedEntity.isPresent() && !this.targetedEntity.get().matches($$0, $$1, $$22 instanceof Mob ? ((Mob)$$22).getTarget() : null)) {
            return false;
        }
        if (this.periodicTick.isPresent() && $$22.tickCount % this.periodicTick.get() != 0) {
            return false;
        }
        if (this.team.isPresent() && (($$7 = $$22.getTeam()) == null || !this.team.get().equals(((Team)$$7).getName()))) {
            return false;
        }
        if (this.slots.isPresent() && !this.slots.get().matches($$22)) {
            return false;
        }
        if (!this.components.test($$22)) {
            return false;
        }
        return this.nbt.isEmpty() || this.nbt.get().matches($$22);
    }

    public static LootContext createContext(ServerPlayer $$0, Entity $$1) {
        LootParams $$2 = new LootParams.Builder($$0.level()).withParameter(LootContextParams.THIS_ENTITY, $$1).withParameter(LootContextParams.ORIGIN, $$0.position()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
        return new LootContext.Builder($$2).create(Optional.empty());
    }

    public static final class LocationWrapper
    extends Record {
        final Optional<LocationPredicate> located;
        final Optional<LocationPredicate> steppingOn;
        final Optional<LocationPredicate> affectsMovement;
        public static final MapCodec<LocationWrapper> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)LocationPredicate.CODEC.optionalFieldOf("location").forGetter(LocationWrapper::located), (App)LocationPredicate.CODEC.optionalFieldOf("stepping_on").forGetter(LocationWrapper::steppingOn), (App)LocationPredicate.CODEC.optionalFieldOf("movement_affected_by").forGetter(LocationWrapper::affectsMovement)).apply((Applicative)$$0, LocationWrapper::new));

        public LocationWrapper(Optional<LocationPredicate> $$0, Optional<LocationPredicate> $$1, Optional<LocationPredicate> $$2) {
            this.located = $$0;
            this.steppingOn = $$1;
            this.affectsMovement = $$2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LocationWrapper.class, "located;steppingOn;affectsMovement", "located", "steppingOn", "affectsMovement"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LocationWrapper.class, "located;steppingOn;affectsMovement", "located", "steppingOn", "affectsMovement"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LocationWrapper.class, "located;steppingOn;affectsMovement", "located", "steppingOn", "affectsMovement"}, this, $$0);
        }

        public Optional<LocationPredicate> located() {
            return this.located;
        }

        public Optional<LocationPredicate> steppingOn() {
            return this.steppingOn;
        }

        public Optional<LocationPredicate> affectsMovement() {
            return this.affectsMovement;
        }
    }

    public static class Builder {
        private Optional<EntityTypePredicate> entityType = Optional.empty();
        private Optional<DistancePredicate> distanceToPlayer = Optional.empty();
        private Optional<MovementPredicate> movement = Optional.empty();
        private Optional<LocationPredicate> located = Optional.empty();
        private Optional<LocationPredicate> steppingOnLocation = Optional.empty();
        private Optional<LocationPredicate> movementAffectedBy = Optional.empty();
        private Optional<MobEffectsPredicate> effects = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();
        private Optional<EntityFlagsPredicate> flags = Optional.empty();
        private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
        private Optional<EntitySubPredicate> subPredicate = Optional.empty();
        private Optional<Integer> periodicTick = Optional.empty();
        private Optional<EntityPredicate> vehicle = Optional.empty();
        private Optional<EntityPredicate> passenger = Optional.empty();
        private Optional<EntityPredicate> targetedEntity = Optional.empty();
        private Optional<String> team = Optional.empty();
        private Optional<SlotsPredicate> slots = Optional.empty();
        private DataComponentMatchers components = DataComponentMatchers.ANY;

        public static Builder entity() {
            return new Builder();
        }

        public Builder of(HolderGetter<EntityType<?>> $$0, EntityType<?> $$1) {
            this.entityType = Optional.of(EntityTypePredicate.of($$0, $$1));
            return this;
        }

        public Builder of(HolderGetter<EntityType<?>> $$0, TagKey<EntityType<?>> $$1) {
            this.entityType = Optional.of(EntityTypePredicate.of($$0, $$1));
            return this;
        }

        public Builder entityType(EntityTypePredicate $$0) {
            this.entityType = Optional.of($$0);
            return this;
        }

        public Builder distance(DistancePredicate $$0) {
            this.distanceToPlayer = Optional.of($$0);
            return this;
        }

        public Builder moving(MovementPredicate $$0) {
            this.movement = Optional.of($$0);
            return this;
        }

        public Builder located(LocationPredicate.Builder $$0) {
            this.located = Optional.of($$0.build());
            return this;
        }

        public Builder steppingOn(LocationPredicate.Builder $$0) {
            this.steppingOnLocation = Optional.of($$0.build());
            return this;
        }

        public Builder movementAffectedBy(LocationPredicate.Builder $$0) {
            this.movementAffectedBy = Optional.of($$0.build());
            return this;
        }

        public Builder effects(MobEffectsPredicate.Builder $$0) {
            this.effects = $$0.build();
            return this;
        }

        public Builder nbt(NbtPredicate $$0) {
            this.nbt = Optional.of($$0);
            return this;
        }

        public Builder flags(EntityFlagsPredicate.Builder $$0) {
            this.flags = Optional.of($$0.build());
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate.Builder $$0) {
            this.equipment = Optional.of($$0.build());
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate $$0) {
            this.equipment = Optional.of($$0);
            return this;
        }

        public Builder subPredicate(EntitySubPredicate $$0) {
            this.subPredicate = Optional.of($$0);
            return this;
        }

        public Builder periodicTick(int $$0) {
            this.periodicTick = Optional.of($$0);
            return this;
        }

        public Builder vehicle(Builder $$0) {
            this.vehicle = Optional.of($$0.build());
            return this;
        }

        public Builder passenger(Builder $$0) {
            this.passenger = Optional.of($$0.build());
            return this;
        }

        public Builder targetedEntity(Builder $$0) {
            this.targetedEntity = Optional.of($$0.build());
            return this;
        }

        public Builder team(String $$0) {
            this.team = Optional.of($$0);
            return this;
        }

        public Builder slots(SlotsPredicate $$0) {
            this.slots = Optional.of($$0);
            return this;
        }

        public Builder components(DataComponentMatchers $$0) {
            this.components = $$0;
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(this.entityType, this.distanceToPlayer, this.movement, new LocationWrapper(this.located, this.steppingOnLocation, this.movementAffectedBy), this.effects, this.nbt, this.flags, this.equipment, this.subPredicate, this.periodicTick, this.vehicle, this.passenger, this.targetedEntity, this.team, this.slots, this.components);
        }
    }
}

