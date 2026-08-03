package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record ScaleExponentially(LevelBasedValue base, LevelBasedValue exponent) implements EnchantmentValueEffect {
   public static final MapCodec<ScaleExponentially> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            LevelBasedValue.CODEC.fieldOf("base").forGetter(ScaleExponentially::base),
            LevelBasedValue.CODEC.fieldOf("exponent").forGetter(ScaleExponentially::exponent)
         )
         .apply($$0, ScaleExponentially::new)
   );

   @Override
   public float process(int $$0, RandomSource $$1, float $$2) {
      return (float)($$2 * Math.pow(this.base.calculate($$0), this.exponent.calculate($$0)));
   }

   @Override
   public MapCodec<ScaleExponentially> codec() {
      return CODEC;
   }
}
