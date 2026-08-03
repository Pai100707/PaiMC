package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance> {
   @Override
   public Codec<KilledTrigger.TriggerInstance> codec() {
      return KilledTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Entity $$1, DamageSource $$2) {
      LootContext $$3 = EntityPredicate.createContext($$0, $$1);
      this.trigger($$0, $$3x -> $$3x.matches($$0, $$3, $$2));
   }

   public record TriggerInstance(
      Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entityPredicate, Optional<DamageSourcePredicate> killingBlow
   ) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<KilledTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(KilledTrigger.TriggerInstance::player),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(KilledTrigger.TriggerInstance::entityPredicate),
               DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(KilledTrigger.TriggerInstance::killingBlow)
            )
            .apply($$0, KilledTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(Optional<EntityPredicate> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_KILLED_ENTITY
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(EntityPredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_KILLED_ENTITY
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> playerKilledEntity() {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_KILLED_ENTITY
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(
         Optional<EntityPredicate> $$0, Optional<DamageSourcePredicate> $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_KILLED_ENTITY
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), $$1));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(
         EntityPredicate.Builder $$0, Optional<DamageSourcePredicate> $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_KILLED_ENTITY
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), $$1));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(
         Optional<EntityPredicate> $$0, DamageSourcePredicate.Builder $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_KILLED_ENTITY
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), Optional.of($$1.build())));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(
         EntityPredicate.Builder $$0, DamageSourcePredicate.Builder $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_KILLED_ENTITY
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), Optional.of($$1.build())));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> playerKilledEntityNearSculkCatalyst() {
         return net.minecraft.advancements.CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.ENTITY_KILLED_PLAYER
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(EntityPredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.ENTITY_KILLED_PLAYER
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer() {
         return net.minecraft.advancements.CriteriaTriggers.ENTITY_KILLED_PLAYER
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(
         Optional<EntityPredicate> $$0, Optional<DamageSourcePredicate> $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.ENTITY_KILLED_PLAYER
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), $$1));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(
         EntityPredicate.Builder $$0, Optional<DamageSourcePredicate> $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.ENTITY_KILLED_PLAYER
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), $$1));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(
         Optional<EntityPredicate> $$0, DamageSourcePredicate.Builder $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.ENTITY_KILLED_PLAYER
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), Optional.of($$1.build())));
      }

      public static net.minecraft.advancements.Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(
         EntityPredicate.Builder $$0, DamageSourcePredicate.Builder $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.ENTITY_KILLED_PLAYER
            .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0)), Optional.of($$1.build())));
      }

      public boolean matches(ServerPlayer $$0, LootContext $$1, DamageSource $$2) {
         return this.killingBlow.isPresent() && !this.killingBlow.get().matches($$0, $$2)
            ? false
            : this.entityPredicate.isEmpty() || this.entityPredicate.get().matches($$1);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.entityPredicate, "entity");
      }
   }
}
