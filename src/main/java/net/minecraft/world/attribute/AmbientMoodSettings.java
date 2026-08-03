package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public record AmbientMoodSettings(Holder<SoundEvent> soundEvent, int tickDelay, int blockSearchExtent, double soundPositionOffset) {
   public static final Codec<net.minecraft.world.attribute.AmbientMoodSettings> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            SoundEvent.CODEC.fieldOf("sound").forGetter($$0x -> $$0x.soundEvent),
            Codec.INT.fieldOf("tick_delay").forGetter($$0x -> $$0x.tickDelay),
            Codec.INT.fieldOf("block_search_extent").forGetter($$0x -> $$0x.blockSearchExtent),
            Codec.DOUBLE.fieldOf("offset").forGetter($$0x -> $$0x.soundPositionOffset)
         )
         .apply($$0, net.minecraft.world.attribute.AmbientMoodSettings::new)
   );
   public static final net.minecraft.world.attribute.AmbientMoodSettings LEGACY_CAVE_SETTINGS = new net.minecraft.world.attribute.AmbientMoodSettings(
      SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0
   );
}
