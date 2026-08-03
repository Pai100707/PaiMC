package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance> {
   @Override
   public Codec<PlayerInteractTrigger.TriggerInstance> codec() {
      return PlayerInteractTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ItemStack $$1, Entity $$2) {
      LootContext $$3 = EntityPredicate.createContext($$0, $$2);
      this.trigger($$0, $$2x -> $$2x.matches($$1, $$3));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<ContextAwarePredicate> entity)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<PlayerInteractTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PlayerInteractTrigger.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(PlayerInteractTrigger.TriggerInstance::item),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(PlayerInteractTrigger.TriggerInstance::entity)
            )
            .apply($$0, PlayerInteractTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<PlayerInteractTrigger.TriggerInstance> itemUsedOnEntity(
         Optional<ContextAwarePredicate> $$0, ItemPredicate.Builder $$1, Optional<ContextAwarePredicate> $$2
      ) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY
            .createCriterion(new PlayerInteractTrigger.TriggerInstance($$0, Optional.of($$1.build()), $$2));
      }

      public static net.minecraft.advancements.Criterion<PlayerInteractTrigger.TriggerInstance> equipmentSheared(
         Optional<ContextAwarePredicate> $$0, ItemPredicate.Builder $$1, Optional<ContextAwarePredicate> $$2
      ) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_SHEARED_EQUIPMENT
            .createCriterion(new PlayerInteractTrigger.TriggerInstance($$0, Optional.of($$1.build()), $$2));
      }

      public static net.minecraft.advancements.Criterion<PlayerInteractTrigger.TriggerInstance> equipmentSheared(
         ItemPredicate.Builder $$0, Optional<ContextAwarePredicate> $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.PLAYER_SHEARED_EQUIPMENT
            .createCriterion(new PlayerInteractTrigger.TriggerInstance(Optional.empty(), Optional.of($$0.build()), $$1));
      }

      public static net.minecraft.advancements.Criterion<PlayerInteractTrigger.TriggerInstance> itemUsedOnEntity(
         ItemPredicate.Builder $$0, Optional<ContextAwarePredicate> $$1
      ) {
         return itemUsedOnEntity(Optional.empty(), $$0, $$1);
      }

      public boolean matches(ItemStack $$0, LootContext $$1) {
         return this.item.isPresent() && !this.item.get().test($$0) ? false : this.entity.isEmpty() || this.entity.get().matches($$1);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.entity, "entity");
      }
   }
}
