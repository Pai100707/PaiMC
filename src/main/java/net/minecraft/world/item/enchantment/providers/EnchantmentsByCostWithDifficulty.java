package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentsByCostWithDifficulty(HolderSet<Enchantment> enchantments, int minCost, int maxCostSpan) implements EnchantmentProvider {
   public static final int MAX_ALLOWED_VALUE_PART = 10000;
   public static final MapCodec<EnchantmentsByCostWithDifficulty> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).fieldOf("enchantments").forGetter(EnchantmentsByCostWithDifficulty::enchantments),
            ExtraCodecs.intRange(1, 10000).fieldOf("min_cost").forGetter(EnchantmentsByCostWithDifficulty::minCost),
            ExtraCodecs.intRange(0, 10000).fieldOf("max_cost_span").forGetter(EnchantmentsByCostWithDifficulty::maxCostSpan)
         )
         .apply($$0, EnchantmentsByCostWithDifficulty::new)
   );

   @Override
   public void enchant(net.minecraft.world.item.ItemStack $$0, ItemEnchantments.Mutable $$1, RandomSource $$2, DifficultyInstance $$3) {
      float $$4 = $$3.getSpecialMultiplier();
      int $$5 = Mth.randomBetweenInclusive($$2, this.minCost, this.minCost + (int)($$4 * this.maxCostSpan));

      for (EnchantmentInstance $$7 : EnchantmentHelper.selectEnchantment($$2, $$0, $$5, this.enchantments.stream())) {
         $$1.upgrade($$7.enchantment(), $$7.level());
      }
   }

   @Override
   public MapCodec<EnchantmentsByCostWithDifficulty> codec() {
      return CODEC;
   }
}
