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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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

public class FriendlyByteBuf extends ByteBuf {
   private final ByteBuf source;
   public static final short MAX_STRING_LENGTH = 32767;
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
   public <T> T readWithCodec(DynamicOps<Tag> $$0, Codec<T> $$1, NbtAccounter $$2) {
      Tag $$3 = this.readNbt($$2);
      return (T)$$1.parse($$0, $$3).getOrThrow($$1x -> new DecoderException("Failed to decode: " + $$1x + " " + $$3));
   }

   @Deprecated
   public <T> net.minecraft.network.FriendlyByteBuf writeWithCodec(DynamicOps<Tag> $$0, Codec<T> $$1, T $$2) {
      Tag $$3 = (Tag)$$1.encodeStart($$0, $$2).getOrThrow($$1x -> new EncoderException("Failed to encode: " + $$1x + " " + $$2));
      this.writeNbt($$3);
      return this;
   }

   public <T> T readLenientJsonWithCodec(Codec<T> $$0) {
      JsonElement $$1 = LenientJsonParser.parse(this.readUtf());
      DataResult<T> $$2 = $$0.parse(JsonOps.INSTANCE, $$1);
      return (T)$$2.getOrThrow($$0x -> new DecoderException("Failed to decode JSON: " + $$0x));
   }

   public <T> void writeJsonWithCodec(Codec<T> $$0, T $$1) {
      DataResult<JsonElement> $$2 = $$0.encodeStart(JsonOps.INSTANCE, $$1);
      this.writeUtf(GSON.toJson((JsonElement)$$2.getOrThrow($$1x -> new EncoderException("Failed to encode: " + $$1x + " " + $$1))));
   }

   public static <T> IntFunction<T> limitValue(IntFunction<T> $$0, int $$1) {
      return $$2 -> {
         if ($$2 > $$1) {
            throw new DecoderException("Value " + $$2 + " is larger than limit " + $$1);
         } else {
            return $$0.apply($$2);
         }
      };
   }

   public <T, C extends Collection<T>> C readCollection(IntFunction<C> $$0, StreamDecoder<? super net.minecraft.network.FriendlyByteBuf, T> $$1) {
      int $$2 = this.readVarInt();
      C $$3 = (C)$$0.apply($$2);

      for (int $$4 = 0; $$4 < $$2; $$4++) {
         $$3.add($$1.decode(this));
      }

      return $$3;
   }

   public <T> void writeCollection(Collection<T> $$0, StreamEncoder<? super net.minecraft.network.FriendlyByteBuf, T> $$1) {
      this.writeVarInt($$0.size());

      for (T $$2 : $$0) {
         $$1.encode(this, $$2);
      }
   }

   public <T> List<T> readList(StreamDecoder<? super net.minecraft.network.FriendlyByteBuf, T> $$0) {
      return this.readCollection(Lists::newArrayListWithCapacity, $$0);
   }

   public IntList readIntIdList() {
      int $$0 = this.readVarInt();
      IntList $$1 = new IntArrayList();

      for (int $$2 = 0; $$2 < $$0; $$2++) {
         $$1.add(this.readVarInt());
      }

      return $$1;
   }

   public void writeIntIdList(IntList $$0) {
      this.writeVarInt($$0.size());
      $$0.forEach(this::writeVarInt);
   }

   public <K, V, M extends Map<K, V>> M readMap(
      IntFunction<M> $$0,
      StreamDecoder<? super net.minecraft.network.FriendlyByteBuf, K> $$1,
      StreamDecoder<? super net.minecraft.network.FriendlyByteBuf, V> $$2
   ) {
      int $$3 = this.readVarInt();
      M $$4 = (M)$$0.apply($$3);

      for (int $$5 = 0; $$5 < $$3; $$5++) {
         K $$6 = $$1.decode(this);
         V $$7 = $$2.decode(this);
         $$4.put($$6, $$7);
      }

      return $$4;
   }

