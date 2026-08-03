package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PowerParticleOption implements ParticleOptions {
   private final ParticleType<PowerParticleOption> type;
   private final float power;

   public static MapCodec<PowerParticleOption> codec(ParticleType<PowerParticleOption> $$0) {
      return Codec.FLOAT.xmap($$1 -> new PowerParticleOption($$0, $$1), $$0x -> $$0x.power).optionalFieldOf("power", create($$0, 1.0F));
   }

   public static StreamCodec<? super ByteBuf, PowerParticleOption> streamCodec(ParticleType<PowerParticleOption> $$0) {
      return ByteBufCodecs.FLOAT.map($$1 -> new PowerParticleOption($$0, $$1), $$0x -> $$0x.power);
   }

   private PowerParticleOption(ParticleType<PowerParticleOption> $$0, float $$1) {
      this.type = $$0;
      this.power = $$1;
   }

   @Override
   public ParticleType<PowerParticleOption> getType() {
      return this.type;
   }

   public float getPower() {
      return this.power;
   }

   public static PowerParticleOption create(ParticleType<PowerParticleOption> $$0, float $$1) {
      return new PowerParticleOption($$0, $$1);
   }
}
