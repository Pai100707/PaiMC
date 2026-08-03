package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentsByCost(HolderSet<Enchantment> enchantments, IntProvider cost) implements EnchantmentProvider {
   public static final MapCodec<EnchantmentsByCost> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).fieldOf("enchantments").forGetter(EnchantmentsByCost::enchantments),
            IntProvider.CODEC.fieldOf("cost").forGetter(EnchantmentsByCost::cost)
         )
         .apply($$0, EnchantmentsByCost::new)
   );

   @Override
   public void enchant(net.minecraft.world.item.ItemStack $$0, ItemEnchantments.Mutable $$1, RandomSource $$2, DifficultyInstance $$3) {
      for (EnchantmentInstance $$5 : EnchantmentHelper.selectEnchantment($$2, $$0, this.cost.sample($$2), this.enchantments.stream())) {
         $$1.upgrade($$5.enchantment(), $$5.level());
      }
   }

   @Override
   public MapCodec<EnchantmentsByCost> codec() {
      return CODEC;
   }
}
