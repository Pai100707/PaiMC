package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record AmbientSounds(
   Optional<Holder<SoundEvent>> loop,
   Optional<net.minecraft.world.attribute.AmbientMoodSettings> mood,
   List<net.minecraft.world.attribute.AmbientAdditionsSettings> additions
) {
   public static final net.minecraft.world.attribute.AmbientSounds EMPTY = new net.minecraft.world.attribute.AmbientSounds(
      Optional.empty(), Optional.empty(), List.of()
   );
   public static final net.minecraft.world.attribute.AmbientSounds LEGACY_CAVE_SETTINGS = new net.minecraft.world.attribute.AmbientSounds(
      Optional.empty(), Optional.of(net.minecraft.world.attribute.AmbientMoodSettings.LEGACY_CAVE_SETTINGS), List.of()
   );
   public static final Codec<net.minecraft.world.attribute.AmbientSounds> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            SoundEvent.CODEC.optionalFieldOf("loop").forGetter(net.minecraft.world.attribute.AmbientSounds::loop),
            net.minecraft.world.attribute.AmbientMoodSettings.CODEC.optionalFieldOf("mood").forGetter(net.minecraft.world.attribute.AmbientSounds::mood),
            ExtraCodecs.compactListCodec(net.minecraft.world.attribute.AmbientAdditionsSettings.CODEC)
               .optionalFieldOf("additions", List.of())
               .forGetter(net.minecraft.world.attribute.AmbientSounds::additions)
         )
         .apply($$0, net.minecraft.world.attribute.AmbientSounds::new)
   );
}
