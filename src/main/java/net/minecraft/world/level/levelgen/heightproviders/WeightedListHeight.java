package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class WeightedListHeight extends HeightProvider {
   public static final MapCodec<WeightedListHeight> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(WeightedList.nonEmptyCodec(HeightProvider.CODEC).fieldOf("distribution").forGetter($$0x -> $$0x.distribution))
         .apply($$0, WeightedListHeight::new)
   );
   private final WeightedList<HeightProvider> distribution;

   public WeightedListHeight(WeightedList<HeightProvider> $$0) {
      this.distribution = $$0;
   }

   @Override
   public int sample(RandomSource $$0, WorldGenerationContext $$1) {
      return ((HeightProvider)this.distribution.getRandomOrThrow($$0)).sample($$0, $$1);
   }

   @Override
   public HeightProviderType<?> getType() {
      return HeightProviderType.WEIGHTED_LIST;
   }
}
