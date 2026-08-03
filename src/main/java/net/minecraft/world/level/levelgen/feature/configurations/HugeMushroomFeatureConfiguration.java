package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class HugeMushroomFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<HugeMushroomFeatureConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BlockStateProvider.CODEC.fieldOf("cap_provider").forGetter($$0x -> $$0x.capProvider),
            BlockStateProvider.CODEC.fieldOf("stem_provider").forGetter($$0x -> $$0x.stemProvider),
            Codec.INT.fieldOf("foliage_radius").orElse(2).forGetter($$0x -> $$0x.foliageRadius)
         )
         .apply($$0, HugeMushroomFeatureConfiguration::new)
   );
   public final BlockStateProvider capProvider;
   public final BlockStateProvider stemProvider;
   public final int foliageRadius;

   public HugeMushroomFeatureConfiguration(BlockStateProvider $$0, BlockStateProvider $$1, int $$2) {
      this.capProvider = $$0;
      this.stemProvider = $$1;
      this.foliageRadius = $$2;
   }
}
