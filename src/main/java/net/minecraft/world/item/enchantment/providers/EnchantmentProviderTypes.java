package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;

public interface EnchantmentProviderTypes {
   static MapCodec<? extends EnchantmentProvider> bootstrap(Registry<MapCodec<? extends EnchantmentProvider>> $$0) {
      Registry.register($$0, "by_cost", EnchantmentsByCost.CODEC);
      Registry.register($$0, "by_cost_with_difficulty", EnchantmentsByCostWithDifficulty.CODEC);
      return (MapCodec<? extends EnchantmentProvider>)Registry.register($$0, "single", SingleEnchantment.CODEC);
   }
}
