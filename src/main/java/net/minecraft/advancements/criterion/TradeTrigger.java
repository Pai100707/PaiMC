package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
   @Override
   public Codec<TradeTrigger.TriggerInstance> codec() {
      return TradeTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, AbstractVillager $$1, ItemStack $$2) {
      LootContext $$3 = EntityPredicate.createContext($$0, $$1);
      this.trigger($$0, $$2x -> $$2x.matches($$3, $$2));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> villager, Optional<ItemPredicate> item)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<TradeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TradeTrigger.TriggerInstance::player),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("villager").forGetter(TradeTrigger.TriggerInstance::villager),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TradeTrigger.TriggerInstance::item)
            )
            .apply($$0, TradeTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<TradeTrigger.TriggerInstance> tradedWithVillager() {
         return net.minecraft.advancements.CriteriaTriggers.TRADE
            .createCriterion(new TradeTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<TradeTrigger.TriggerInstance> tradedWithVillager(EntityPredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.TRADE
            .createCriterion(new TradeTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap($$0)), Optional.empty(), Optional.empty()));
      }

      public boolean matches(LootContext $$0, ItemStack $$1) {
         return this.villager.isPresent() && !this.villager.get().matches($$0) ? false : !this.item.isPresent() || this.item.get().test($$1);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.villager, "villager");
      }
   }
}
