package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.PlayerAdvancements;

public class ImpossibleTrigger implements net.minecraft.advancements.CriterionTrigger<ImpossibleTrigger.TriggerInstance> {
   @Override
   public void addPlayerListener(PlayerAdvancements $$0, net.minecraft.advancements.CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> $$1) {
   }

   @Override
   public void removePlayerListener(PlayerAdvancements $$0, net.minecraft.advancements.CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> $$1) {
   }

   @Override
   public void removePlayerListeners(PlayerAdvancements $$0) {
   }

   @Override
   public Codec<ImpossibleTrigger.TriggerInstance> codec() {
      return ImpossibleTrigger.TriggerInstance.CODEC;
   }

   public record TriggerInstance() implements net.minecraft.advancements.CriterionTriggerInstance {
      public static final Codec<ImpossibleTrigger.TriggerInstance> CODEC = MapCodec.unitCodec(new ImpossibleTrigger.TriggerInstance());

      @Override
      public void validate(CriterionValidator $$0) {
      }
   }
}
