package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;

public record Music(Holder<net.minecraft.sounds.SoundEvent> sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
   public static final Codec<net.minecraft.sounds.Music> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            net.minecraft.sounds.SoundEvent.CODEC.fieldOf("sound").forGetter(net.minecraft.sounds.Music::sound),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("min_delay").forGetter(net.minecraft.sounds.Music::minDelay),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_delay").forGetter(net.minecraft.sounds.Music::maxDelay),
            Codec.BOOL.optionalFieldOf("replace_current_music", false).forGetter(net.minecraft.sounds.Music::replaceCurrentMusic)
         )
         .apply($$0, net.minecraft.sounds.Music::new)
   );
}
