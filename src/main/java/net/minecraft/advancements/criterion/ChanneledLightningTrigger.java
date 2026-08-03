package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
   @Override
   public Codec<ChanneledLightningTrigger.TriggerInstance> codec() {
      return ChanneledLightningTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Collection<? extends Entity> $$1) {
      List<LootContext> $$2 = $$1.stream().map($$1x -> EntityPredicate.createContext($$0, $$1x)).collect(Collectors.toList());
      this.trigger($$0, $$1x -> $$1x.matches($$2));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<ChanneledLightningTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ChanneledLightningTrigger.TriggerInstance::player),
               EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(ChanneledLightningTrigger.TriggerInstance::victims)
            )
            .apply($$0, ChanneledLightningTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<ChanneledLightningTrigger.TriggerInstance> channeledLightning(EntityPredicate.Builder... $$0) {
         return net.minecraft.advancements.CriteriaTriggers.CHANNELED_LIGHTNING
            .createCriterion(new ChanneledLightningTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0)));
      }

      public boolean matches(Collection<? extends LootContext> $$0) {
         for (ContextAwarePredicate $$1 : this.victims) {
            boolean $$2 = false;

            for (LootContext $$3 : $$0) {
               if ($$1.matches($$3)) {
                  $$2 = true;
                  break;
               }
            }

            if (!$$2) {
               return false;
            }
         }

         return true;
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntities(this.victims, "victims");
      }
   }
}
