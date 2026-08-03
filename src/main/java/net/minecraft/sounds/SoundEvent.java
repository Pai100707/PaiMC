package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;

public record SoundEvent(Identifier location, Optional<Float> fixedRange) {
   public static final Codec<net.minecraft.sounds.SoundEvent> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Identifier.CODEC.fieldOf("sound_id").forGetter(net.minecraft.sounds.SoundEvent::location),
            Codec.FLOAT.lenientOptionalFieldOf("range").forGetter(net.minecraft.sounds.SoundEvent::fixedRange)
         )
         .apply($$0, net.minecraft.sounds.SoundEvent::create)
   );
   public static final Codec<Holder<net.minecraft.sounds.SoundEvent>> CODEC = RegistryFileCodec.create(Registries.SOUND_EVENT, DIRECT_CODEC);
   public static final StreamCodec<ByteBuf, net.minecraft.sounds.SoundEvent> DIRECT_STREAM_CODEC = StreamCodec.composite(
      Identifier.STREAM_CODEC,
      net.minecraft.sounds.SoundEvent::location,
      ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional),
      net.minecraft.sounds.SoundEvent::fixedRange,
      net.minecraft.sounds.SoundEvent::create
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, Holder<net.minecraft.sounds.SoundEvent>> STREAM_CODEC = ByteBufCodecs.holder(
      Registries.SOUND_EVENT, DIRECT_STREAM_CODEC
   );

   private static net.minecraft.sounds.SoundEvent create(Identifier $$0, Optional<Float> $$1) {
      return $$1.<net.minecraft.sounds.SoundEvent>map($$1x -> createFixedRangeEvent($$0, $$1x)).orElseGet(() -> createVariableRangeEvent($$0));
   }

   public static net.minecraft.sounds.SoundEvent createVariableRangeEvent(Identifier $$0) {
      return new net.minecraft.sounds.SoundEvent($$0, Optional.empty());
   }

   public static net.minecraft.sounds.SoundEvent createFixedRangeEvent(Identifier $$0, float $$1) {
      return new net.minecraft.sounds.SoundEvent($$0, Optional.of($$1));
   }

   public float getRange(float $$0) {
      return this.fixedRange.orElse($$0 > 1.0F ? 16.0F * $$0 : 16.0F);
   }
}
