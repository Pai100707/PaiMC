package net.minecraft.util.valueproviders;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

public class WeightedListInt extends IntProvider {
   public static final MapCodec<WeightedListInt> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(WeightedList.nonEmptyCodec(IntProvider.CODEC).fieldOf("distribution").forGetter($$0x -> $$0x.distribution))
         .apply($$0, WeightedListInt::new)
   );
   private final WeightedList<IntProvider> distribution;
   private final int minValue;
   private final int maxValue;

   public WeightedListInt(WeightedList<IntProvider> $$0) {
      this.distribution = $$0;
      int $$1 = Integer.MAX_VALUE;
      int $$2 = Integer.MIN_VALUE;

      for (Weighted<IntProvider> $$3 : $$0.unwrap()) {
         int $$4 = $$3.value().getMinValue();
         int $$5 = $$3.value().getMaxValue();
         $$1 = Math.min($$1, $$4);
         $$2 = Math.max($$2, $$5);
      }

      this.minValue = $$1;
      this.maxValue = $$2;
   }

   @Override
   public int sample(net.minecraft.util.RandomSource $$0) {
      return this.distribution.getRandomOrThrow($$0).sample($$0);
   }

   @Override
   public int getMinValue() {
      return this.minValue;
   }

   @Override
   public int getMaxValue() {
      return this.maxValue;
   }

   @Override
   public IntProviderType<?> getType() {
      return IntProviderType.WEIGHTED_LIST;
   }
}
