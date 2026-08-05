/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record Brightness(int block, int sky) {
    public static final Codec<Integer> LIGHT_VALUE_CODEC = ExtraCodecs.intRange(0, 15);
    public static final Codec<Brightness> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)LIGHT_VALUE_CODEC.fieldOf("block").forGetter(Brightness::block), (App)LIGHT_VALUE_CODEC.fieldOf("sky").forGetter(Brightness::sky)).apply((Applicative)$$0, Brightness::new));
    public static final Brightness FULL_BRIGHT = new Brightness(15, 15);

    public static int pack(int $$0, int $$1) {
        return $$0 << 4 | $$1 << 20;
    }

    public int pack() {
        return Brightness.pack(this.block, this.sky);
    }

    public static int block(int $$0) {
        return $$0 >> 4 & 0xFFFF;
    }

    public static int sky(int $$0) {
        return $$0 >> 20 & 0xFFFF;
    }

    public static Brightness unpack(int $$0) {
        return new Brightness(Brightness.block($$0), Brightness.sky($$0));
    }
}

