package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class FishingRodHookedTrigger extends SimpleCriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
   @Override
   public Codec<FishingRodHookedTrigger.TriggerInstance> codec() {
      return FishingRodHookedTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ItemStack $$1, FishingHook $$2, Collection<ItemStack> $$3) {
      LootContext $$4 = EntityPredicate.createContext($$0, (Entity)($$2.getHookedIn() != null ? $$2.getHookedIn() : $$2));
      this.trigger($$0, $$3x -> $$3x.matches($$1, $$4, $$3));
   }

   public record TriggerInstance(
      Optional<ContextAwarePredicate> player, Optional<ItemPredicate> rod, Optional<ContextAwarePredicate> entity, Optional<ItemPredicate> item
   ) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<FishingRodHookedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(FishingRodHookedTrigger.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("rod").forGetter(FishingRodHookedTrigger.TriggerInstance::rod),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(FishingRodHookedTrigger.TriggerInstance::entity),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(FishingRodHookedTrigger.TriggerInstance::item)
            )
            .apply($$0, FishingRodHookedTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<FishingRodHookedTrigger.TriggerInstance> fishedItem(
         Optional<ItemPredicate> $$0, Optional<EntityPredicate> $$1, Optional<ItemPredicate> $$2
      ) {
         return net.minecraft.advancements.CriteriaTriggers.FISHING_ROD_HOOKED
            .createCriterion(new FishingRodHookedTrigger.TriggerInstance(Optional.empty(), $$0, EntityPredicate.wrap($$1), $$2));
      }

      public boolean matches(ItemStack $$0, LootContext $$1, Collection<ItemStack> $$2) {
         if (this.rod.isPresent() && !this.rod.get().test($$0)) {
            return false;
         } else if (this.entity.isPresent() && !this.entity.get().matches($$1)) {
            return false;
         } else {
            if (this.item.isPresent()) {
               boolean $$3 = false;
               Entity $$4 = (Entity)$$1.getOptionalParameter(LootContextParams.THIS_ENTITY);
               if ($$4 instanceof ItemEntity $$5 && this.item.get().test($$5.getItem())) {
                  $$3 = true;
               }

               for (ItemStack $$6 : $$2) {
                  if (this.item.get().test($$6)) {
                     $$3 = true;
                     break;
                  }
               }

               if (!$$3) {
                  return false;
               }
            }

            return true;
         }
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.entity, "entity");
      }
   }
}
