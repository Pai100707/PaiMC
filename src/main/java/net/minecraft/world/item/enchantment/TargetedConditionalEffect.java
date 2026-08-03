package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record TargetedConditionalEffect<T>(EnchantmentTarget enchanted, EnchantmentTarget affected, T effect, Optional<LootItemCondition> requirements) {
   public static <S> Codec<TargetedConditionalEffect<S>> codec(Codec<S> $$0, ContextKeySet $$1) {
      return RecordCodecBuilder.create(
         $$2 -> $$2.group(
               EnchantmentTarget.CODEC.fieldOf("enchanted").forGetter(TargetedConditionalEffect::enchanted),
               EnchantmentTarget.CODEC.fieldOf("affected").forGetter(TargetedConditionalEffect::affected),
               $$0.fieldOf("effect").forGetter(TargetedConditionalEffect::effect),
               ConditionalEffect.conditionCodec($$1).optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)
            )
            .apply($$2, TargetedConditionalEffect::new)
      );
   }

   public static <S> Codec<TargetedConditionalEffect<S>> equipmentDropsCodec(Codec<S> $$0, ContextKeySet $$1) {
      return RecordCodecBuilder.create(
         $$2 -> $$2.group(
               EnchantmentTarget.CODEC
                  .validate(
                     $$0xx -> $$0xx != EnchantmentTarget.DAMAGING_ENTITY
                        ? DataResult.success($$0xx)
                        : DataResult.error(() -> "enchanted must be attacker or victim")
                  )
                  .fieldOf("enchanted")
                  .forGetter(TargetedConditionalEffect::enchanted),
               $$0.fieldOf("effect").forGetter(TargetedConditionalEffect::effect),
               ConditionalEffect.conditionCodec($$1).optionalFieldOf("requirements").forGetter(TargetedConditionalEffect::requirements)
            )
            .apply($$2, ($$0xx, $$1xx, $$2x) -> new TargetedConditionalEffect<>($$0xx, EnchantmentTarget.VICTIM, $$1xx, $$2x))
      );
   }

   public boolean matches(LootContext $$0) {
      return this.requirements.isEmpty() ? true : this.requirements.get().test($$0);
   }
}
