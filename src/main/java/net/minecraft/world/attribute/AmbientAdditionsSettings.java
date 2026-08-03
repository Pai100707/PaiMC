package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public record AmbientAdditionsSettings(Holder<SoundEvent> soundEvent, double tickChance) {
   public static final Codec<net.minecraft.world.attribute.AmbientAdditionsSettings> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            SoundEvent.CODEC.fieldOf("sound").forGetter($$0x -> $$0x.soundEvent), Codec.DOUBLE.fieldOf("tick_chance").forGetter($$0x -> $$0x.tickChance)
         )
         .apply($$0, net.minecraft.world.attribute.AmbientAdditionsSettings::new)
   );
}
