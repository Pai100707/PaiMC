/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record GlobalPos(ResourceKey<Level> dimension, BlockPos pos) {
    public static final MapCodec<GlobalPos> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(GlobalPos::dimension), (App)BlockPos.CODEC.fieldOf("pos").forGetter(GlobalPos::pos)).apply((Applicative)$$0, GlobalPos::of));
    public static final Codec<GlobalPos> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, GlobalPos> STREAM_CODEC = StreamCodec.composite(ResourceKey.streamCodec(Registries.DIMENSION), GlobalPos::dimension, BlockPos.STREAM_CODEC, GlobalPos::pos, GlobalPos::of);

    public static GlobalPos of(ResourceKey<Level> $$0, BlockPos $$1) {
        return new GlobalPos($$0, $$1);
    }

    @Override
    public String toString() {
        return String.valueOf(this.dimension) + " " + String.valueOf(this.pos);
    }

    public boolean isCloseEnough(ResourceKey<Level> $$0, BlockPos $$1, int $$2) {
        return this.dimension.equals($$0) && this.pos.distChessboard($$1) <= $$2;
    }
}