   public <K, V> Map<K, V> readMap(
      StreamDecoder<? super net.minecraft.network.FriendlyByteBuf, K> $$0, StreamDecoder<? super net.minecraft.network.FriendlyByteBuf, V> $$1
   ) {
      return this.readMap(Maps::newHashMapWithExpectedSize, $$0, $$1);
   }

   public <K, V> void writeMap(
      Map<K, V> $$0, StreamEncoder<? super net.minecraft.network.FriendlyByteBuf, K> $$1, StreamEncoder<? super net.minecraft.network.FriendlyByteBuf, V> $$2
   ) {
      this.writeVarInt($$0.size());
      $$0.forEach(($$2x, $$3) -> {
         $$1.encode(this, (K)$$2x);
         $$2.encode(this, (V)$$3);
      });
   }

   public void readWithCount(Consumer<net.minecraft.network.FriendlyByteBuf> $$0) {
      int $$1 = this.readVarInt();

      for (int $$2 = 0; $$2 < $$1; $$2++) {
         $$0.accept(this);
      }
   }

   public <E extends Enum<E>> void writeEnumSet(EnumSet<E> $$0, Class<E> $$1) {
      E[] $$2 = (E[])$$1.getEnumConstants();
      BitSet $$3 = new BitSet($$2.length);

      for (int $$4 = 0; $$4 < $$2.length; $$4++) {
         $$3.set($$4, $$0.contains($$2[$$4]));
      }

      this.writeFixedBitSet($$3, $$2.length);
   }

