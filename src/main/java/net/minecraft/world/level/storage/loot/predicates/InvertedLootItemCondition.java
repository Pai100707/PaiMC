package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public record InvertedLootItemCondition(LootItemCondition term) implements LootItemCondition {
   public static final MapCodec<InvertedLootItemCondition> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(LootItemCondition.DIRECT_CODEC.fieldOf("term").forGetter(InvertedLootItemCondition::term)).apply($$0, InvertedLootItemCondition::new)
   );

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.INVERTED;
   }

   public boolean test(LootContext $$0) {
      return !this.term.test($$0);
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.term.getReferencedContextParams();
   }

   @Override
   public void validate(ValidationContext $$0) {
      LootItemCondition.super.validate($$0);
      this.term.validate($$0);
   }

   public static LootItemCondition.Builder invert(LootItemCondition.Builder $$0) {
      InvertedLootItemCondition $$1 = new InvertedLootItemCondition($$0.build());
      return () -> $$1;
   }
}
