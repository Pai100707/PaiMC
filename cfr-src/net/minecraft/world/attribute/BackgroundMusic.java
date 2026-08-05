/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;

public record BackgroundMusic(Optional<Music> defaultMusic, Optional<Music> creativeMusic, Optional<Music> underwaterMusic) {
    public static final BackgroundMusic EMPTY = new BackgroundMusic(Optional.empty(), Optional.empty(), Optional.empty());
    public static final BackgroundMusic OVERWORLD = new BackgroundMusic(Optional.of(Musics.GAME), Optional.of(Musics.CREATIVE), Optional.empty());
    public static final Codec<BackgroundMusic> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Music.CODEC.optionalFieldOf("default").forGetter(BackgroundMusic::defaultMusic), (App)Music.CODEC.optionalFieldOf("creative").forGetter(BackgroundMusic::creativeMusic), (App)Music.CODEC.optionalFieldOf("underwater").forGetter(BackgroundMusic::underwaterMusic)).apply((Applicative)$$0, BackgroundMusic::new));

    public BackgroundMusic(Music $$0) {
        this(Optional.of($$0), Optional.empty(), Optional.empty());
    }

    public BackgroundMusic(Holder<SoundEvent> $$0) {
        this(Musics.createGameMusic($$0));
    }

    public BackgroundMusic withUnderwater(Music $$0) {
        return new BackgroundMusic(this.defaultMusic, this.creativeMusic, Optional.of($$0));
    }

    public Optional<Music> select(boolean $$0, boolean $$1) {
        if ($$1 && this.underwaterMusic.isPresent()) {
            return this.underwaterMusic;
        }
        if ($$0 && this.creativeMusic.isPresent()) {
            return this.creativeMusic;
        }
        return this.defaultMusic;
    }
}