   public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> $$0) {
      E[] $$1 = (E[])$$0.getEnumConstants();
      BitSet $$2 = this.readFixedBitSet($$1.length);
      EnumSet<E> $$3 = EnumSet.noneOf($$0);

      for (int $$4 = 0; $$4 < $$1.length; $$4++) {
         if ($$2.get($$4)) {
            $$3.add($$1[$$4]);
         }
      }

      return $$3;
   }

   public <T> void writeOptional(Optional<T> $$0, StreamEncoder<? super net.minecraft.network.FriendlyByteBuf, T> $$1) {
      if ($$0.isPresent()) {
         this.writeBoolean(true);
         $$1.encode(this, $$0.get());
      } else {
         this.writeBoolean(false);
      }
   }

   public <T> Optional<T> readOptional(StreamDecoder<? super net.minecraft.network.FriendlyByteBuf, T> $$0) {
      return this.readBoolean() ? Optional.of($$0.decode(this)) : Optional.empty();
   }

   public <L, R> void writeEither(
      Either<L, R> $$0,
      StreamEncoder<? super net.minecraft.network.FriendlyByteBuf, L> $$1,
      StreamEncoder<? super net.minecraft.network.FriendlyByteBuf, R> $$2
   ) {
      $$0.ifLeft($$1x -> {
         this.writeBoolean(true);
         $$1.encode(this, (L)$$1x);
      }).ifRight($$1x -> {
         this.writeBoolean(false);
         $$2.encode(this, (R)$$1x);
      });
   }

   public <L, R> Either<L, R> readEither(
      StreamDecoder<? super net.minecraft.network.FriendlyByteBuf, L> $$0, StreamDecoder<? super net.minecraft.network.FriendlyByteBuf, R> $$1
   ) {
      return this.readBoolean() ? Either.left($$0.decode(this)) : Either.right($$1.decode(this));
   }

   @Nullable
   public <T> T readNullable(StreamDecoder<? super net.minecraft.network.FriendlyByteBuf, T> $$0) {
      return readNullable(this, $$0);
   }

   @Nullable
   public static <T, B extends ByteBuf> T readNullable(B $$0, StreamDecoder<? super B, T> $$1) {
      return $$0.readBoolean() ? $$1.decode($$0) : null;
   }

   public <T> void writeNullable(@Nullable T $$0, StreamEncoder<? super net.minecraft.network.FriendlyByteBuf, T> $$1) {
      writeNullable(this, $$0, $$1);
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
      return readByteArray(this);
   }

   public static byte[] readByteArray(ByteBuf $$0) {
      return readByteArray($$0, $$0.readableBytes());
   }

   public net.minecraft.network.FriendlyByteBuf writeByteArray(byte[] $$0) {
      writeByteArray(this, $$0);
      return this;
   }

   public static void writeByteArray(ByteBuf $$0, byte[] $$1) {
      net.minecraft.network.VarInt.write($$0, $$1.length);
      $$0.writeBytes($$1);
   }

   public byte[] readByteArray(int $$0) {
      return readByteArray(this, $$0);
   }

   public static byte[] readByteArray(ByteBuf $$0, int $$1) {
      int $$2 = net.minecraft.network.VarInt.read($$0);
      if ($$2 > $$1) {
         throw new DecoderException("ByteArray with size " + $$2 + " is bigger than allowed " + $$1);
      } else {
         byte[] $$3 = new byte[$$2];
         $$0.readBytes($$3);
         return $$3;
      }
   }

   public net.minecraft.network.FriendlyByteBuf writeVarIntArray(int[] $$0) {
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
      } else {
         int[] $$2 = new int[$$1];

         for (int $$3 = 0; $$3 < $$2.length; $$3++) {
            $$2[$$3] = this.readVarInt();
         }

         return $$2;
      }
   }

   public net.minecraft.network.FriendlyByteBuf writeLongArray(long[] $$0) {
      writeLongArray(this, $$0);
      return this;
   }

   public static void writeLongArray(ByteBuf $$0, long[] $$1) {
      net.minecraft.network.VarInt.write($$0, $$1.length);
      writeFixedSizeLongArray($$0, $$1);
   }

   public net.minecraft.network.FriendlyByteBuf writeFixedSizeLongArray(long[] $$0) {
      writeFixedSizeLongArray(this, $$0);
      return this;
   }

   public static void writeFixedSizeLongArray(ByteBuf $$0, long[] $$1) {
      for (long $$2 : $$1) {
         $$0.writeLong($$2);
      }
   }

   public long[] readLongArray() {
      return readLongArray(this);
   }

   public long[] readFixedSizeLongArray(long[] $$0) {
      return readFixedSizeLongArray(this, $$0);
   }

   public static long[] readLongArray(ByteBuf $$0) {
      int $$1 = net.minecraft.network.VarInt.read($$0);
      int $$2 = $$0.readableBytes() / 8;
      if ($$1 > $$2) {
         throw new DecoderException("LongArray with size " + $$1 + " is bigger than allowed " + $$2);
      } else {
         return readFixedSizeLongArray($$0, new long[$$1]);
      }
   }

   public static long[] readFixedSizeLongArray(ByteBuf $$0, long[] $$1) {
      for (int $$2 = 0; $$2 < $$1.length; $$2++) {
         $$1[$$2] = $$0.readLong();
      }

      return $$1;
   }

   public BlockPos readBlockPos() {
      return readBlockPos(this);
   }

   public static BlockPos readBlockPos(ByteBuf $$0) {
      return BlockPos.of($$0.readLong());
   }

   public net.minecraft.network.FriendlyByteBuf writeBlockPos(BlockPos $$0) {
      writeBlockPos(this, $$0);
      return this;
   }

   public static void writeBlockPos(ByteBuf $$0, BlockPos $$1) {
      $$0.writeLong($$1.asLong());
   }

   public ChunkPos readChunkPos() {
      return new ChunkPos(this.readLong());
   }

   public net.minecraft.network.FriendlyByteBuf writeChunkPos(ChunkPos $$0) {
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
      return readVector3f(this);
   }

   public static Vector3f readVector3f(ByteBuf $$0) {
      return new Vector3f($$0.readFloat(), $$0.readFloat(), $$0.readFloat());
   }

   public void writeVector3f(Vector3f $$0) {
      writeVector3f(this, $$0);
   }

   public static void writeVector3f(ByteBuf $$0, Vector3fc $$1) {
      $$0.writeFloat($$1.x());
      $$0.writeFloat($$1.y());
      $$0.writeFloat($$1.z());
   }

   public Quaternionf readQuaternion() {
      return readQuaternion(this);
   }

   public static Quaternionf readQuaternion(ByteBuf $$0) {
      return new Quaternionf($$0.readFloat(), $$0.readFloat(), $$0.readFloat(), $$0.readFloat());
   }

   public void writeQuaternion(Quaternionf $$0) {
      writeQuaternion(this, $$0);
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
      return readVec3(this);
   }

   public static void writeVec3(ByteBuf $$0, Vec3 $$1) {
      $$0.writeDouble($$1.x());
      $$0.writeDouble($$1.y());
      $$0.writeDouble($$1.z());
   }

   public void writeVec3(Vec3 $$0) {
      writeVec3(this, $$0);
   }

   public Vec3 readLpVec3() {
      return net.minecraft.network.LpVec3.read(this);
   }

   public void writeLpVec3(Vec3 $$0) {
      net.minecraft.network.LpVec3.write(this, $$0);
   }

   public <T extends Enum<T>> T readEnum(Class<T> $$0) {
      return $$0.getEnumConstants()[this.readVarInt()];
   }

   public net.minecraft.network.FriendlyByteBuf writeEnum(Enum<?> $$0) {
      return this.writeVarInt($$0.ordinal());
   }

   public <T> T readById(IntFunction<T> $$0) {
      int $$1 = this.readVarInt();
      return $$0.apply($$1);
   }

   public <T> net.minecraft.network.FriendlyByteBuf writeById(ToIntFunction<T> $$0, T $$1) {
      int $$2 = $$0.applyAsInt($$1);
      return this.writeVarInt($$2);
   }

   public int readVarInt() {
      return net.minecraft.network.VarInt.read(this.source);
   }

   public long readVarLong() {
      return net.minecraft.network.VarLong.read(this.source);
   }

   public net.minecraft.network.FriendlyByteBuf writeUUID(UUID $$0) {
      writeUUID(this, $$0);
      return this;
   }

   public static void writeUUID(ByteBuf $$0, UUID $$1) {
      $$0.writeLong($$1.getMostSignificantBits());
      $$0.writeLong($$1.getLeastSignificantBits());
   }

   public UUID readUUID() {
      return readUUID(this);
   }

   public static UUID readUUID(ByteBuf $$0) {
      return new UUID($$0.readLong(), $$0.readLong());
   }

   public net.minecraft.network.FriendlyByteBuf writeVarInt(int $$0) {
      net.minecraft.network.VarInt.write(this.source, $$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeVarLong(long $$0) {
      net.minecraft.network.VarLong.write(this.source, $$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeNbt(@Nullable Tag $$0) {
      writeNbt(this, $$0);
      return this;
   }

   public static void writeNbt(ByteBuf $$0, @Nullable Tag $$1) {
      if ($$1 == null) {
         $$1 = EndTag.INSTANCE;
      }

      try {
         NbtIo.writeAnyTag($$1, new ByteBufOutputStream($$0));
      } catch (IOException var3) {
         throw new EncoderException(var3);
      }
   }

   @Nullable
   public CompoundTag readNbt() {
      return readNbt(this);
   }

   @Nullable
   public static CompoundTag readNbt(ByteBuf $$0) {
      Tag $$1 = readNbt($$0, NbtAccounter.defaultQuota());
      if ($$1 != null && !($$1 instanceof CompoundTag)) {
         throw new DecoderException("Not a compound tag: " + $$1);
      } else {
         return (CompoundTag)$$1;
      }
   }

   @Nullable
   public static Tag readNbt(ByteBuf $$0, NbtAccounter $$1) {
      try {
         Tag $$2 = NbtIo.readAnyTag(new ByteBufInputStream($$0), $$1);
         return $$2.getId() == 0 ? null : $$2;
      } catch (IOException var3) {
         throw new EncoderException(var3);
      }
   }

   @Nullable
   public Tag readNbt(NbtAccounter $$0) {
      return readNbt(this, $$0);
   }

   public String readUtf() {
      return this.readUtf(32767);
   }

   public String readUtf(int $$0) {
      return net.minecraft.network.Utf8String.read(this.source, $$0);
   }

   public net.minecraft.network.FriendlyByteBuf writeUtf(String $$0) {
      return this.writeUtf($$0, 32767);
   }

   public net.minecraft.network.FriendlyByteBuf writeUtf(String $$0, int $$1) {
      net.minecraft.network.Utf8String.write(this.source, $$0, $$1);
      return this;
   }

   public Identifier readIdentifier() {
      return Identifier.parse(this.readUtf(32767));
   }

   public net.minecraft.network.FriendlyByteBuf writeIdentifier(Identifier $$0) {
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
      } catch (CryptException var2) {
         throw new DecoderException("Malformed public key bytes", var2);
      }
   }

   public net.minecraft.network.FriendlyByteBuf writePublicKey(PublicKey $$0) {
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
      return new BlockHitResult(new Vec3((double)$$0.getX() + $$2, (double)$$0.getY() + $$3, (double)$$0.getZ() + $$4), $$1, $$0, $$5, $$6);
   }

   public void writeBlockHitResult(BlockHitResult $$0) {
      BlockPos $$1 = $$0.getBlockPos();
      this.writeBlockPos($$1);
      this.writeEnum($$0.getDirection());
      Vec3 $$2 = $$0.getLocation();
      this.writeFloat((float)($$2.x - $$1.getX()));
      this.writeFloat((float)($$2.y - $$1.getY()));
      this.writeFloat((float)($$2.z - $$1.getZ()));
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
      } else {
         byte[] $$2 = $$0.toByteArray();
         this.writeBytes(Arrays.copyOf($$2, Mth.positiveCeilDiv($$1, 8)));
      }
   }

   public static int readContainerId(ByteBuf $$0) {
      return net.minecraft.network.VarInt.read($$0);
   }

   public int readContainerId() {
      return readContainerId(this.source);
   }

   public static void writeContainerId(ByteBuf $$0, int $$1) {
      net.minecraft.network.VarInt.write($$0, $$1);
   }

   public void writeContainerId(int $$0) {
      writeContainerId(this.source, $$0);
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

   public net.minecraft.network.FriendlyByteBuf capacity(int $$0) {
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

   public net.minecraft.network.FriendlyByteBuf readerIndex(int $$0) {
      this.source.readerIndex($$0);
      return this;
   }

   public int writerIndex() {
      return this.source.writerIndex();
   }

   public net.minecraft.network.FriendlyByteBuf writerIndex(int $$0) {
      this.source.writerIndex($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setIndex(int $$0, int $$1) {
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

   public net.minecraft.network.FriendlyByteBuf clear() {
      this.source.clear();
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf markReaderIndex() {
      this.source.markReaderIndex();
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf resetReaderIndex() {
      this.source.resetReaderIndex();
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf markWriterIndex() {
      this.source.markWriterIndex();
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf resetWriterIndex() {
      this.source.resetWriterIndex();
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf discardReadBytes() {
      this.source.discardReadBytes();
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf discardSomeReadBytes() {
      this.source.discardSomeReadBytes();
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf ensureWritable(int $$0) {
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

   public net.minecraft.network.FriendlyByteBuf getBytes(int $$0, ByteBuf $$1) {
      this.source.getBytes($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf getBytes(int $$0, ByteBuf $$1, int $$2) {
      this.source.getBytes($$0, $$1, $$2);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf getBytes(int $$0, ByteBuf $$1, int $$2, int $$3) {
      this.source.getBytes($$0, $$1, $$2, $$3);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf getBytes(int $$0, byte[] $$1) {
      this.source.getBytes($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf getBytes(int $$0, byte[] $$1, int $$2, int $$3) {
      this.source.getBytes($$0, $$1, $$2, $$3);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf getBytes(int $$0, ByteBuffer $$1) {
      this.source.getBytes($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf getBytes(int $$0, OutputStream $$1, int $$2) throws IOException {
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

   public net.minecraft.network.FriendlyByteBuf setBoolean(int $$0, boolean $$1) {
      this.source.setBoolean($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setByte(int $$0, int $$1) {
      this.source.setByte($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setShort(int $$0, int $$1) {
      this.source.setShort($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setShortLE(int $$0, int $$1) {
      this.source.setShortLE($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setMedium(int $$0, int $$1) {
      this.source.setMedium($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setMediumLE(int $$0, int $$1) {
      this.source.setMediumLE($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setInt(int $$0, int $$1) {
      this.source.setInt($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setIntLE(int $$0, int $$1) {
      this.source.setIntLE($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setLong(int $$0, long $$1) {
      this.source.setLong($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setLongLE(int $$0, long $$1) {
      this.source.setLongLE($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setChar(int $$0, int $$1) {
      this.source.setChar($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setFloat(int $$0, float $$1) {
      this.source.setFloat($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setDouble(int $$0, double $$1) {
      this.source.setDouble($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setBytes(int $$0, ByteBuf $$1) {
      this.source.setBytes($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setBytes(int $$0, ByteBuf $$1, int $$2) {
      this.source.setBytes($$0, $$1, $$2);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setBytes(int $$0, ByteBuf $$1, int $$2, int $$3) {
      this.source.setBytes($$0, $$1, $$2, $$3);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setBytes(int $$0, byte[] $$1) {
      this.source.setBytes($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setBytes(int $$0, byte[] $$1, int $$2, int $$3) {
      this.source.setBytes($$0, $$1, $$2, $$3);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf setBytes(int $$0, ByteBuffer $$1) {
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

   public net.minecraft.network.FriendlyByteBuf setZero(int $$0, int $$1) {
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

   public net.minecraft.network.FriendlyByteBuf readBytes(ByteBuf $$0) {
      this.source.readBytes($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf readBytes(ByteBuf $$0, int $$1) {
      this.source.readBytes($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf readBytes(ByteBuf $$0, int $$1, int $$2) {
      this.source.readBytes($$0, $$1, $$2);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf readBytes(byte[] $$0) {
      this.source.readBytes($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf readBytes(byte[] $$0, int $$1, int $$2) {
      this.source.readBytes($$0, $$1, $$2);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf readBytes(ByteBuffer $$0) {
      this.source.readBytes($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf readBytes(OutputStream $$0, int $$1) throws IOException {
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

   public net.minecraft.network.FriendlyByteBuf skipBytes(int $$0) {
      this.source.skipBytes($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeBoolean(boolean $$0) {
      this.source.writeBoolean($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeByte(int $$0) {
      this.source.writeByte($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeShort(int $$0) {
      this.source.writeShort($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeShortLE(int $$0) {
      this.source.writeShortLE($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeMedium(int $$0) {
      this.source.writeMedium($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeMediumLE(int $$0) {
      this.source.writeMediumLE($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeInt(int $$0) {
      this.source.writeInt($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeIntLE(int $$0) {
      this.source.writeIntLE($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeLong(long $$0) {
      this.source.writeLong($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeLongLE(long $$0) {
      this.source.writeLongLE($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeChar(int $$0) {
      this.source.writeChar($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeFloat(float $$0) {
      this.source.writeFloat($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeDouble(double $$0) {
      this.source.writeDouble($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeBytes(ByteBuf $$0) {
      this.source.writeBytes($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeBytes(ByteBuf $$0, int $$1) {
      this.source.writeBytes($$0, $$1);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeBytes(ByteBuf $$0, int $$1, int $$2) {
      this.source.writeBytes($$0, $$1, $$2);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeBytes(byte[] $$0) {
      this.source.writeBytes($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeBytes(byte[] $$0, int $$1, int $$2) {
      this.source.writeBytes($$0, $$1, $$2);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf writeBytes(ByteBuffer $$0) {
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

   public net.minecraft.network.FriendlyByteBuf writeZero(int $$0) {
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

   public net.minecraft.network.FriendlyByteBuf retain(int $$0) {
      this.source.retain($$0);
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf retain() {
      this.source.retain();
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf touch() {
      this.source.touch();
      return this;
   }

   public net.minecraft.network.FriendlyByteBuf touch(Object $$0) {
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
}
