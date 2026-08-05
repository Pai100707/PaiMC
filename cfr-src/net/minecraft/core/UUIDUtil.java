/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.authlib.GameProfile
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.util.UndashedUuid
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.util.UndashedUuid;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;

public final class UUIDUtil {
    public static final Codec<UUID> CODEC = Codec.INT_STREAM.comapFlatMap($$0 -> Util.fixedSize($$0, 4).map(UUIDUtil::uuidFromIntArray), $$0 -> Arrays.stream(UUIDUtil.uuidToIntArray($$0)));
    public static final Codec<Set<UUID>> CODEC_SET = Codec.list(CODEC).xmap(Sets::newHashSet, Lists::newArrayList);
    public static final Codec<Set<UUID>> CODEC_LINKED_SET = Codec.list(CODEC).xmap(Sets::newLinkedHashSet, Lists::newArrayList);
    public static final Codec<UUID> STRING_CODEC = Codec.STRING.comapFlatMap($$0 -> {
        try {
            return DataResult.success((Object)UUID.fromString($$0), (Lifecycle)Lifecycle.stable());
        }
        catch (IllegalArgumentException $$1) {
            return DataResult.error(() -> "Invalid UUID " + $$0 + ": " + $$1.getMessage());
        }
    }, UUID::toString);
    public static final Codec<UUID> AUTHLIB_CODEC = Codec.withAlternative((Codec)Codec.STRING.comapFlatMap($$0 -> {
        try {
            return DataResult.success((Object)UndashedUuid.fromStringLenient((String)$$0), (Lifecycle)Lifecycle.stable());
        }
        catch (IllegalArgumentException $$1) {
            return DataResult.error(() -> "Invalid UUID " + $$0 + ": " + $$1.getMessage());
        }
    }, UndashedUuid::toString), CODEC);
    public static final Codec<UUID> LENIENT_CODEC = Codec.withAlternative(CODEC, STRING_CODEC);
    public static final StreamCodec<ByteBuf, UUID> STREAM_CODEC = new StreamCodec<ByteBuf, UUID>(){

        @Override
        public UUID decode(ByteBuf $$0) {
            return FriendlyByteBuf.readUUID($$0);
        }

        @Override
        public void encode(ByteBuf $$0, UUID $$1) {
            FriendlyByteBuf.writeUUID($$0, $$1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (UUID)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final int UUID_BYTES = 16;
    private static final String UUID_PREFIX_OFFLINE_PLAYER = "OfflinePlayer:";

    private UUIDUtil() {
    }

    public static UUID uuidFromIntArray(int[] $$0) {
        return new UUID((long)$$0[0] << 32 | (long)$$0[1] & 0xFFFFFFFFL, (long)$$0[2] << 32 | (long)$$0[3] & 0xFFFFFFFFL);
    }

    public static int[] uuidToIntArray(UUID $$0) {
        long $$1 = $$0.getMostSignificantBits();
        long $$2 = $$0.getLeastSignificantBits();
        return UUIDUtil.leastMostToIntArray($$1, $$2);
    }

    private static int[] leastMostToIntArray(long $$0, long $$1) {
        return new int[]{(int)($$0 >> 32), (int)$$0, (int)($$1 >> 32), (int)$$1};
    }

    public static byte[] uuidToByteArray(UUID $$0) {
        byte[] $$1 = new byte[16];
        ByteBuffer.wrap($$1).order(ByteOrder.BIG_ENDIAN).putLong($$0.getMostSignificantBits()).putLong($$0.getLeastSignificantBits());
        return $$1;
    }

    public static UUID readUUID(Dynamic<?> $$0) {
        int[] $$1 = $$0.asIntStream().toArray();
        if ($$1.length != 4) {
            throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + $$1.length + ".");
        }
        return UUIDUtil.uuidFromIntArray($$1);
    }

    public static UUID createOfflinePlayerUUID(String $$0) {
        return UUID.nameUUIDFromBytes((UUID_PREFIX_OFFLINE_PLAYER + $$0).getBytes(StandardCharsets.UTF_8));
    }

    public static GameProfile createOfflineProfile(String $$0) {
        UUID $$1 = UUIDUtil.createOfflinePlayerUUID($$0);
        return new GameProfile($$1, $$0);
    }
}

