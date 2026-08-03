package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record SingleEnchantment(Holder<Enchantment> enchantment, IntProvider level) implements EnchantmentProvider {
   public static final MapCodec<SingleEnchantment> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Enchantment.CODEC.fieldOf("enchantment").forGetter(SingleEnchantment::enchantment),
            IntProvider.CODEC.fieldOf("level").forGetter(SingleEnchantment::level)
         )
         .apply($$0, SingleEnchantment::new)
   );

   @Override
   public void enchant(net.minecraft.world.item.ItemStack $$0, ItemEnchantments.Mutable $$1, RandomSource $$2, DifficultyInstance $$3) {
      $$1.upgrade(
         this.enchantment,
         Mth.clamp(this.level.sample($$2), ((Enchantment)this.enchantment.value()).getMinLevel(), ((Enchantment)this.enchantment.value()).getMaxLevel())
      );
   }

   @Override
   public MapCodec<SingleEnchantment> codec() {
      return CODEC;
   }
}
