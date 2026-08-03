package net.minecraft.world.attribute.modifier;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FloatWithAlpha(float value, float alpha) {
   private static final Codec<FloatWithAlpha> FULL_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.FLOAT.fieldOf("value").forGetter(FloatWithAlpha::value),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("alpha", 1.0F).forGetter(FloatWithAlpha::alpha)
         )
         .apply($$0, FloatWithAlpha::new)
   );
   public static final Codec<FloatWithAlpha> CODEC = Codec.either(Codec.FLOAT, FULL_CODEC)
      .xmap($$0 -> (FloatWithAlpha)$$0.map(FloatWithAlpha::new, $$0x -> $$0x), $$0 -> $$0.alpha() == 1.0F ? Either.left($$0.value()) : Either.right($$0));

   public FloatWithAlpha(float $$0) {
      this($$0, 1.0F);
   }
}
