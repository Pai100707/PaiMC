package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.server.PlayerAdvancements;

public interface CriterionTrigger<T extends net.minecraft.advancements.CriterionTriggerInstance> {
   void addPlayerListener(PlayerAdvancements var1, net.minecraft.advancements.CriterionTrigger.Listener<T> var2);

   void removePlayerListener(PlayerAdvancements var1, net.minecraft.advancements.CriterionTrigger.Listener<T> var2);

   void removePlayerListeners(PlayerAdvancements var1);

   Codec<T> codec();

   default net.minecraft.advancements.Criterion<T> createCriterion(T $$0) {
      return new net.minecraft.advancements.Criterion<>(this, $$0);
   }

   public record Listener<T extends net.minecraft.advancements.CriterionTriggerInstance>(
      T trigger, net.minecraft.advancements.AdvancementHolder advancement, String criterion
   ) {
      public void run(PlayerAdvancements $$0) {
         $$0.award(this.advancement, this.criterion);
      }
   }
}
