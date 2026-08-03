package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record Instrument(Holder<SoundEvent> soundEvent, float useDuration, float range, Component description) {
   public static final Codec<net.minecraft.world.item.Instrument> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            SoundEvent.CODEC.fieldOf("sound_event").forGetter(net.minecraft.world.item.Instrument::soundEvent),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("use_duration").forGetter(net.minecraft.world.item.Instrument::useDuration),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("range").forGetter(net.minecraft.world.item.Instrument::range),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(net.minecraft.world.item.Instrument::description)
         )
         .apply($$0, net.minecraft.world.item.Instrument::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.Instrument> DIRECT_STREAM_CODEC = StreamCodec.composite(
      SoundEvent.STREAM_CODEC,
      net.minecraft.world.item.Instrument::soundEvent,
      ByteBufCodecs.FLOAT,
      net.minecraft.world.item.Instrument::useDuration,
      ByteBufCodecs.FLOAT,
      net.minecraft.world.item.Instrument::range,
      ComponentSerialization.STREAM_CODEC,
      net.minecraft.world.item.Instrument::description,
      net.minecraft.world.item.Instrument::new
   );
   public static final Codec<Holder<net.minecraft.world.item.Instrument>> CODEC = RegistryFileCodec.create(Registries.INSTRUMENT, DIRECT_CODEC);
   public static final StreamCodec<RegistryFriendlyByteBuf, Holder<net.minecraft.world.item.Instrument>> STREAM_CODEC = ByteBufCodecs.holder(
      Registries.INSTRUMENT, DIRECT_STREAM_CODEC
   );
}
