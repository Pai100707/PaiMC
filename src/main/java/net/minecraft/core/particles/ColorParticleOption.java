package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;

public class ColorParticleOption implements ParticleOptions {
   private final ParticleType<ColorParticleOption> type;
   private final int color;

   public static MapCodec<ColorParticleOption> codec(ParticleType<ColorParticleOption> $$0) {
      return ExtraCodecs.ARGB_COLOR_CODEC.xmap($$1 -> new ColorParticleOption($$0, $$1), $$0x -> $$0x.color).fieldOf("color");
   }

   public static StreamCodec<? super ByteBuf, ColorParticleOption> streamCodec(ParticleType<ColorParticleOption> $$0) {
      return ByteBufCodecs.INT.map($$1 -> new ColorParticleOption($$0, $$1), $$0x -> $$0x.color);
   }

   private ColorParticleOption(ParticleType<ColorParticleOption> $$0, int $$1) {
      this.type = $$0;
      this.color = $$1;
   }

   @Override
   public ParticleType<ColorParticleOption> getType() {
      return this.type;
   }

   public float getRed() {
      return ARGB.red(this.color) / 255.0F;
   }

   public float getGreen() {
      return ARGB.green(this.color) / 255.0F;
   }

   public float getBlue() {
      return ARGB.blue(this.color) / 255.0F;
   }

   public float getAlpha() {
      return ARGB.alpha(this.color) / 255.0F;
   }

   public static ColorParticleOption create(ParticleType<ColorParticleOption> $$0, int $$1) {
      return new ColorParticleOption($$0, $$1);
   }

   public static ColorParticleOption create(ParticleType<ColorParticleOption> $$0, float $$1, float $$2, float $$3) {
      return create($$0, ARGB.colorFromFloat(1.0F, $$1, $$2, $$3));
   }
}
