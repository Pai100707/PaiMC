package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger extends SimpleCriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
   @Override
   public Codec<EntityHurtPlayerTrigger.TriggerInstance> codec() {
      return EntityHurtPlayerTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, DamageSource $$1, float $$2, float $$3, boolean $$4) {
      this.trigger($$0, $$5 -> $$5.matches($$0, $$1, $$2, $$3, $$4));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DamagePredicate> damage) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<EntityHurtPlayerTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(EntityHurtPlayerTrigger.TriggerInstance::player),
               DamagePredicate.CODEC.optionalFieldOf("damage").forGetter(EntityHurtPlayerTrigger.TriggerInstance::damage)
            )
            .apply($$0, EntityHurtPlayerTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer() {
         return net.minecraft.advancements.CriteriaTriggers.ENTITY_HURT_PLAYER
            .createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer(DamagePredicate $$0) {
         return net.minecraft.advancements.CriteriaTriggers.ENTITY_HURT_PLAYER
            .createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.of($$0)));
      }

      public static net.minecraft.advancements.Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer(DamagePredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.ENTITY_HURT_PLAYER
            .createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.of($$0.build())));
      }

      public boolean matches(ServerPlayer $$0, DamageSource $$1, float $$2, float $$3, boolean $$4) {
         return !this.damage.isPresent() || this.damage.get().matches($$0, $$1, $$2, $$3, $$4);
      }
   }
}
