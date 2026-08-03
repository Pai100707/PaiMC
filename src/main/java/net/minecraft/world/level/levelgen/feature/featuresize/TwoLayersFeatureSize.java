package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

public class TwoLayersFeatureSize extends FeatureSize {
   public static final MapCodec<TwoLayersFeatureSize> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.intRange(0, 81).fieldOf("limit").orElse(1).forGetter($$0x -> $$0x.limit),
            Codec.intRange(0, 16).fieldOf("lower_size").orElse(0).forGetter($$0x -> $$0x.lowerSize),
            Codec.intRange(0, 16).fieldOf("upper_size").orElse(1).forGetter($$0x -> $$0x.upperSize),
            minClippedHeightCodec()
         )
         .apply($$0, TwoLayersFeatureSize::new)
   );
   private final int limit;
   private final int lowerSize;
   private final int upperSize;

   public TwoLayersFeatureSize(int $$0, int $$1, int $$2) {
      this($$0, $$1, $$2, OptionalInt.empty());
   }

   public TwoLayersFeatureSize(int $$0, int $$1, int $$2, OptionalInt $$3) {
      super($$3);
      this.limit = $$0;
      this.lowerSize = $$1;
      this.upperSize = $$2;
   }

   @Override
   protected FeatureSizeType<?> type() {
      return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
   }

   @Override
   public int getSizeAtHeight(int $$0, int $$1) {
      return $$1 < this.limit ? this.lowerSize : this.upperSize;
   }
}
