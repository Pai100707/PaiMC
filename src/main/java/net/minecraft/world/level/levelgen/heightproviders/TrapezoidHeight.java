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

public class TrapezoidHeight extends HeightProvider {
   public static final MapCodec<TrapezoidHeight> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter($$0x -> $$0x.minInclusive),
            VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter($$0x -> $$0x.maxInclusive),
            Codec.INT.optionalFieldOf("plateau", 0).forGetter($$0x -> $$0x.plateau)
         )
         .apply($$0, TrapezoidHeight::new)
   );
   private static final Logger LOGGER = LogUtils.getLogger();
   private final VerticalAnchor minInclusive;
   private final VerticalAnchor maxInclusive;
   private final int plateau;

   private TrapezoidHeight(VerticalAnchor $$0, VerticalAnchor $$1, int $$2) {
      this.minInclusive = $$0;
      this.maxInclusive = $$1;
      this.plateau = $$2;
   }

   public static TrapezoidHeight of(VerticalAnchor $$0, VerticalAnchor $$1, int $$2) {
      return new TrapezoidHeight($$0, $$1, $$2);
   }

   public static TrapezoidHeight of(VerticalAnchor $$0, VerticalAnchor $$1) {
      return of($$0, $$1, 0);
   }

   @Override
   public int sample(RandomSource $$0, WorldGenerationContext $$1) {
      int $$2 = this.minInclusive.resolveY($$1);
      int $$3 = this.maxInclusive.resolveY($$1);
      if ($$2 > $$3) {
         LOGGER.warn("Empty height range: {}", this);
         return $$2;
      } else {
         int $$4 = $$3 - $$2;
         if (this.plateau >= $$4) {
            return Mth.randomBetweenInclusive($$0, $$2, $$3);
         } else {
            int $$5 = ($$4 - this.plateau) / 2;
            int $$6 = $$4 - $$5;
            return $$2 + Mth.randomBetweenInclusive($$0, 0, $$6) + Mth.randomBetweenInclusive($$0, 0, $$5);
         }
      }
   }

   @Override
   public HeightProviderType<?> getType() {
      return HeightProviderType.TRAPEZOID;
   }

   @Override
   public String toString() {
      return this.plateau == 0
         ? "triangle (" + this.minInclusive + "-" + this.maxInclusive + ")"
         : "trapezoid(" + this.plateau + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
   }
}
