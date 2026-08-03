package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.ProblemReporter.Collector;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record ConditionalEffect<T>(T effect, Optional<LootItemCondition> requirements) {
   public static Codec<LootItemCondition> conditionCodec(ContextKeySet $$0) {
      return LootItemCondition.DIRECT_CODEC.validate($$1 -> {
         Collector $$2 = new Collector();
         ValidationContext $$3 = new ValidationContext($$2, $$0);
         $$1.validate($$3);
         return !$$2.isEmpty() ? DataResult.error(() -> "Validation error in enchantment effect condition: " + $$2.getReport()) : DataResult.success($$1);
      });
   }

   public static <T> Codec<ConditionalEffect<T>> codec(Codec<T> $$0, ContextKeySet $$1) {
      return RecordCodecBuilder.create(
         $$2 -> $$2.group(
               $$0.fieldOf("effect").forGetter(ConditionalEffect::effect),
               conditionCodec($$1).optionalFieldOf("requirements").forGetter(ConditionalEffect::requirements)
            )
            .apply($$2, ConditionalEffect::new)
      );
   }

   public boolean matches(LootContext $$0) {
      return this.requirements.isEmpty() ? true : this.requirements.get().test($$0);
   }
}
