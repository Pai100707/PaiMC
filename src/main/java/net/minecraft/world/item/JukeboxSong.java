package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public record JukeboxSong(Holder<SoundEvent> soundEvent, Component description, float lengthInSeconds, int comparatorOutput) {
   public static final Codec<net.minecraft.world.item.JukeboxSong> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            SoundEvent.CODEC.fieldOf("sound_event").forGetter(net.minecraft.world.item.JukeboxSong::soundEvent),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(net.minecraft.world.item.JukeboxSong::description),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("length_in_seconds").forGetter(net.minecraft.world.item.JukeboxSong::lengthInSeconds),
            ExtraCodecs.intRange(0, 15).fieldOf("comparator_output").forGetter(net.minecraft.world.item.JukeboxSong::comparatorOutput)
         )
         .apply($$0, net.minecraft.world.item.JukeboxSong::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.JukeboxSong> DIRECT_STREAM_CODEC = StreamCodec.composite(
      SoundEvent.STREAM_CODEC,
      net.minecraft.world.item.JukeboxSong::soundEvent,
      ComponentSerialization.STREAM_CODEC,
      net.minecraft.world.item.JukeboxSong::description,
      ByteBufCodecs.FLOAT,
      net.minecraft.world.item.JukeboxSong::lengthInSeconds,
      ByteBufCodecs.VAR_INT,
      net.minecraft.world.item.JukeboxSong::comparatorOutput,
      net.minecraft.world.item.JukeboxSong::new
   );
   public static final Codec<Holder<net.minecraft.world.item.JukeboxSong>> CODEC = RegistryFixedCodec.create(Registries.JUKEBOX_SONG);
   public static final StreamCodec<RegistryFriendlyByteBuf, Holder<net.minecraft.world.item.JukeboxSong>> STREAM_CODEC = ByteBufCodecs.holder(
      Registries.JUKEBOX_SONG, DIRECT_STREAM_CODEC
   );
   private static final int SONG_END_PADDING_TICKS = 20;

   public int lengthInTicks() {
      return Mth.ceil(this.lengthInSeconds * 20.0F);
   }

   public boolean hasFinished(long $$0) {
      return $$0 >= this.lengthInTicks() + 20;
   }

   public static Optional<Holder<net.minecraft.world.item.JukeboxSong>> fromStack(Provider $$0, net.minecraft.world.item.ItemStack $$1) {
      net.minecraft.world.item.JukeboxPlayable $$2 = (net.minecraft.world.item.JukeboxPlayable)$$1.get(DataComponents.JUKEBOX_PLAYABLE);
      return $$2 != null ? $$2.song().unwrap($$0) : Optional.empty();
   }
}
