package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record EnchantmentLevelProvider(LevelBasedValue amount) implements NumberProvider {
   public static final MapCodec<EnchantmentLevelProvider> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter(EnchantmentLevelProvider::amount)).apply($$0, EnchantmentLevelProvider::new)
   );

   @Override
   public float getFloat(LootContext $$0) {
      int $$1 = $$0.<Integer>getParameter(LootContextParams.ENCHANTMENT_LEVEL);
      return this.amount.calculate($$1);
   }

   @Override
   public LootNumberProviderType getType() {
      return NumberProviders.ENCHANTMENT_LEVEL;
   }

   public static EnchantmentLevelProvider forEnchantmentLevel(LevelBasedValue $$0) {
      return new EnchantmentLevelProvider($$0);
   }
}
