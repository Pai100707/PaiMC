package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
   @Override
   public Codec<PlayerHurtEntityTrigger.TriggerInstance> codec() {
      return PlayerHurtEntityTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Entity $$1, DamageSource $$2, float $$3, float $$4, boolean $$5) {
      LootContext $$6 = EntityPredicate.createContext($$0, $$1);
      this.trigger($$0, $$6x -> $$6x.matches($$0, $$6, $$2, $$3, $$4, $$5));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DamagePredicate> damage, Optional<ContextAwarePredicate> entity)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<PlayerHurtEntityTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PlayerHurtEntityTrigger.TriggerInstance::player),
               DamagePredicate.CODEC.optionalFieldOf("damage").forGetter(PlayerHurtEntityTrigger.TriggerInstance::damage),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(PlayerHurtEntityTrigger.TriggerInstance::entity)
            )
            .apply($$0, PlayerHurtEntityTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntity() {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_HURT_ENTITY
            .createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntityWithDamage(Optional<DamagePredicate> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_HURT_ENTITY
            .createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), $$0, Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntityWithDamage(DamagePredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_HURT_ENTITY
            .createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.of($$0.build()), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntity(Optional<EntityPredicate> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_HURT_ENTITY
            .createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.empty(), EntityPredicate.wrap($$0)));
      }

      public static net.minecraft.advancements.Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntity(
         Optional<DamagePredicate> $$0, Optional<EntityPredicate> $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_HURT_ENTITY
            .createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), $$0, EntityPredicate.wrap($$1)));
      }

      public static net.minecraft.advancements.Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntity(
         DamagePredicate.Builder $$0, Optional<EntityPredicate> $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_HURT_ENTITY
            .createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.of($$0.build()), EntityPredicate.wrap($$1)));
      }

      public boolean matches(ServerPlayer $$0, LootContext $$1, DamageSource $$2, float $$3, float $$4, boolean $$5) {
         return this.damage.isPresent() && !this.damage.get().matches($$0, $$2, $$3, $$4, $$5)
            ? false
            : !this.entity.isPresent() || this.entity.get().matches($$1);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.entity, "entity");
      }
   }
}
