package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum Unit {
   INSTANCE;

   public static final Codec<net.minecraft.util.Unit> CODEC = MapCodec.unitCodec(INSTANCE);
   public static final StreamCodec<ByteBuf, net.minecraft.util.Unit> STREAM_CODEC = StreamCodec.unit(INSTANCE);
}
