package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

public class LayerConfiguration implements FeatureConfiguration {
   public static final Codec<LayerConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter($$0x -> $$0x.height),
            BlockState.CODEC.fieldOf("state").forGetter($$0x -> $$0x.state)
         )
         .apply($$0, LayerConfiguration::new)
   );
   public final int height;
   public final BlockState state;

   public LayerConfiguration(int $$0, BlockState $$1) {
      this.height = $$0;
      this.state = $$1;
   }
}
