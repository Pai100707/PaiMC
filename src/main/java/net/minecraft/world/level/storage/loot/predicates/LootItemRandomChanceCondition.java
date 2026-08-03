package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record LootItemRandomChanceCondition(NumberProvider chance) implements LootItemCondition {
   public static final MapCodec<LootItemRandomChanceCondition> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(NumberProviders.CODEC.fieldOf("chance").forGetter(LootItemRandomChanceCondition::chance)).apply($$0, LootItemRandomChanceCondition::new)
   );

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.RANDOM_CHANCE;
   }

   public boolean test(LootContext $$0) {
      float $$1 = this.chance.getFloat($$0);
      return $$0.getRandom().nextFloat() < $$1;
   }

   public static LootItemCondition.Builder randomChance(float $$0) {
      return () -> new LootItemRandomChanceCondition(ConstantValue.exactly($$0));
   }

   public static LootItemCondition.Builder randomChance(NumberProvider $$0) {
      return () -> new LootItemRandomChanceCondition($$0);
   }
}
