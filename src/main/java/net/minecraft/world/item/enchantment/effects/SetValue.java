package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record SetValue(LevelBasedValue value) implements EnchantmentValueEffect {
   public static final MapCodec<SetValue> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(LevelBasedValue.CODEC.fieldOf("value").forGetter(SetValue::value)).apply($$0, SetValue::new)
   );

   @Override
   public float process(int $$0, RandomSource $$1, float $$2) {
      return this.value.calculate($$0);
   }

   @Override
   public MapCodec<SetValue> codec() {
      return CODEC;
   }
}
