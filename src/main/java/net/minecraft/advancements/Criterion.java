package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;

public record Criterion<T extends net.minecraft.advancements.CriterionTriggerInstance>(net.minecraft.advancements.CriterionTrigger<T> trigger, T triggerInstance) {
   private static final MapCodec<net.minecraft.advancements.Criterion<?>> MAP_CODEC = ExtraCodecs.dispatchOptionalValue(
      "trigger",
      "conditions",
      net.minecraft.advancements.CriteriaTriggers.CODEC,
      net.minecraft.advancements.Criterion::trigger,
      net.minecraft.advancements.Criterion::criterionCodec
   );
   public static final Codec<net.minecraft.advancements.Criterion<?>> CODEC = MAP_CODEC.codec();

   private static <T extends net.minecraft.advancements.CriterionTriggerInstance> Codec<net.minecraft.advancements.Criterion<T>> criterionCodec(
      net.minecraft.advancements.CriterionTrigger<T> $$0
   ) {
      return $$0.codec().xmap($$1 -> new net.minecraft.advancements.Criterion<>($$0, (T)$$1), net.minecraft.advancements.Criterion::triggerInstance);
   }
}
