package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record MultiplyValue(LevelBasedValue factor) implements EnchantmentValueEffect {
   public static final MapCodec<MultiplyValue> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(LevelBasedValue.CODEC.fieldOf("factor").forGetter(MultiplyValue::factor)).apply($$0, MultiplyValue::new)
   );

   @Override
   public float process(int $$0, RandomSource $$1, float $$2) {
      return $$2 * this.factor.calculate($$0);
   }

   @Override
   public MapCodec<MultiplyValue> codec() {
      return CODEC;
   }
}
