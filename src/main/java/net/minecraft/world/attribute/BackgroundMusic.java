package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;

public record BackgroundMusic(Optional<Music> defaultMusic, Optional<Music> creativeMusic, Optional<Music> underwaterMusic) {
   public static final net.minecraft.world.attribute.BackgroundMusic EMPTY = new net.minecraft.world.attribute.BackgroundMusic(
      Optional.empty(), Optional.empty(), Optional.empty()
   );
   public static final net.minecraft.world.attribute.BackgroundMusic OVERWORLD = new net.minecraft.world.attribute.BackgroundMusic(
      Optional.of(Musics.GAME), Optional.of(Musics.CREATIVE), Optional.empty()
   );
   public static final Codec<net.minecraft.world.attribute.BackgroundMusic> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Music.CODEC.optionalFieldOf("default").forGetter(net.minecraft.world.attribute.BackgroundMusic::defaultMusic),
            Music.CODEC.optionalFieldOf("creative").forGetter(net.minecraft.world.attribute.BackgroundMusic::creativeMusic),
            Music.CODEC.optionalFieldOf("underwater").forGetter(net.minecraft.world.attribute.BackgroundMusic::underwaterMusic)
         )
         .apply($$0, net.minecraft.world.attribute.BackgroundMusic::new)
   );

   public BackgroundMusic(Music $$0) {
      this(Optional.of($$0), Optional.empty(), Optional.empty());
   }

   public BackgroundMusic(Holder<SoundEvent> $$0) {
      this(Musics.createGameMusic($$0));
   }

   public net.minecraft.world.attribute.BackgroundMusic withUnderwater(Music $$0) {
      return new net.minecraft.world.attribute.BackgroundMusic(this.defaultMusic, this.creativeMusic, Optional.of($$0));
   }

   public Optional<Music> select(boolean $$0, boolean $$1) {
      if ($$1 && this.underwaterMusic.isPresent()) {
         return this.underwaterMusic;
      } else {
         return $$0 && this.creativeMusic.isPresent() ? this.creativeMusic : this.defaultMusic;
      }
   }
}
