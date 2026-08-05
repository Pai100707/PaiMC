package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PickedUpItemTrigger extends SimpleCriterionTrigger<PickedUpItemTrigger.TriggerInstance> {
   @Override
   public Codec<PickedUpItemTrigger.TriggerInstance> codec() {
      return PickedUpItemTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ItemStack $$1, Entity $$2) {
      LootContext $$3 = EntityPredicate.createContext($$0, $$2);
      this.trigger($$0, $$3x -> $$3x.matches($$0, $$1, $$3));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<ContextAwarePredicate> entity)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<PickedUpItemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PickedUpItemTrigger.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(PickedUpItemTrigger.TriggerInstance::item),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(PickedUpItemTrigger.TriggerInstance::entity)
            )
            .apply($$0, PickedUpItemTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<PickedUpItemTrigger.TriggerInstance> thrownItemPickedUpByEntity(
         ContextAwarePredicate $$0, Optional<ItemPredicate> $$1, Optional<ContextAwarePredicate> $$2
      ) {
         return net.minecraft.advancements.CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY
            .createCriterion(new PickedUpItemTrigger.TriggerInstance(Optional.of($$0), $$1, $$2));
      }

      public static net.minecraft.advancements.Criterion<PickedUpItemTrigger.TriggerInstance> thrownItemPickedUpByPlayer(
         Optional<ContextAwarePredicate> $$0, Optional<ItemPredicate> $$1, Optional<ContextAwarePredicate> $$2
      ) {
         return net.minecraft.advancements.CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER
            .createCriterion(new PickedUpItemTrigger.TriggerInstance($$0, $$1, $$2));
      }

      public boolean matches(ServerPlayer $$0, ItemStack $$1, LootContext $$2) {
         return this.item.isPresent() && !this.item.get().test($$1) ? false : !this.entity.isPresent() || this.entity.get().matches($$2);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.entity, "entity");
      }
   }
}
