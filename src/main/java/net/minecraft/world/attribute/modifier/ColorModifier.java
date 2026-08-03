package net.minecraft.world.attribute.modifier;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public interface ColorModifier<Argument> extends AttributeModifier<Integer, Argument> {
   ColorModifier<Integer> ALPHA_BLEND = new ColorModifier<Integer>() {
      public Integer apply(Integer $$0, Integer $$1) {
         return ARGB.alphaBlend($$0, $$1);
      }

      @Override
      public Codec<Integer> argumentCodec(net.minecraft.world.attribute.EnvironmentAttribute<Integer> $$0) {
         return ExtraCodecs.STRING_ARGB_COLOR;
      }

      @Override
      public net.minecraft.world.attribute.LerpFunction<Integer> argumentKeyframeLerp(net.minecraft.world.attribute.EnvironmentAttribute<Integer> $$0) {
         return net.minecraft.world.attribute.LerpFunction.ofColor();
      }
   };
   ColorModifier<Integer> ADD = ARGB::addRgb;
   ColorModifier<Integer> SUBTRACT = ARGB::subtractRgb;
   ColorModifier<Integer> MULTIPLY_RGB = ARGB::multiply;
   ColorModifier<Integer> MULTIPLY_ARGB = ARGB::multiply;
   ColorModifier<ColorModifier.BlendToGray> BLEND_TO_GRAY = new ColorModifier<ColorModifier.BlendToGray>() {
      public Integer apply(Integer $$0, ColorModifier.BlendToGray $$1) {
         int $$2 = ARGB.scaleRGB(ARGB.greyscale($$0), $$1.brightness);
         return ARGB.srgbLerp($$1.factor, $$0, $$2);
      }

      @Override
      public Codec<ColorModifier.BlendToGray> argumentCodec(net.minecraft.world.attribute.EnvironmentAttribute<Integer> $$0) {
         return ColorModifier.BlendToGray.CODEC;
      }

      @Override
      public net.minecraft.world.attribute.LerpFunction<ColorModifier.BlendToGray> argumentKeyframeLerp(
         net.minecraft.world.attribute.EnvironmentAttribute<Integer> $$0
      ) {
         return ($$0x, $$1, $$2) -> new ColorModifier.BlendToGray(Mth.lerp($$0x, $$1.brightness, $$2.brightness), Mth.lerp($$0x, $$1.factor, $$2.factor));
      }
   };

   @FunctionalInterface
   public interface ArgbModifier extends ColorModifier<Integer> {
      @Override
      default Codec<Integer> argumentCodec(net.minecraft.world.attribute.EnvironmentAttribute<Integer> $$0) {
         return Codec.either(ExtraCodecs.STRING_ARGB_COLOR, ExtraCodecs.RGB_COLOR_CODEC)
            .xmap(Either::unwrap, $$0x -> ARGB.alpha($$0x) == 255 ? Either.right($$0x) : Either.left($$0x));
      }

      @Override
      default net.minecraft.world.attribute.LerpFunction<Integer> argumentKeyframeLerp(net.minecraft.world.attribute.EnvironmentAttribute<Integer> $$0) {
         return net.minecraft.world.attribute.LerpFunction.ofColor();
      }
   }

   public record BlendToGray(float brightness, float factor) {
      public static final Codec<ColorModifier.BlendToGray> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.floatRange(0.0F, 1.0F).fieldOf("brightness").forGetter(ColorModifier.BlendToGray::brightness),
               Codec.floatRange(0.0F, 1.0F).fieldOf("factor").forGetter(ColorModifier.BlendToGray::factor)
            )
            .apply($$0, ColorModifier.BlendToGray::new)
      );
   }

   @FunctionalInterface
   public interface RgbModifier extends ColorModifier<Integer> {
      @Override
      default Codec<Integer> argumentCodec(net.minecraft.world.attribute.EnvironmentAttribute<Integer> $$0) {
         return ExtraCodecs.STRING_RGB_COLOR;
      }

      @Override
      default net.minecraft.world.attribute.LerpFunction<Integer> argumentKeyframeLerp(net.minecraft.world.attribute.EnvironmentAttribute<Integer> $$0) {
         return net.minecraft.world.attribute.LerpFunction.ofColor();
      }
   }
}
