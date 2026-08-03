package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.jspecify.annotations.Nullable;

public record EntityPredicate(
   Optional<EntityTypePredicate> entityType,
   Optional<DistancePredicate> distanceToPlayer,
   Optional<MovementPredicate> movement,
   EntityPredicate.LocationWrapper location,
   Optional<MobEffectsPredicate> effects,
   Optional<NbtPredicate> nbt,
   Optional<EntityFlagsPredicate> flags,
   Optional<EntityEquipmentPredicate> equipment,
   Optional<EntitySubPredicate> subPredicate,
   Optional<Integer> periodicTick,
   Optional<EntityPredicate> vehicle,
   Optional<EntityPredicate> passenger,
   Optional<EntityPredicate> targetedEntity,
   Optional<String> team,
   Optional<SlotsPredicate> slots,
   DataComponentMatchers components
) {
   public static final Codec<EntityPredicate> CODEC = Codec.recursive(
      "EntityPredicate",
      $$0 -> RecordCodecBuilder.create(
         $$1 -> $$1.group(
               EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(EntityPredicate::entityType),
               DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(EntityPredicate::distanceToPlayer),
               MovementPredicate.CODEC.optionalFieldOf("movement").forGetter(EntityPredicate::movement),
               EntityPredicate.LocationWrapper.CODEC.forGetter(EntityPredicate::location),
               MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(EntityPredicate::effects),
               NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(EntityPredicate::nbt),
               EntityFlagsPredicate.CODEC.optionalFieldOf("flags").forGetter(EntityPredicate::flags),
               EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(EntityPredicate::equipment),
               EntitySubPredicate.CODEC.optionalFieldOf("type_specific").forGetter(EntityPredicate::subPredicate),
               ExtraCodecs.POSITIVE_INT.optionalFieldOf("periodic_tick").forGetter(EntityPredicate::periodicTick),
               $$0.optionalFieldOf("vehicle").forGetter(EntityPredicate::vehicle),
               $$0.optionalFieldOf("passenger").forGetter(EntityPredicate::passenger),
               $$0.optionalFieldOf("targeted_entity").forGetter(EntityPredicate::targetedEntity),
               Codec.STRING.optionalFieldOf("team").forGetter(EntityPredicate::team),
               SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(EntityPredicate::slots),
               DataComponentMatchers.CODEC.forGetter(EntityPredicate::components)
            )
            .apply($$1, EntityPredicate::new)
      )
   );
   public static final Codec<ContextAwarePredicate> ADVANCEMENT_CODEC = Codec.withAlternative(ContextAwarePredicate.CODEC, CODEC, EntityPredicate::wrap);

   public static ContextAwarePredicate wrap(EntityPredicate.Builder $$0) {
      return wrap($$0.build());
   }

   public static Optional<ContextAwarePredicate> wrap(Optional<EntityPredicate> $$0) {
      return $$0.map(EntityPredicate::wrap);
   }

   public static List<ContextAwarePredicate> wrap(EntityPredicate.Builder... $$0) {
      return Stream.of($$0).map(EntityPredicate::wrap).toList();
   }

   public static ContextAwarePredicate wrap(EntityPredicate $$0) {
      LootItemCondition $$1 = LootItemEntityPropertyCondition.hasProperties(EntityTarget.THIS, $$0).build();
      return new ContextAwarePredicate(List.of($$1));
   }

   public boolean matches(ServerPlayer $$0, @Nullable Entity $$1) {
      return this.matches($$0.level(), $$0.position(), $$1);
   }

   public boolean matches(ServerLevel $$0, @Nullable Vec3 $$1, @Nullable Entity $$2) {
      if ($$2 == null) {
         return false;
      } else if (this.entityType.isPresent() && !this.entityType.get().matches($$2.getType())) {
         return false;
      } else {
         if ($$1 == null) {
            if (this.distanceToPlayer.isPresent()) {
               return false;
            }
         } else if (this.distanceToPlayer.isPresent() && !this.distanceToPlayer.get().matches($$1.x, $$1.y, $$1.z, $$2.getX(), $$2.getY(), $$2.getZ())) {
            return false;
         }

         if (this.movement.isPresent()) {
            Vec3 $$3 = $$2.getKnownMovement();
            Vec3 $$4 = $$3.scale(20.0);
            if (!this.movement.get().matches($$4.x, $$4.y, $$4.z, $$2.fallDistance)) {
               return false;
            }
         }

         if (this.location.located.isPresent() && !this.location.located.get().matches($$0, $$2.getX(), $$2.getY(), $$2.getZ())) {
            return false;
         } else {
            if (this.location.steppingOn.isPresent()) {
               Vec3 $$5 = Vec3.atCenterOf($$2.getOnPos());
               if (!$$2.onGround() || !this.location.steppingOn.get().matches($$0, $$5.x(), $$5.y(), $$5.z())) {
                  return false;
               }
            }

            if (this.location.affectsMovement.isPresent()) {
               Vec3 $$6 = Vec3.atCenterOf($$2.getBlockPosBelowThatAffectsMyMovement());
               if (!this.location.affectsMovement.get().matches($$0, $$6.x(), $$6.y(), $$6.z())) {
                  return false;
               }
            }

            if (this.effects.isPresent() && !this.effects.get().matches($$2)) {
               return false;
            } else if (this.flags.isPresent() && !this.flags.get().matches($$2)) {
               return false;
            } else if (this.equipment.isPresent() && !this.equipment.get().matches($$2)) {
               return false;
            } else if (this.subPredicate.isPresent() && !this.subPredicate.get().matches($$2, $$0, $$1)) {
               return false;
            } else if (this.vehicle.isPresent() && !this.vehicle.get().matches($$0, $$1, $$2.getVehicle())) {
               return false;
            } else if (this.passenger.isPresent() && $$2.getPassengers().stream().noneMatch($$2x -> this.passenger.get().matches($$0, $$1, $$2x))) {
               return false;
            } else if (this.targetedEntity.isPresent() && !this.targetedEntity.get().matches($$0, $$1, $$2 instanceof Mob ? ((Mob)$$2).getTarget() : null)) {
               return false;
            } else if (this.periodicTick.isPresent() && $$2.tickCount % this.periodicTick.get() != 0) {
               return false;
            } else {
               if (this.team.isPresent()) {
                  Team $$7 = $$2.getTeam();
                  if ($$7 == null || !this.team.get().equals($$7.getName())) {
                     return false;
                  }
               }

               if (this.slots.isPresent() && !this.slots.get().matches($$2)) {
                  return false;
               } else {
                  return !this.components.test((DataComponentGetter)$$2) ? false : this.nbt.isEmpty() || this.nbt.get().matches($$2);
               }
            }
         }
      }
   }

   public static LootContext createContext(ServerPlayer $$0, Entity $$1) {
      LootParams $$2 = new net.minecraft.world.level.storage.loot.LootParams.Builder($$0.level())
         .withParameter(LootContextParams.THIS_ENTITY, $$1)
         .withParameter(LootContextParams.ORIGIN, $$0.position())
         .create(LootContextParamSets.ADVANCEMENT_ENTITY);
      return new net.minecraft.world.level.storage.loot.LootContext.Builder($$2).create(Optional.empty());
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

      public static EntityPredicate.Builder entity() {
         return new EntityPredicate.Builder();
      }

      public EntityPredicate.Builder of(HolderGetter<EntityType<?>> $$0, EntityType<?> $$1) {
         this.entityType = Optional.of(EntityTypePredicate.of($$0, $$1));
         return this;
      }

      public EntityPredicate.Builder of(HolderGetter<EntityType<?>> $$0, TagKey<EntityType<?>> $$1) {
         this.entityType = Optional.of(EntityTypePredicate.of($$0, $$1));
         return this;
      }

      public EntityPredicate.Builder entityType(EntityTypePredicate $$0) {
         this.entityType = Optional.of($$0);
         return this;
      }

      public EntityPredicate.Builder distance(DistancePredicate $$0) {
         this.distanceToPlayer = Optional.of($$0);
         return this;
      }

      public EntityPredicate.Builder moving(MovementPredicate $$0) {
         this.movement = Optional.of($$0);
         return this;
      }

      public EntityPredicate.Builder located(LocationPredicate.Builder $$0) {
         this.located = Optional.of($$0.build());
         return this;
      }

      public EntityPredicate.Builder steppingOn(LocationPredicate.Builder $$0) {
         this.steppingOnLocation = Optional.of($$0.build());
         return this;
      }

      public EntityPredicate.Builder movementAffectedBy(LocationPredicate.Builder $$0) {
         this.movementAffectedBy = Optional.of($$0.build());
         return this;
      }

      public EntityPredicate.Builder effects(MobEffectsPredicate.Builder $$0) {
         this.effects = $$0.build();
         return this;
      }

      public EntityPredicate.Builder nbt(NbtPredicate $$0) {
         this.nbt = Optional.of($$0);
         return this;
      }

      public EntityPredicate.Builder flags(EntityFlagsPredicate.Builder $$0) {
         this.flags = Optional.of($$0.build());
         return this;
      }

      public EntityPredicate.Builder equipment(EntityEquipmentPredicate.Builder $$0) {
         this.equipment = Optional.of($$0.build());
         return this;
      }

      public EntityPredicate.Builder equipment(EntityEquipmentPredicate $$0) {
         this.equipment = Optional.of($$0);
         return this;
      }

      public EntityPredicate.Builder subPredicate(EntitySubPredicate $$0) {
         this.subPredicate = Optional.of($$0);
         return this;
      }

      public EntityPredicate.Builder periodicTick(int $$0) {
         this.periodicTick = Optional.of($$0);
         return this;
      }

      public EntityPredicate.Builder vehicle(EntityPredicate.Builder $$0) {
         this.vehicle = Optional.of($$0.build());
         return this;
      }

      public EntityPredicate.Builder passenger(EntityPredicate.Builder $$0) {
         this.passenger = Optional.of($$0.build());
         return this;
      }

      public EntityPredicate.Builder targetedEntity(EntityPredicate.Builder $$0) {
         this.targetedEntity = Optional.of($$0.build());
         return this;
      }

      public EntityPredicate.Builder team(String $$0) {
         this.team = Optional.of($$0);
         return this;
      }

      public EntityPredicate.Builder slots(SlotsPredicate $$0) {
         this.slots = Optional.of($$0);
         return this;
      }

      public EntityPredicate.Builder components(DataComponentMatchers $$0) {
         this.components = $$0;
         return this;
      }

      public EntityPredicate build() {
         return new EntityPredicate(
            this.entityType,
            this.distanceToPlayer,
            this.movement,
            new EntityPredicate.LocationWrapper(this.located, this.steppingOnLocation, this.movementAffectedBy),
            this.effects,
            this.nbt,
            this.flags,
            this.equipment,
            this.subPredicate,
            this.periodicTick,
            this.vehicle,
            this.passenger,
            this.targetedEntity,
            this.team,
            this.slots,
            this.components
         );
      }
   }

   public record LocationWrapper(Optional<LocationPredicate> located, Optional<LocationPredicate> steppingOn, Optional<LocationPredicate> affectsMovement) {
      public static final MapCodec<EntityPredicate.LocationWrapper> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               LocationPredicate.CODEC.optionalFieldOf("location").forGetter(EntityPredicate.LocationWrapper::located),
               LocationPredicate.CODEC.optionalFieldOf("stepping_on").forGetter(EntityPredicate.LocationWrapper::steppingOn),
               LocationPredicate.CODEC.optionalFieldOf("movement_affected_by").forGetter(EntityPredicate.LocationWrapper::affectsMovement)
            )
            .apply($$0, EntityPredicate.LocationWrapper::new)
      );
   }
}
