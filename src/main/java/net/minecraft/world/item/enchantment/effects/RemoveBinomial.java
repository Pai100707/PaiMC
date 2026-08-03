package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record RemoveBinomial(LevelBasedValue chance) implements EnchantmentValueEffect {
   public static final MapCodec<RemoveBinomial> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(LevelBasedValue.CODEC.fieldOf("chance").forGetter(RemoveBinomial::chance)).apply($$0, RemoveBinomial::new)
   );

   @Override
   public float process(int $$0, RandomSource $$1, float $$2) {
      float $$3 = this.chance.calculate($$0);
      int $$4 = 0;
      if (!($$2 <= 128.0F) && !($$2 * $$3 < 20.0F) && !($$2 * (1.0F - $$3) < 20.0F)) {
         double $$6 = Math.floor($$2 * $$3);
         double $$7 = Math.sqrt($$2 * $$3 * (1.0F - $$3));
         $$4 = (int)Math.round($$6 + $$1.nextGaussian() * $$7);
         $$4 = Math.clamp((long)$$4, 0, (int)$$2);
      } else {
         for (int $$5 = 0; $$5 < $$2; $$5++) {
            if ($$1.nextFloat() < $$3) {
               $$4++;
            }
         }
      }

      return $$2 - $$4;
   }

   @Override
   public MapCodec<RemoveBinomial> codec() {
      return CODEC;
   }
}
