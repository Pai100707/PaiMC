package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.slf4j.Logger;

public class VeryBiasedToBottomHeight extends HeightProvider {
   public static final MapCodec<VeryBiasedToBottomHeight> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter($$0x -> $$0x.minInclusive),
            VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter($$0x -> $$0x.maxInclusive),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("inner", 1).forGetter($$0x -> $$0x.inner)
         )
         .apply($$0, VeryBiasedToBottomHeight::new)
   );
   private static final Logger LOGGER = LogUtils.getLogger();
   private final VerticalAnchor minInclusive;
   private final VerticalAnchor maxInclusive;
   private final int inner;

   private VeryBiasedToBottomHeight(VerticalAnchor $$0, VerticalAnchor $$1, int $$2) {
      this.minInclusive = $$0;
      this.maxInclusive = $$1;
      this.inner = $$2;
   }

   public static VeryBiasedToBottomHeight of(VerticalAnchor $$0, VerticalAnchor $$1, int $$2) {
      return new VeryBiasedToBottomHeight($$0, $$1, $$2);
   }

   @Override
   public int sample(RandomSource $$0, WorldGenerationContext $$1) {
      int $$2 = this.minInclusive.resolveY($$1);
      int $$3 = this.maxInclusive.resolveY($$1);
      if ($$3 - $$2 - this.inner + 1 <= 0) {
         LOGGER.warn("Empty height range: {}", this);
         return $$2;
      } else {
         int $$4 = Mth.nextInt($$0, $$2 + this.inner, $$3);
         int $$5 = Mth.nextInt($$0, $$2, $$4 - 1);
         return Mth.nextInt($$0, $$2, $$5 - 1 + this.inner);
      }
   }

   @Override
   public HeightProviderType<?> getType() {
      return HeightProviderType.VERY_BIASED_TO_BOTTOM;
   }

   @Override
   public String toString() {
      return "biased[" + this.minInclusive + "-" + this.maxInclusive + " inner: " + this.inner + "]";
   }
}
