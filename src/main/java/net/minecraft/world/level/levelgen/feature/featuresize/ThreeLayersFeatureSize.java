package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

public class ThreeLayersFeatureSize extends FeatureSize {
   public static final MapCodec<ThreeLayersFeatureSize> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.intRange(0, 80).fieldOf("limit").orElse(1).forGetter($$0x -> $$0x.limit),
            Codec.intRange(0, 80).fieldOf("upper_limit").orElse(1).forGetter($$0x -> $$0x.upperLimit),
            Codec.intRange(0, 16).fieldOf("lower_size").orElse(0).forGetter($$0x -> $$0x.lowerSize),
            Codec.intRange(0, 16).fieldOf("middle_size").orElse(1).forGetter($$0x -> $$0x.middleSize),
            Codec.intRange(0, 16).fieldOf("upper_size").orElse(1).forGetter($$0x -> $$0x.upperSize),
            minClippedHeightCodec()
         )
         .apply($$0, ThreeLayersFeatureSize::new)
   );
   private final int limit;
   private final int upperLimit;
   private final int lowerSize;
   private final int middleSize;
   private final int upperSize;

   public ThreeLayersFeatureSize(int $$0, int $$1, int $$2, int $$3, int $$4, OptionalInt $$5) {
      super($$5);
      this.limit = $$0;
      this.upperLimit = $$1;
      this.lowerSize = $$2;
      this.middleSize = $$3;
      this.upperSize = $$4;
   }

   @Override
   protected FeatureSizeType<?> type() {
      return FeatureSizeType.THREE_LAYERS_FEATURE_SIZE;
   }

   @Override
   public int getSizeAtHeight(int $$0, int $$1) {
      if ($$1 < this.limit) {
         return this.lowerSize;
      } else {
         return $$1 >= $$0 - this.upperLimit ? this.upperSize : this.middleSize;
      }
   }
}
