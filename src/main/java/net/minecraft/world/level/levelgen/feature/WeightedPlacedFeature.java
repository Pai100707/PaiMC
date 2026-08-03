package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WeightedPlacedFeature {
   public static final Codec<WeightedPlacedFeature> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            PlacedFeature.CODEC.fieldOf("feature").forGetter($$0x -> $$0x.feature),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter($$0x -> $$0x.chance)
         )
         .apply($$0, WeightedPlacedFeature::new)
   );
   public final Holder<PlacedFeature> feature;
   public final float chance;

   public WeightedPlacedFeature(Holder<PlacedFeature> $$0, float $$1) {
      this.feature = $$0;
      this.chance = $$1;
   }

   public boolean place(net.minecraft.world.level.WorldGenLevel $$0, ChunkGenerator $$1, RandomSource $$2, BlockPos $$3) {
      return ((PlacedFeature)this.feature.value()).place($$0, $$1, $$2, $$3);
   }
}
