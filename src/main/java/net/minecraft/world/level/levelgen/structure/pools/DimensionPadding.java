package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;

public record DimensionPadding(int bottom, int top) {
   private static final Codec<DimensionPadding> RECORD_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.NON_NEGATIVE_INT.lenientOptionalFieldOf("bottom", 0).forGetter($$0x -> $$0x.bottom),
            ExtraCodecs.NON_NEGATIVE_INT.lenientOptionalFieldOf("top", 0).forGetter($$0x -> $$0x.top)
         )
         .apply($$0, DimensionPadding::new)
   );
   public static final Codec<DimensionPadding> CODEC = Codec.either(ExtraCodecs.NON_NEGATIVE_INT, RECORD_CODEC)
      .xmap(
         $$0 -> (DimensionPadding)$$0.map(DimensionPadding::new, Function.identity()),
         $$0 -> $$0.hasEqualTopAndBottom() ? Either.left($$0.bottom) : Either.right($$0)
      );
   public static final DimensionPadding ZERO = new DimensionPadding(0);

   public DimensionPadding(int $$0) {
      this($$0, $$0);
   }

   public boolean hasEqualTopAndBottom() {
      return this.top == this.bottom;
   }
}
