package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;

public class SpellParticleOption implements ParticleOptions {
   private final ParticleType<SpellParticleOption> type;
   private final int color;
   private final float power;

   public static MapCodec<SpellParticleOption> codec(ParticleType<SpellParticleOption> $$0) {
      return RecordCodecBuilder.mapCodec(
         $$1 -> $$1.group(
               ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("color", -1).forGetter($$0xx -> $$0xx.color),
               Codec.FLOAT.optionalFieldOf("power", 1.0F).forGetter($$0xx -> $$0xx.power)
            )
            .apply($$1, ($$1x, $$2) -> new SpellParticleOption($$0, $$1x, $$2))
      );
   }

   public static StreamCodec<? super ByteBuf, SpellParticleOption> streamCodec(ParticleType<SpellParticleOption> $$0) {
      return StreamCodec.composite(
         ByteBufCodecs.INT, $$0x -> $$0x.color, ByteBufCodecs.FLOAT, $$0x -> $$0x.power, ($$1, $$2) -> new SpellParticleOption($$0, $$1, $$2)
      );
   }

   private SpellParticleOption(ParticleType<SpellParticleOption> $$0, int $$1, float $$2) {
      this.type = $$0;
      this.color = $$1;
      this.power = $$2;
   }

   @Override
   public ParticleType<SpellParticleOption> getType() {
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

   public float getPower() {
      return this.power;
   }

   public static SpellParticleOption create(ParticleType<SpellParticleOption> $$0, int $$1, float $$2) {
      return new SpellParticleOption($$0, $$1, $$2);
   }

   public static SpellParticleOption create(ParticleType<SpellParticleOption> $$0, float $$1, float $$2, float $$3, float $$4) {
      return create($$0, ARGB.colorFromFloat(1.0F, $$1, $$2, $$3), $$4);
   }
}
