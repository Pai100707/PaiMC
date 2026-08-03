package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
   @Override
   public Codec<CuredZombieVillagerTrigger.TriggerInstance> codec() {
      return CuredZombieVillagerTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Zombie $$1, Villager $$2) {
      LootContext $$3 = EntityPredicate.createContext($$0, $$1);
      LootContext $$4 = EntityPredicate.createContext($$0, $$2);
      this.trigger($$0, $$2x -> $$2x.matches($$3, $$4));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> zombie, Optional<ContextAwarePredicate> villager)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<CuredZombieVillagerTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CuredZombieVillagerTrigger.TriggerInstance::player),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("zombie").forGetter(CuredZombieVillagerTrigger.TriggerInstance::zombie),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("villager").forGetter(CuredZombieVillagerTrigger.TriggerInstance::villager)
            )
            .apply($$0, CuredZombieVillagerTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<CuredZombieVillagerTrigger.TriggerInstance> curedZombieVillager() {
         return net.minecraft.advancements.CriteriaTriggers.CURED_ZOMBIE_VILLAGER
            .createCriterion(new CuredZombieVillagerTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public boolean matches(LootContext $$0, LootContext $$1) {
         return this.zombie.isPresent() && !this.zombie.get().matches($$0) ? false : !this.villager.isPresent() || this.villager.get().matches($$1);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.zombie, "zombie");
         $$0.validateEntity(this.villager, "villager");
      }
   }
}
