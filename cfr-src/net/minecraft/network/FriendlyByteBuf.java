/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufAllocator
 *  io.netty.buffer.ByteBufInputStream
 *  io.netty.buffer.ByteBufOutputStream
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  io.netty.util.ByteProcessor
 *  io.netty.util.ReferenceCounted
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import io.netty.util.ReferenceCounted;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.LpVec3;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class FriendlyByteBuf
extends ByteBuf {
    private final ByteBuf source;
    public static final short MAX_STRING_LENGTH = Short.MAX_VALUE;
    public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
    private static final int PUBLIC_KEY_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_LENGTH = 512;
    private static final Gson GSON = new Gson();

    public FriendlyByteBuf(ByteBuf $$0) {
        this.source = $$0;
    }

    @Deprecated
    public <T> T readWithCodecTrusted(DynamicOps<Tag> $$0, Codec<T> $$1) {
        return this.readWithCodec($$0, $$1, NbtAccounter.unlimitedHeap());
    }

    @Deprecated
    public <T> T readWithCodec(DynamicOps<Tag> $$0, Codec<T> $$12, NbtAccounter $$2) {
        Tag $$3 = this.readNbt($$2);
        return (T)$$12.parse($$0, (Object)$$3).getOrThrow($$1 -> new DecoderException("Failed to decode: " + $$1 + " " + String.valueOf($$3)));
    }

    @Deprecated
    public <T> FriendlyByteBuf writeWithCodec(DynamicOps<Tag> $$0, Codec<T> $$12, T $$2) {
        Tag $$3 = (Tag)$$12.encodeStart($$0, $$2).getOrThrow($$1 -> new EncoderException("Failed to encode: " + $$1 + " " + String.valueOf($$2)));
        this.writeNbt($$3);
        return this;
    }

    public <T> T readLenientJsonWithCodec(Codec<T> $$02) {
        JsonElement $$1 = LenientJsonParser.parse(this.readUtf());
        DataResult $$2 = $$02.parse((DynamicOps)JsonOps.INSTANCE, (Object)$$1);
        return (T)$$2.getOrThrow($$0 -> new DecoderException("Failed to decode JSON: " + $$0));
    }

    public <T> void writeJsonWithCodec(Codec<T> $$0, T $$12) {
        DataResult $$2 = $$0.encodeStart((DynamicOps)JsonOps.INSTANCE, $$12);
        this.writeUtf(GSON.toJson((JsonElement)$$2.getOrThrow($$1 -> new EncoderException("Failed to encode: " + $$1 + " " + String.valueOf($$12)))));
    }

    public static <T> IntFunction<T> limitValue(IntFunction<T> $$0, int $$1) {
        return $$2 -> {
            if ($$2 > $$1) {
                throw new DecoderException("Value " + $$2 + " is larger than limit " + $$1);
            }
            return $$0.apply($$2);
        };
    }

    public <T, C extends Collection<T>> C readCollection(IntFunction<C> $$0, StreamDecoder<? super FriendlyByteBuf, T> $$1) {
        int $$2 = this.readVarInt();
        Collection $$3 = (Collection)$$0.apply($$2);
        for (int $$4 = 0; $$4 < $$2; ++$$4) {
            $$3.add($$1.decode(this));
        }
        return (C)$$3;
    }

    public <T> void writeCollection(Collection<T> $$0, StreamEncoder<? super FriendlyByteBuf, T> $$1) {
        this.writeVarInt($$0.size());
        for (T $$2 : $$0) {
            $$1.encode(this, $$2);
        }
    }

    public <T> List<T> readList(StreamDecoder<? super FriendlyByteBuf, T> $$0) {
        return this.readCollection(Lists::newArrayListWithCapacity, $$0);
    }

    public IntList readIntIdList() {
        int $$0 = this.readVarInt();
        IntArrayList $$1 = new IntArrayList();
        for (int $$2 = 0; $$2 < $$0; ++$$2) {
            $$1.add(this.readVarInt());
        }
        return $$1;
    }

    public void writeIntIdList(IntList $$0) {
        this.writeVarInt($$0.size());
        $$0.forEach(this::writeVarInt);
    }

    public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> $$0, StreamDecoder<? super FriendlyByteBuf, K> $$1, StreamDecoder<? super FriendlyByteBuf, V> $$2) {
        int $$3 = this.readVarInt();
        Map $$4 = (Map)$$0.apply($$3);
        for (int $$5 = 0; $$5 < $$3; ++$$5) {
            K $$6 = $$1.decode(this);
            V $$7 = $$2.decode(this);
            $$4.put($$6, $$7);
        }
        return (M)$$4;
    }

    public <K, V> Map<K, V> readMap(StreamDecoder<? super FriendlyByteBuf, K> $$0, StreamDecoder<? super FriendlyByteBuf, V> $$1) {
        return this.readMap(Maps::newHashMapWithExpectedSize, $$0, $$1);
    }

    public <K, V> void writeMap(Map<K, V> $$0, StreamEncoder<? super FriendlyByteBuf, K> $$1, StreamEncoder<? super FriendlyByteBuf, V> $$22) {
        this.writeVarInt($$0.size());
        $$0.forEach(($$2, $$3) -> {
            $$1.encode(this, $$2);
            $$22.encode(this, $$3);
        });
    }

    public void readWithCount(Consumer<FriendlyByteBuf> $$0) {
        int $$1 = this.readVarInt();
        for (int $$2 = 0; $$2 < $$1; ++$$2) {
            $$0.accept(this);
        }
    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> $$0, Class<E> $$1) {
        Enum[] $$2 = (Enum[])$$1.getEnumConstants();
        BitSet $$3 = new BitSet($$2.length);
        for (int $$4 = 0; $$4 < $$2.length; ++$$4) {
            $$3.set($$4, $$0.contains($$2[$$4]));
        }
        this.writeFixedBitSet($$3, $$2.length);
    }

    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> $$0) {
        Enum[] $$1 = (Enum[])$$0.getEnumConstants();
        BitSet $$2 = this.readFixedBitSet($$1.length);
        EnumSet<Enum> $$3 = EnumSet.noneOf($$0);
        for (int $$4 = 0; $$4 < $$1.length; ++$$4) {
            if (!$$2.get($$4)) continue;
            $$3.add($$1[$$4]);
        }
        return $$3;
    }

    public <T> void writeOptional(Optional<T> $$0, StreamEncoder<? super FriendlyByteBuf, T> $$1) {
        if ($$0.isPresent()) {
            this.writeBoolean(true);
            $$1.encode(this, $$0.get());
        } else {
            this.writeBoolean(false);
        }
    }

    public <T> Optional<T> readOptional(StreamDecoder<? super FriendlyByteBuf, T> $$0) {
        if (this.readBoolean()) {
            return Optional.of($$0.decode(this));
        }
        return Optional.empty();
    }

    public <L, R> void writeEither(Either<L, R> $$0, StreamEncoder<? super FriendlyByteBuf, L> $$12, StreamEncoder<? super FriendlyByteBuf, R> $$2) {
        $$0.ifLeft($$1 -> {
            this.writeBoolean(true);
            $$12.encode(this, $$1);
        }).ifRight($$1 -> {
            this.writeBoolean(false);
            $$2.encode(this, $$1);
        });
    }

    public <L, R> Either<L, R> readEither(StreamDecoder<? super FriendlyByteBuf, L> $$0, StreamDecoder<? super FriendlyByteBuf, R> $$1) {
        if (this.readBoolean()) {
            return Either.left($$0.decode(this));
        }
        return Either.right($$1.decode(this));
    }

    public <T> @Nullable T readNullable(StreamDecoder<? super FriendlyByteBuf, T> $$0) {
        return FriendlyByteBuf.readNullable(this, $$0);
    }

    public static <T, B extends ByteBuf> @Nullable T readNullable(B $$0, StreamDecoder<? super B, T> $$1) {
        if ($$0.readBoolean()) {
            return $$1.decode($$0);
        }
        return null;
    }

    public <T> void writeNullable(@Nullable T $$0, StreamEncoder<? super FriendlyByteBuf, T> $$1) {
        FriendlyByteBuf.writeNullable(this, $$0, $$1);
    }

    public static <T, B extends ByteBuf> void writeNullable(B $$0, @Nullable T $$1, StreamEncoder<? super B, T> $$2) {
        if ($$1 != null) {
            $$0.writeBoolean(true);
            $$2.encode($$0, $$1);
        } else {
            $$0.writeBoolean(false);
        }
    }

    public byte[] readByteArray() {
        return FriendlyByteBuf.readByteArray(this);
    }

    public static byte[] readByteArray(ByteBuf $$0) {
        return FriendlyByteBuf.readByteArray($$0, $$0.readableBytes());
    }

    public FriendlyByteBuf writeByteArray(byte[] $$0) {
        FriendlyByteBuf.writeByteArray(this, $$0);
        return this;
    }

    public static void writeByteArray(ByteBuf $$0, byte[] $$1) {
        VarInt.write($$0, $$1.length);
        $$0.writeBytes($$1);
    }

    public byte[] readByteArray(int $$0) {
        return FriendlyByteBuf.readByteArray(this, $$0);
    }

    public static byte[] readByteArray(ByteBuf $$0, int $$1) {
        int $$2 = VarInt.read($$0);
        if ($$2 > $$1) {
            throw new DecoderException("ByteArray with size " + $$2 + " is bigger than allowed " + $$1);
        }
        byte[] $$3 = new byte[$$2];
        $$0.readBytes($$3);
        return $$3;
    }

    public FriendlyByteBuf writeVarIntArray(int[] $$0) {
        this.writeVarInt($$0.length);
        for (int $$1 : $$0) {
            this.writeVarInt($$1);
        }
        return this;
    }

    public int[] readVarIntArray() {
        return this.readVarIntArray(this.readableBytes());
    }

    public int[] readVarIntArray(int $$0) {
        int $$1 = this.readVarInt();
        if ($$1 > $$0) {
            throw new DecoderException("VarIntArray with size " + $$1 + " is bigger than allowed " + $$0);
        }
        int[] $$2 = new int[$$1];
        for (int $$3 = 0; $$3 < $$2.length; ++$$3) {
            $$2[$$3] = this.readVarInt();
        }
        return $$2;
    }

    public FriendlyByteBuf writeLongArray(long[] $$0) {
        FriendlyByteBuf.writeLongArray(this, $$0);
        return this;
    }

    public static void writeLongArray(ByteBuf $$0, long[] $$1) {
        VarInt.write($$0, $$1.length);
        FriendlyByteBuf.writeFixedSizeLongArray($$0, $$1);
    }

    public FriendlyByteBuf writeFixedSizeLongArray(long[] $$0) {
        FriendlyByteBuf.writeFixedSizeLongArray(this, $$0);
        return this;
    }

    public static void writeFixedSizeLongArray(ByteBuf $$0, long[] $$1) {
        for (long $$2 : $$1) {
            $$0.writeLong($$2);
        }
    }

    public long[] readLongArray() {
        return FriendlyByteBuf.readLongArray(this);
    }

    public long[] readFixedSizeLongArray(long[] $$0) {
        return FriendlyByteBuf.readFixedSizeLongArray(this, $$0);
    }

    public static long[] readLongArray(ByteBuf $$0) {
        int $$2;
        int $$1 = VarInt.read($$0);
        if ($$1 > ($$2 = $$0.readableBytes() / 8)) {
            throw new DecoderException("LongArray with size " + $$1 + " is bigger than allowed " + $$2);
        }
        return FriendlyByteBuf.readFixedSizeLongArray($$0, new long[$$1]);
    }

    public static long[] readFixedSizeLongArray(ByteBuf $$0, long[] $$1) {
        for (int $$2 = 0; $$2 < $$1.length; ++$$2) {
            $$1[$$2] = $$0.readLong();
        }
        return $$1;
    }

    public BlockPos readBlockPos() {
        return FriendlyByteBuf.readBlockPos(this);
    }

    public static BlockPos readBlockPos(ByteBuf $$0) {
        return BlockPos.of($$0.readLong());
    }

    public FriendlyByteBuf writeBlockPos(BlockPos $$0) {
        FriendlyByteBuf.writeBlockPos(this, $$0);
        return this;
    }

    public static void writeBlockPos(ByteBuf $$0, BlockPos $$1) {
        $$0.writeLong($$1.asLong());
    }

    public ChunkPos readChunkPos() {
        return new ChunkPos(this.readLong());
    }

    public FriendlyByteBuf writeChunkPos(ChunkPos $$0) {
        this.writeLong($$0.toLong());
        return this;
    }

    public static ChunkPos readChunkPos(ByteBuf $$0) {
        return new ChunkPos($$0.readLong());
    }

    public static void writeChunkPos(ByteBuf $$0, ChunkPos $$1) {
        $$0.writeLong($$1.toLong());
    }

    public GlobalPos readGlobalPos() {
        ResourceKey<Level> $$0 = this.readResourceKey(Registries.DIMENSION);
        BlockPos $$1 = this.readBlockPos();
        return GlobalPos.of($$0, $$1);
    }

    public void writeGlobalPos(GlobalPos $$0) {
        this.writeResourceKey($$0.dimension());
        this.writeBlockPos($$0.pos());
    }

    public Vector3f readVector3f() {
        return FriendlyByteBuf.readVector3f(this);
    }

    public static Vector3f readVector3f(ByteBuf $$0) {
        return new Vector3f($$0.readFloat(), $$0.readFloat(), $$0.readFloat());
    }

    public void writeVector3f(Vector3f $$0) {
        FriendlyByteBuf.writeVector3f(this, (Vector3fc)$$0);
    }

    public static void writeVector3f(ByteBuf $$0, Vector3fc $$1) {
        $$0.writeFloat($$1.x());
        $$0.writeFloat($$1.y());
        $$0.writeFloat($$1.z());
    }

    public Quaternionf readQuaternion() {
        return FriendlyByteBuf.readQuaternion(this);
    }

    public static Quaternionf readQuaternion(ByteBuf $$0) {
        return new Quaternionf($$0.readFloat(), $$0.readFloat(), $$0.readFloat(), $$0.readFloat());
    }

    public void writeQuaternion(Quaternionf $$0) {
        FriendlyByteBuf.writeQuaternion(this, (Quaternionfc)$$0);
    }

    public static void writeQuaternion(ByteBuf $$0, Quaternionfc $$1) {
        $$0.writeFloat($$1.x());
        $$0.writeFloat($$1.y());
        $$0.writeFloat($$1.z());
        $$0.writeFloat($$1.w());
    }

    public static Vec3 readVec3(ByteBuf $$0) {
        return new Vec3($$0.readDouble(), $$0.readDouble(), $$0.readDouble());
    }

    public Vec3 readVec3() {
        return FriendlyByteBuf.readVec3(this);
    }

    public static void writeVec3(ByteBuf $$0, Vec3 $$1) {
        $$0.writeDouble($$1.x());
        $$0.writeDouble($$1.y());
        $$0.writeDouble($$1.z());
    }

    public void writeVec3(Vec3 $$0) {
        FriendlyByteBuf.writeVec3(this, $$0);
    }

    public Vec3 readLpVec3() {
        return LpVec3.read(this);
    }

    public void writeLpVec3(Vec3 $$0) {
        LpVec3.write(this, $$0);
    }

    public <T extends Enum<T>> T readEnum(Class<T> $$0) {
        return (T)((Enum[])$$0.getEnumConstants())[this.readVarInt()];
    }

    public FriendlyByteBuf writeEnum(Enum<?> $$0) {
        return this.writeVarInt($$0.ordinal());
    }

    public <T> T readById(IntFunction<T> $$0) {
        int $$1 = this.readVarInt();
        return $$0.apply($$1);
    }

    public <T> FriendlyByteBuf writeById(ToIntFunction<T> $$0, T $$1) {
        int $$2 = $$0.applyAsInt($$1);
        return this.writeVarInt($$2);
    }

    public int readVarInt() {
        return VarInt.read(this.source);
    }

    public long readVarLong() {
        return VarLong.read(this.source);
    }

    public FriendlyByteBuf writeUUID(UUID $$0) {
        FriendlyByteBuf.writeUUID(this, $$0);
        return this;
    }

    public static void writeUUID(ByteBuf $$0, UUID $$1) {
        $$0.writeLong($$1.getMostSignificantBits());
        $$0.writeLong($$1.getLeastSignificantBits());
    }

    public UUID readUUID() {
        return FriendlyByteBuf.readUUID(this);
    }

    public static UUID readUUID(ByteBuf $$0) {
        return new UUID($$0.readLong(), $$0.readLong());
    }

    public FriendlyByteBuf writeVarInt(int $$0) {
        VarInt.write(this.source, $$0);
        return this;
    }

    public FriendlyByteBuf writeVarLong(long $$0) {
        VarLong.write(this.source, $$0);
        return this;
    }

    public FriendlyByteBuf writeNbt(@Nullable Tag $$0) {
        FriendlyByteBuf.writeNbt(this, $$0);
        return this;
    }

    public static void writeNbt(ByteBuf $$0, @Nullable Tag $$1) {
        if ($$1 == null) {
            $$1 = EndTag.INSTANCE;
        }
        try {
            NbtIo.writeAnyTag($$1, (DataOutput)new ByteBufOutputStream($$0));
        }
        catch (IOException $$2) {
            throw new EncoderException((Throwable)$$2);
        }
    }

    public @Nullable CompoundTag readNbt() {
        return FriendlyByteBuf.readNbt(this);
    }

    public static @Nullable CompoundTag readNbt(ByteBuf $$0) {
        Tag $$1 = FriendlyByteBuf.readNbt($$0, NbtAccounter.defaultQuota());
        if ($$1 == null || $$1 instanceof CompoundTag) {
            return (CompoundTag)$$1;
        }
        throw new DecoderException("Not a compound tag: " + String.valueOf($$1));
    }

    public static @Nullable Tag readNbt(ByteBuf $$0, NbtAccounter $$1) {
        try {
            Tag $$2 = NbtIo.readAnyTag((DataInput)new ByteBufInputStream($$0), $$1);
            if ($$2.getId() == 0) {
                return null;
            }
            return $$2;
        }
        catch (IOException $$3) {
            throw new EncoderException((Throwable)$$3);
        }
    }

    public @Nullable Tag readNbt(NbtAccounter $$0) {
        return FriendlyByteBuf.readNbt(this, $$0);
    }

    public String readUtf() {
        return this.readUtf(Short.MAX_VALUE);
    }

    public String readUtf(int $$0) {
        return Utf8String.read(this.source, $$0);
    }

    public FriendlyByteBuf writeUtf(String $$0) {
        return this.writeUtf($$0, Short.MAX_VALUE);
    }

    public FriendlyByteBuf writeUtf(String $$0, int $$1) {
        Utf8String.write(this.source, $$0, $$1);
        return this;
    }

    public Identifier readIdentifier() {
        return Identifier.parse(this.readUtf(Short.MAX_VALUE));
    }

    public FriendlyByteBuf writeIdentifier(Identifier $$0) {
        this.writeUtf($$0.toString());
        return this;
    }

    public <T> ResourceKey<T> readResourceKey(ResourceKey<? extends Registry<T>> $$0) {
        Identifier $$1 = this.readIdentifier();
        return ResourceKey.create($$0, $$1);
    }

    public void writeResourceKey(ResourceKey<?> $$0) {
        this.writeIdentifier($$0.identifier());
    }

    public <T> ResourceKey<? extends Registry<T>> readRegistryKey() {
        Identifier $$0 = this.readIdentifier();
        return ResourceKey.createRegistryKey($$0);
    }

    public Instant readInstant() {
        return Instant.ofEpochMilli(this.readLong());
    }

    public void writeInstant(Instant $$0) {
        this.writeLong($$0.toEpochMilli());
    }

    public PublicKey readPublicKey() {
        try {
            return Crypt.byteToPublicKey(this.readByteArray(512));
        }
        catch (CryptException $$0) {
            throw new DecoderException("Malformed public key bytes", (Throwable)$$0);
        }
    }

    public FriendlyByteBuf writePublicKey(PublicKey $$0) {
        this.writeByteArray($$0.getEncoded());
        return this;
    }

    public BlockHitResult readBlockHitResult() {
        BlockPos $$0 = this.readBlockPos();
        Direction $$1 = this.readEnum(Direction.class);
        float $$2 = this.readFloat();
        float $$3 = this.readFloat();
        float $$4 = this.readFloat();
        boolean $$5 = this.readBoolean();
        boolean $$6 = this.readBoolean();
        return new BlockHitResult(new Vec3((double)$$0.getX() + (double)$$2, (double)$$0.getY() + (double)$$3, (double)$$0.getZ() + (double)$$4), $$1, $$0, $$5, $$6);
    }

    public void writeBlockHitResult(BlockHitResult $$0) {
        BlockPos $$1 = $$0.getBlockPos();
        this.writeBlockPos($$1);
        this.writeEnum($$0.getDirection());
        Vec3 $$2 = $$0.getLocation();
        this.writeFloat((float)($$2.x - (double)$$1.getX()));
        this.writeFloat((float)($$2.y - (double)$$1.getY()));
        this.writeFloat((float)($$2.z - (double)$$1.getZ()));
        this.writeBoolean($$0.isInside());
        this.writeBoolean($$0.isWorldBorderHit());
    }

    public BitSet readBitSet() {
        return BitSet.valueOf(this.readLongArray());
    }

    public void writeBitSet(BitSet $$0) {
        this.writeLongArray($$0.toLongArray());
    }

    public BitSet readFixedBitSet(int $$0) {
        byte[] $$1 = new byte[Mth.positiveCeilDiv($$0, 8)];
        this.readBytes($$1);
        return BitSet.valueOf($$1);
    }

    public void writeFixedBitSet(BitSet $$0, int $$1) {
        if ($$0.length() > $$1) {
            throw new EncoderException("BitSet is larger than expected size (" + $$0.length() + ">" + $$1 + ")");
        }
        byte[] $$2 = $$0.toByteArray();
        this.writeBytes(Arrays.copyOf($$2, Mth.positiveCeilDiv($$1, 8)));
    }

    public static int readContainerId(ByteBuf $$0) {
        return VarInt.read($$0);
    }

    public int readContainerId() {
        return FriendlyByteBuf.readContainerId(this.source);
    }

    public static void writeContainerId(ByteBuf $$0, int $$1) {
        VarInt.write($$0, $$1);
    }

    public void writeContainerId(int $$0) {
        FriendlyByteBuf.writeContainerId(this.source, $$0);
    }

    public boolean isContiguous() {
        return this.source.isContiguous();
    }

    public int maxFastWritableBytes() {
        return this.source.maxFastWritableBytes();
    }

    public int capacity() {
        return this.source.capacity();
    }

    public FriendlyByteBuf capacity(int $$0) {
        this.source.capacity($$0);
        return this;
    }

    public int maxCapacity() {
        return this.source.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return this.source.alloc();
    }

    public ByteOrder order() {
        return this.source.order();
    }

    public ByteBuf order(ByteOrder $$0) {
        return this.source.order($$0);
    }

    public ByteBuf unwrap() {
        return this.source;
    }

    public boolean isDirect() {
        return this.source.isDirect();
    }

    public boolean isReadOnly() {
        return this.source.isReadOnly();
    }

    public ByteBuf asReadOnly() {
        return this.source.asReadOnly();
    }

    public int readerIndex() {
        return this.source.readerIndex();
    }

    public FriendlyByteBuf readerIndex(int $$0) {
        this.source.readerIndex($$0);
        return this;
    }

    public int writerIndex() {
        return this.source.writerIndex();
    }

    public FriendlyByteBuf writerIndex(int $$0) {
        this.source.writerIndex($$0);
        return this;
    }

    public FriendlyByteBuf setIndex(int $$0, int $$1) {
        this.source.setIndex($$0, $$1);
        return this;
    }

    public int readableBytes() {
        return this.source.readableBytes();
    }

    public int writableBytes() {
        return this.source.writableBytes();
    }

    public int maxWritableBytes() {
        return this.source.maxWritableBytes();
    }

    public boolean isReadable() {
        return this.source.isReadable();
    }

    public boolean isReadable(int $$0) {
        return this.source.isReadable($$0);
    }

    public boolean isWritable() {
        return this.source.isWritable();
    }

    public boolean isWritable(int $$0) {
        return this.source.isWritable($$0);
    }

    public FriendlyByteBuf clear() {
        this.source.clear();
        return this;
    }

    public FriendlyByteBuf markReaderIndex() {
        this.source.markReaderIndex();
        return this;
    }

    public FriendlyByteBuf resetReaderIndex() {
        this.source.resetReaderIndex();
        return this;
    }

    public FriendlyByteBuf markWriterIndex() {
        this.source.markWriterIndex();
        return this;
    }

    public FriendlyByteBuf resetWriterIndex() {
        this.source.resetWriterIndex();
        return this;
    }

    public FriendlyByteBuf discardReadBytes() {
        this.source.discardReadBytes();
        return this;
    }

    public FriendlyByteBuf discardSomeReadBytes() {
        this.source.discardSomeReadBytes();
        return this;
    }

    public FriendlyByteBuf ensureWritable(int $$0) {
        this.source.ensureWritable($$0);
        return this;
    }

    public int ensureWritable(int $$0, boolean $$1) {
        return this.source.ensureWritable($$0, $$1);
    }

    public boolean getBoolean(int $$0) {
        return this.source.getBoolean($$0);
    }

    public byte getByte(int $$0) {
        return this.source.getByte($$0);
    }

    public short getUnsignedByte(int $$0) {
        return this.source.getUnsignedByte($$0);
    }

    public short getShort(int $$0) {
        return this.source.getShort($$0);
    }

    public short getShortLE(int $$0) {
        return this.source.getShortLE($$0);
    }

    public int getUnsignedShort(int $$0) {
        return this.source.getUnsignedShort($$0);
    }

    public int getUnsignedShortLE(int $$0) {
        return this.source.getUnsignedShortLE($$0);
    }

    public int getMedium(int $$0) {
        return this.source.getMedium($$0);
    }

    public int getMediumLE(int $$0) {
        return this.source.getMediumLE($$0);
    }

    public int getUnsignedMedium(int $$0) {
        return this.source.getUnsignedMedium($$0);
    }

    public int getUnsignedMediumLE(int $$0) {
        return this.source.getUnsignedMediumLE($$0);
    }

    public int getInt(int $$0) {
        return this.source.getInt($$0);
    }

    public int getIntLE(int $$0) {
        return this.source.getIntLE($$0);
    }

    public long getUnsignedInt(int $$0) {
        return this.source.getUnsignedInt($$0);
    }

    public long getUnsignedIntLE(int $$0) {
        return this.source.getUnsignedIntLE($$0);
    }

    public long getLong(int $$0) {
        return this.source.getLong($$0);
    }

    public long getLongLE(int $$0) {
        return this.source.getLongLE($$0);
    }

    public char getChar(int $$0) {
        return this.source.getChar($$0);
    }

    public float getFloat(int $$0) {
        return this.source.getFloat($$0);
    }

    public double getDouble(int $$0) {
        return this.source.getDouble($$0);
    }

    public FriendlyByteBuf getBytes(int $$0, ByteBuf $$1) {
        this.source.getBytes($$0, $$1);
        return this;
    }

    public FriendlyByteBuf getBytes(int $$0, ByteBuf $$1, int $$2) {
        this.source.getBytes($$0, $$1, $$2);
        return this;
    }

    public FriendlyByteBuf getBytes(int $$0, ByteBuf $$1, int $$2, int $$3) {
        this.source.getBytes($$0, $$1, $$2, $$3);
        return this;
    }

    public FriendlyByteBuf getBytes(int $$0, byte[] $$1) {
        this.source.getBytes($$0, $$1);
        return this;
    }

    public FriendlyByteBuf getBytes(int $$0, byte[] $$1, int $$2, int $$3) {
        this.source.getBytes($$0, $$1, $$2, $$3);
        return this;
    }

    public FriendlyByteBuf getBytes(int $$0, ByteBuffer $$1) {
        this.source.getBytes($$0, $$1);
        return this;
    }

    public FriendlyByteBuf getBytes(int $$0, OutputStream $$1, int $$2) throws IOException {
        this.source.getBytes($$0, $$1, $$2);
        return this;
    }

    public int getBytes(int $$0, GatheringByteChannel $$1, int $$2) throws IOException {
        return this.source.getBytes($$0, $$1, $$2);
    }

    public int getBytes(int $$0, FileChannel $$1, long $$2, int $$3) throws IOException {
        return this.source.getBytes($$0, $$1, $$2, $$3);
    }

    public CharSequence getCharSequence(int $$0, int $$1, Charset $$2) {
        return this.source.getCharSequence($$0, $$1, $$2);
    }

    public FriendlyByteBuf setBoolean(int $$0, boolean $$1) {
        this.source.setBoolean($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setByte(int $$0, int $$1) {
        this.source.setByte($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setShort(int $$0, int $$1) {
        this.source.setShort($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setShortLE(int $$0, int $$1) {
        this.source.setShortLE($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setMedium(int $$0, int $$1) {
        this.source.setMedium($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setMediumLE(int $$0, int $$1) {
        this.source.setMediumLE($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setInt(int $$0, int $$1) {
        this.source.setInt($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setIntLE(int $$0, int $$1) {
        this.source.setIntLE($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setLong(int $$0, long $$1) {
        this.source.setLong($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setLongLE(int $$0, long $$1) {
        this.source.setLongLE($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setChar(int $$0, int $$1) {
        this.source.setChar($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setFloat(int $$0, float $$1) {
        this.source.setFloat($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setDouble(int $$0, double $$1) {
        this.source.setDouble($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setBytes(int $$0, ByteBuf $$1) {
        this.source.setBytes($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setBytes(int $$0, ByteBuf $$1, int $$2) {
        this.source.setBytes($$0, $$1, $$2);
        return this;
    }

    public FriendlyByteBuf setBytes(int $$0, ByteBuf $$1, int $$2, int $$3) {
        this.source.setBytes($$0, $$1, $$2, $$3);
        return this;
    }

    public FriendlyByteBuf setBytes(int $$0, byte[] $$1) {
        this.source.setBytes($$0, $$1);
        return this;
    }

    public FriendlyByteBuf setBytes(int $$0, byte[] $$1, int $$2, int $$3) {
        this.source.setBytes($$0, $$1, $$2, $$3);
        return this;
    }

    public FriendlyByteBuf setBytes(int $$0, ByteBuffer $$1) {
        this.source.setBytes($$0, $$1);
        return this;
    }

    public int setBytes(int $$0, InputStream $$1, int $$2) throws IOException {
        return this.source.setBytes($$0, $$1, $$2);
    }

    public int setBytes(int $$0, ScatteringByteChannel $$1, int $$2) throws IOException {
        return this.source.setBytes($$0, $$1, $$2);
    }

    public int setBytes(int $$0, FileChannel $$1, long $$2, int $$3) throws IOException {
        return this.source.setBytes($$0, $$1, $$2, $$3);
    }

    public FriendlyByteBuf setZero(int $$0, int $$1) {
        this.source.setZero($$0, $$1);
        return this;
    }

    public int setCharSequence(int $$0, CharSequence $$1, Charset $$2) {
        return this.source.setCharSequence($$0, $$1, $$2);
    }

    public boolean readBoolean() {
        return this.source.readBoolean();
    }

    public byte readByte() {
        return this.source.readByte();
    }

    public short readUnsignedByte() {
        return this.source.readUnsignedByte();
    }

    public short readShort() {
        return this.source.readShort();
    }

    public short readShortLE() {
        return this.source.readShortLE();
    }

    public int readUnsignedShort() {
        return this.source.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return this.source.readUnsignedShortLE();
    }

    public int readMedium() {
        return this.source.readMedium();
    }

    public int readMediumLE() {
        return this.source.readMediumLE();
    }

    public int readUnsignedMedium() {
        return this.source.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return this.source.readUnsignedMediumLE();
    }

    public int readInt() {
        return this.source.readInt();
    }

    public int readIntLE() {
        return this.source.readIntLE();
    }

    public long readUnsignedInt() {
        return this.source.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return this.source.readUnsignedIntLE();
    }

    public long readLong() {
        return this.source.readLong();
    }

    public long readLongLE() {
        return this.source.readLongLE();
    }

    public char readChar() {
        return this.source.readChar();
    }

    public float readFloat() {
        return this.source.readFloat();
    }

    public double readDouble() {
        return this.source.readDouble();
    }

    public ByteBuf readBytes(int $$0) {
        return this.source.readBytes($$0);
    }

    public ByteBuf readSlice(int $$0) {
        return this.source.readSlice($$0);
    }

    public ByteBuf readRetainedSlice(int $$0) {
        return this.source.readRetainedSlice($$0);
    }

    public FriendlyByteBuf readBytes(ByteBuf $$0) {
        this.source.readBytes($$0);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuf $$0, int $$1) {
        this.source.readBytes($$0, $$1);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuf $$0, int $$1, int $$2) {
        this.source.readBytes($$0, $$1, $$2);
        return this;
    }

    public FriendlyByteBuf readBytes(byte[] $$0) {
        this.source.readBytes($$0);
        return this;
    }

    public FriendlyByteBuf readBytes(byte[] $$0, int $$1, int $$2) {
        this.source.readBytes($$0, $$1, $$2);
        return this;
    }

    public FriendlyByteBuf readBytes(ByteBuffer $$0) {
        this.source.readBytes($$0);
        return this;
    }

    public FriendlyByteBuf readBytes(OutputStream $$0, int $$1) throws IOException {
        this.source.readBytes($$0, $$1);
        return this;
    }

    public int readBytes(GatheringByteChannel $$0, int $$1) throws IOException {
        return this.source.readBytes($$0, $$1);
    }

    public CharSequence readCharSequence(int $$0, Charset $$1) {
        return this.source.readCharSequence($$0, $$1);
    }

    public String readString(int $$0, Charset $$1) {
        return this.source.readString($$0, $$1);
    }

    public int readBytes(FileChannel $$0, long $$1, int $$2) throws IOException {
        return this.source.readBytes($$0, $$1, $$2);
    }

    public FriendlyByteBuf skipBytes(int $$0) {
        this.source.skipBytes($$0);
        return this;
    }

    public FriendlyByteBuf writeBoolean(boolean $$0) {
        this.source.writeBoolean($$0);
        return this;
    }

    public FriendlyByteBuf writeByte(int $$0) {
        this.source.writeByte($$0);
        return this;
    }

    public FriendlyByteBuf writeShort(int $$0) {
        this.source.writeShort($$0);
        return this;
    }

    public FriendlyByteBuf writeShortLE(int $$0) {
        this.source.writeShortLE($$0);
        return this;
    }

    public FriendlyByteBuf writeMedium(int $$0) {
        this.source.writeMedium($$0);
        return this;
    }

    public FriendlyByteBuf writeMediumLE(int $$0) {
        this.source.writeMediumLE($$0);
        return this;
    }

    public FriendlyByteBuf writeInt(int $$0) {
        this.source.writeInt($$0);
        return this;
    }

    public FriendlyByteBuf writeIntLE(int $$0) {
        this.source.writeIntLE($$0);
        return this;
    }

    public FriendlyByteBuf writeLong(long $$0) {
        this.source.writeLong($$0);
        return this;
    }

    public FriendlyByteBuf writeLongLE(long $$0) {
        this.source.writeLongLE($$0);
        return this;
    }

    public FriendlyByteBuf writeChar(int $$0) {
        this.source.writeChar($$0);
        return this;
    }

    public FriendlyByteBuf writeFloat(float $$0) {
        this.source.writeFloat($$0);
        return this;
    }

    public FriendlyByteBuf writeDouble(double $$0) {
        this.source.writeDouble($$0);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf $$0) {
        this.source.writeBytes($$0);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf $$0, int $$1) {
        this.source.writeBytes($$0, $$1);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuf $$0, int $$1, int $$2) {
        this.source.writeBytes($$0, $$1, $$2);
        return this;
    }

    public FriendlyByteBuf writeBytes(byte[] $$0) {
        this.source.writeBytes($$0);
        return this;
    }

    public FriendlyByteBuf writeBytes(byte[] $$0, int $$1, int $$2) {
        this.source.writeBytes($$0, $$1, $$2);
        return this;
    }

    public FriendlyByteBuf writeBytes(ByteBuffer $$0) {
        this.source.writeBytes($$0);
        return this;
    }

    public int writeBytes(InputStream $$0, int $$1) throws IOException {
        return this.source.writeBytes($$0, $$1);
    }

    public int writeBytes(ScatteringByteChannel $$0, int $$1) throws IOException {
        return this.source.writeBytes($$0, $$1);
    }

    public int writeBytes(FileChannel $$0, long $$1, int $$2) throws IOException {
        return this.source.writeBytes($$0, $$1, $$2);
    }

    public FriendlyByteBuf writeZero(int $$0) {
        this.source.writeZero($$0);
        return this;
    }

    public int writeCharSequence(CharSequence $$0, Charset $$1) {
        return this.source.writeCharSequence($$0, $$1);
    }

    public int indexOf(int $$0, int $$1, byte $$2) {
        return this.source.indexOf($$0, $$1, $$2);
    }

    public int bytesBefore(byte $$0) {
        return this.source.bytesBefore($$0);
    }

    public int bytesBefore(int $$0, byte $$1) {
        return this.source.bytesBefore($$0, $$1);
    }

    public int bytesBefore(int $$0, int $$1, byte $$2) {
        return this.source.bytesBefore($$0, $$1, $$2);
    }

    public int forEachByte(ByteProcessor $$0) {
        return this.source.forEachByte($$0);
    }

    public int forEachByte(int $$0, int $$1, ByteProcessor $$2) {
        return this.source.forEachByte($$0, $$1, $$2);
    }

    public int forEachByteDesc(ByteProcessor $$0) {
        return this.source.forEachByteDesc($$0);
    }

    public int forEachByteDesc(int $$0, int $$1, ByteProcessor $$2) {
        return this.source.forEachByteDesc($$0, $$1, $$2);
    }

    public ByteBuf copy() {
        return this.source.copy();
    }

    public ByteBuf copy(int $$0, int $$1) {
        return this.source.copy($$0, $$1);
    }

    public ByteBuf slice() {
        return this.source.slice();
    }

    public ByteBuf retainedSlice() {
        return this.source.retainedSlice();
    }

    public ByteBuf slice(int $$0, int $$1) {
        return this.source.slice($$0, $$1);
    }

    public ByteBuf retainedSlice(int $$0, int $$1) {
        return this.source.retainedSlice($$0, $$1);
    }

    public ByteBuf duplicate() {
        return this.source.duplicate();
    }

    public ByteBuf retainedDuplicate() {
        return this.source.retainedDuplicate();
    }

    public int nioBufferCount() {
        return this.source.nioBufferCount();
    }

    public ByteBuffer nioBuffer() {
        return this.source.nioBuffer();
    }

    public ByteBuffer nioBuffer(int $$0, int $$1) {
        return this.source.nioBuffer($$0, $$1);
    }

    public ByteBuffer internalNioBuffer(int $$0, int $$1) {
        return this.source.internalNioBuffer($$0, $$1);
    }

    public ByteBuffer[] nioBuffers() {
        return this.source.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int $$0, int $$1) {
        return this.source.nioBuffers($$0, $$1);
    }

    public boolean hasArray() {
        return this.source.hasArray();
    }

    public byte[] array() {
        return this.source.array();
    }

    public int arrayOffset() {
        return this.source.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return this.source.hasMemoryAddress();
    }

    public long memoryAddress() {
        return this.source.memoryAddress();
    }

    public String toString(Charset $$0) {
        return this.source.toString($$0);
    }

    public String toString(int $$0, int $$1, Charset $$2) {
        return this.source.toString($$0, $$1, $$2);
    }

    public int hashCode() {
        return this.source.hashCode();
    }

    public boolean equals(Object $$0) {
        return this.source.equals($$0);
    }

    public int compareTo(ByteBuf $$0) {
        return this.source.compareTo($$0);
    }

    public String toString() {
        return this.source.toString();
    }

    public FriendlyByteBuf retain(int $$0) {
        this.source.retain($$0);
        return this;
    }

    public FriendlyByteBuf retain() {
        this.source.retain();
        return this;
    }

    public FriendlyByteBuf touch() {
        this.source.touch();
        return this;
    }

    public FriendlyByteBuf touch(Object $$0) {
        this.source.touch($$0);
        return this;
    }

    public int refCnt() {
        return this.source.refCnt();
    }

    public boolean release() {
        return this.source.release();
    }

    public boolean release(int $$0) {
        return this.source.release($$0);
    }

    public /* synthetic */ ByteBuf touch(Object object) {
        return this.touch(object);
    }

    public /* synthetic */ ByteBuf touch() {
        return this.touch();
    }

    public /* synthetic */ ByteBuf retain() {
        return this.retain();
    }

    public /* synthetic */ ByteBuf retain(int n) {
        return this.retain(n);
    }

    public /* synthetic */ ByteBuf writeZero(int n) {
        return this.writeZero(n);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuffer byteBuffer) {
        return this.writeBytes(byteBuffer);
    }

    public /* synthetic */ ByteBuf writeBytes(byte[] byArray, int n, int n2) {
        return this.writeBytes(byArray, n, n2);
    }

    public /* synthetic */ ByteBuf writeBytes(byte[] byArray) {
        return this.writeBytes(byArray);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuf byteBuf, int n, int n2) {
        return this.writeBytes(byteBuf, n, n2);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuf byteBuf, int n) {
        return this.writeBytes(byteBuf, n);
    }

    public /* synthetic */ ByteBuf writeBytes(ByteBuf byteBuf) {
        return this.writeBytes(byteBuf);
    }

    public /* synthetic */ ByteBuf writeDouble(double d) {
        return this.writeDouble(d);
    }

    public /* synthetic */ ByteBuf writeFloat(float f) {
        return this.writeFloat(f);
    }

    public /* synthetic */ ByteBuf writeChar(int n) {
        return this.writeChar(n);
    }

    public /* synthetic */ ByteBuf writeLongLE(long l) {
        return this.writeLongLE(l);
    }

    public /* synthetic */ ByteBuf writeLong(long l) {
        return this.writeLong(l);
    }

    public /* synthetic */ ByteBuf writeIntLE(int n) {
        return this.writeIntLE(n);
    }

    public /* synthetic */ ByteBuf writeInt(int n) {
        return this.writeInt(n);
    }

    public /* synthetic */ ByteBuf writeMediumLE(int n) {
        return this.writeMediumLE(n);
    }

    public /* synthetic */ ByteBuf writeMedium(int n) {
        return this.writeMedium(n);
    }

    public /* synthetic */ ByteBuf writeShortLE(int n) {
        return this.writeShortLE(n);
    }

    public /* synthetic */ ByteBuf writeShort(int n) {
        return this.writeShort(n);
    }

    public /* synthetic */ ByteBuf writeByte(int n) {
        return this.writeByte(n);
    }

    public /* synthetic */ ByteBuf writeBoolean(boolean bl) {
        return this.writeBoolean(bl);
    }

    public /* synthetic */ ByteBuf skipBytes(int n) {
        return this.skipBytes(n);
    }

    public /* synthetic */ ByteBuf readBytes(OutputStream outputStream, int n) throws IOException {
        return this.readBytes(outputStream, n);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuffer byteBuffer) {
        return this.readBytes(byteBuffer);
    }

    public /* synthetic */ ByteBuf readBytes(byte[] byArray, int n, int n2) {
        return this.readBytes(byArray, n, n2);
    }

    public /* synthetic */ ByteBuf readBytes(byte[] byArray) {
        return this.readBytes(byArray);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuf byteBuf, int n, int n2) {
        return this.readBytes(byteBuf, n, n2);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuf byteBuf, int n) {
        return this.readBytes(byteBuf, n);
    }

    public /* synthetic */ ByteBuf readBytes(ByteBuf byteBuf) {
        return this.readBytes(byteBuf);
    }

    public /* synthetic */ ByteBuf setZero(int n, int n2) {
        return this.setZero(n, n2);
    }

    public /* synthetic */ ByteBuf setBytes(int n, ByteBuffer byteBuffer) {
        return this.setBytes(n, byteBuffer);
    }

    public /* synthetic */ ByteBuf setBytes(int n, byte[] byArray, int n2, int n3) {
        return this.setBytes(n, byArray, n2, n3);
    }

    public /* synthetic */ ByteBuf setBytes(int n, byte[] byArray) {
        return this.setBytes(n, byArray);
    }

    public /* synthetic */ ByteBuf setBytes(int n, ByteBuf byteBuf, int n2, int n3) {
        return this.setBytes(n, byteBuf, n2, n3);
    }

    public /* synthetic */ ByteBuf setBytes(int n, ByteBuf byteBuf, int n2) {
        return this.setBytes(n, byteBuf, n2);
    }

    public /* synthetic */ ByteBuf setBytes(int n, ByteBuf byteBuf) {
        return this.setBytes(n, byteBuf);
    }

    public /* synthetic */ ByteBuf setDouble(int n, double d) {
        return this.setDouble(n, d);
    }

    public /* synthetic */ ByteBuf setFloat(int n, float f) {
        return this.setFloat(n, f);
    }

    public /* synthetic */ ByteBuf setChar(int n, int n2) {
        return this.setChar(n, n2);
    }

    public /* synthetic */ ByteBuf setLongLE(int n, long l) {
        return this.setLongLE(n, l);
    }

    public /* synthetic */ ByteBuf setLong(int n, long l) {
        return this.setLong(n, l);
    }

    public /* synthetic */ ByteBuf setIntLE(int n, int n2) {
        return this.setIntLE(n, n2);
    }

    public /* synthetic */ ByteBuf setInt(int n, int n2) {
        return this.setInt(n, n2);
    }

    public /* synthetic */ ByteBuf setMediumLE(int n, int n2) {
        return this.setMediumLE(n, n2);
    }

    public /* synthetic */ ByteBuf setMedium(int n, int n2) {
        return this.setMedium(n, n2);
    }

    public /* synthetic */ ByteBuf setShortLE(int n, int n2) {
        return this.setShortLE(n, n2);
    }

    public /* synthetic */ ByteBuf setShort(int n, int n2) {
        return this.setShort(n, n2);
    }

    public /* synthetic */ ByteBuf setByte(int n, int n2) {
        return this.setByte(n, n2);
    }

    public /* synthetic */ ByteBuf setBoolean(int n, boolean bl) {
        return this.setBoolean(n, bl);
    }

    public /* synthetic */ ByteBuf getBytes(int n, OutputStream outputStream, int n2) throws IOException {
        return this.getBytes(n, outputStream, n2);
    }

    public /* synthetic */ ByteBuf getBytes(int n, ByteBuffer byteBuffer) {
        return this.getBytes(n, byteBuffer);
    }

    public /* synthetic */ ByteBuf getBytes(int n, byte[] byArray, int n2, int n3) {
        return this.getBytes(n, byArray, n2, n3);
    }

    public /* synthetic */ ByteBuf getBytes(int n, byte[] byArray) {
        return this.getBytes(n, byArray);
    }

    public /* synthetic */ ByteBuf getBytes(int n, ByteBuf byteBuf, int n2, int n3) {
        return this.getBytes(n, byteBuf, n2, n3);
    }

    public /* synthetic */ ByteBuf getBytes(int n, ByteBuf byteBuf, int n2) {
        return this.getBytes(n, byteBuf, n2);
    }

    public /* synthetic */ ByteBuf getBytes(int n, ByteBuf byteBuf) {
        return this.getBytes(n, byteBuf);
    }

    public /* synthetic */ ByteBuf ensureWritable(int n) {
        return this.ensureWritable(n);
    }

    public /* synthetic */ ByteBuf discardSomeReadBytes() {
        return this.discardSomeReadBytes();
    }

    public /* synthetic */ ByteBuf discardReadBytes() {
        return this.discardReadBytes();
    }

    public /* synthetic */ ByteBuf resetWriterIndex() {
        return this.resetWriterIndex();
    }

    public /* synthetic */ ByteBuf markWriterIndex() {
        return this.markWriterIndex();
    }

    public /* synthetic */ ByteBuf resetReaderIndex() {
        return this.resetReaderIndex();
    }

    public /* synthetic */ ByteBuf markReaderIndex() {
        return this.markReaderIndex();
    }

    public /* synthetic */ ByteBuf clear() {
        return this.clear();
    }

    public /* synthetic */ ByteBuf setIndex(int n, int n2) {
        return this.setIndex(n, n2);
    }

    public /* synthetic */ ByteBuf writerIndex(int n) {
        return this.writerIndex(n);
    }

    public /* synthetic */ ByteBuf readerIndex(int n) {
        return this.readerIndex(n);
    }

    public /* synthetic */ ByteBuf capacity(int n) {
        return this.capacity(n);
    }

    public /* synthetic */ ReferenceCounted touch(Object object) {
        return this.touch(object);
    }

    public /* synthetic */ ReferenceCounted touch() {
        return this.touch();
    }

    public /* synthetic */ ReferenceCounted retain(int n) {
        return this.retain(n);
    }

    public /* synthetic */ ReferenceCounted retain() {
        return this.retain();
    }
}

