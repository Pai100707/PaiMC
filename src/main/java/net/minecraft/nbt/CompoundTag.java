package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import org.slf4j.Logger;

public final class CompoundTag implements net.minecraft.nbt.Tag {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<net.minecraft.nbt.CompoundTag> CODEC = Codec.PASSTHROUGH
      .comapFlatMap(
         $$0 -> {
            net.minecraft.nbt.Tag $$1 = (net.minecraft.nbt.Tag)$$0.convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue();
            return $$1 instanceof net.minecraft.nbt.CompoundTag $$2
               ? DataResult.success($$2 == $$0.getValue() ? $$2.copy() : $$2)
               : DataResult.error(() -> "Not a compound tag: " + $$1);
         },
         $$0 -> new Dynamic(net.minecraft.nbt.NbtOps.INSTANCE, $$0.copy())
      );
   private static final int SELF_SIZE_IN_BYTES = 48;
   private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
   public static final net.minecraft.nbt.TagType<net.minecraft.nbt.CompoundTag> TYPE = new net.minecraft.nbt.TagType.VariableSize<net.minecraft.nbt.CompoundTag>() {
      public net.minecraft.nbt.CompoundTag load(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$1.pushDepth();

         net.minecraft.nbt.CompoundTag var3;
         try {
            var3 = loadCompound($$0, $$1);
         } finally {
            $$1.popDepth();
         }

         return var3;
      }

      private static net.minecraft.nbt.CompoundTag loadCompound(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$1.accountBytes(48L);
         Map<String, net.minecraft.nbt.Tag> $$2 = Maps.newHashMap();

         byte $$3;
         while (($$3 = $$0.readByte()) != 0) {
            String $$4 = readString($$0, $$1);
            net.minecraft.nbt.Tag $$5 = net.minecraft.nbt.CompoundTag.readNamedTagData(net.minecraft.nbt.TagTypes.getType($$3), $$4, $$0, $$1);
            if ($$2.put($$4, $$5) == null) {
               $$1.accountBytes(36L);
            }
         }

         return new net.minecraft.nbt.CompoundTag($$2);
      }

      @Override
      public net.minecraft.nbt.StreamTagVisitor.ValueResult parse(DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
         $$2.pushDepth();

         net.minecraft.nbt.StreamTagVisitor.ValueResult var4;
         try {
            var4 = parseCompound($$0, $$1, $$2);
         } finally {
            $$2.popDepth();
         }

         return var4;
      }

      private static net.minecraft.nbt.StreamTagVisitor.ValueResult parseCompound(
         DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2
      ) throws IOException {
         $$2.accountBytes(48L);

         byte $$3;
         label35:
         while (($$3 = $$0.readByte()) != 0) {
            net.minecraft.nbt.TagType<?> $$4 = net.minecraft.nbt.TagTypes.getType($$3);
            switch ($$1.visitEntry($$4)) {
               case HALT:
                  return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
               case BREAK:
                  net.minecraft.nbt.StringTag.skipString($$0);
                  $$4.skip($$0, $$2);
                  break label35;
               case SKIP:
                  net.minecraft.nbt.StringTag.skipString($$0);
                  $$4.skip($$0, $$2);
                  break;
               default:
                  String $$5 = readString($$0, $$2);
                  switch ($$1.visitEntry($$4, $$5)) {
                     case HALT:
                        return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
                     case BREAK:
                        $$4.skip($$0, $$2);
                        break label35;
                     case SKIP:
                        $$4.skip($$0, $$2);
                        break;
                     default:
                        $$2.accountBytes(36L);
                        switch ($$4.parse($$0, $$1, $$2)) {
                           case HALT:
                              return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
                           case BREAK:
                        }
                  }
            }
         }

         if ($$3 != 0) {
            while (($$3 = $$0.readByte()) != 0) {
               net.minecraft.nbt.StringTag.skipString($$0);
               net.minecraft.nbt.TagTypes.getType($$3).skip($$0, $$2);
            }
         }

         return $$1.visitContainerEnd();
      }

      private static String readString(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         String $$2 = $$0.readUTF();
         $$1.accountBytes(28L);
         $$1.accountBytes(2L, $$2.length());
         return $$2;
      }

      @Override
      public void skip(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$1.pushDepth();

         byte $$2;
         try {
            while (($$2 = $$0.readByte()) != 0) {
               net.minecraft.nbt.StringTag.skipString($$0);
               net.minecraft.nbt.TagTypes.getType($$2).skip($$0, $$1);
            }
         } finally {
            $$1.popDepth();
         }
      }

      @Override
      public String getName() {
         return "COMPOUND";
      }

      @Override
      public String getPrettyName() {
         return "TAG_Compound";
      }
   };
   private final Map<String, net.minecraft.nbt.Tag> tags;

   CompoundTag(Map<String, net.minecraft.nbt.Tag> $$0) {
      this.tags = $$0;
   }

   public CompoundTag() {
      this(new HashMap<>());
   }

   @Override
   public void write(DataOutput $$0) throws IOException {
      for (String $$1 : this.tags.keySet()) {
         net.minecraft.nbt.Tag $$2 = this.tags.get($$1);
         writeNamedTag($$1, $$2, $$0);
      }

      $$0.writeByte(0);
   }

   @Override
   public int sizeInBytes() {
      int $$0 = 48;

      for (Entry<String, net.minecraft.nbt.Tag> $$1 : this.tags.entrySet()) {
         $$0 += 28 + 2 * $$1.getKey().length();
         $$0 += 36;
         $$0 += $$1.getValue().sizeInBytes();
      }

      return $$0;
   }

   public Set<String> keySet() {
      return this.tags.keySet();
   }

   public Set<Entry<String, net.minecraft.nbt.Tag>> entrySet() {
      return this.tags.entrySet();
   }

   public Collection<net.minecraft.nbt.Tag> values() {
      return this.tags.values();
   }

   public void forEach(BiConsumer<String, net.minecraft.nbt.Tag> $$0) {
      this.tags.forEach($$0);
   }

   @Override
   public byte getId() {
      return 10;
   }

   @Override
   public net.minecraft.nbt.TagType<net.minecraft.nbt.CompoundTag> getType() {
      return TYPE;
   }

   public int size() {
      return this.tags.size();
   }

   
   public net.minecraft.nbt.Tag put(String $$0, net.minecraft.nbt.Tag $$1) {
      return this.tags.put($$0, $$1);
   }

   public void putByte(String $$0, byte $$1) {
      this.tags.put($$0, net.minecraft.nbt.ByteTag.valueOf($$1));
   }

   public void putShort(String $$0, short $$1) {
      this.tags.put($$0, net.minecraft.nbt.ShortTag.valueOf($$1));
   }

   public void putInt(String $$0, int $$1) {
      this.tags.put($$0, net.minecraft.nbt.IntTag.valueOf($$1));
   }

   public void putLong(String $$0, long $$1) {
      this.tags.put($$0, net.minecraft.nbt.LongTag.valueOf($$1));
   }

   public void putFloat(String $$0, float $$1) {
      this.tags.put($$0, net.minecraft.nbt.FloatTag.valueOf($$1));
   }

   public void putDouble(String $$0, double $$1) {
      this.tags.put($$0, net.minecraft.nbt.DoubleTag.valueOf($$1));
   }

   public void putString(String $$0, String $$1) {
      this.tags.put($$0, net.minecraft.nbt.StringTag.valueOf($$1));
   }

   public void putByteArray(String $$0, byte[] $$1) {
      this.tags.put($$0, new net.minecraft.nbt.ByteArrayTag($$1));
   }

   public void putIntArray(String $$0, int[] $$1) {
      this.tags.put($$0, new net.minecraft.nbt.IntArrayTag($$1));
   }

   public void putLongArray(String $$0, long[] $$1) {
      this.tags.put($$0, new net.minecraft.nbt.LongArrayTag($$1));
   }

   public void putBoolean(String $$0, boolean $$1) {
      this.tags.put($$0, net.minecraft.nbt.ByteTag.valueOf($$1));
   }

   
   public net.minecraft.nbt.Tag get(String $$0) {
      return this.tags.get($$0);
   }

   public boolean contains(String $$0) {
      return this.tags.containsKey($$0);
   }

   private Optional<net.minecraft.nbt.Tag> getOptional(String $$0) {
      return Optional.ofNullable(this.tags.get($$0));
   }

   public Optional<Byte> getByte(String $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asByte);
   }

   public byte getByteOr(String $$0, byte $$1) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.NumericTag $$2 ? $$2.byteValue() : $$1;
   }

   public Optional<Short> getShort(String $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asShort);
   }

   public short getShortOr(String $$0, short $$1) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.NumericTag $$2 ? $$2.shortValue() : $$1;
   }

   public Optional<Integer> getInt(String $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asInt);
   }

   public int getIntOr(String $$0, int $$1) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.NumericTag $$2 ? $$2.intValue() : $$1;
   }

   public Optional<Long> getLong(String $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asLong);
   }

   public long getLongOr(String $$0, long $$1) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.NumericTag $$2 ? $$2.longValue() : $$1;
   }

   public Optional<Float> getFloat(String $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asFloat);
   }

   public float getFloatOr(String $$0, float $$1) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.NumericTag $$2 ? $$2.floatValue() : $$1;
   }

   public Optional<Double> getDouble(String $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asDouble);
   }

   public double getDoubleOr(String $$0, double $$1) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.NumericTag $$2 ? $$2.doubleValue() : $$1;
   }

   public Optional<String> getString(String $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asString);
   }

   public String getStringOr(String $$0, String $$1) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.StringTag(String var8) ? var8 : $$1;
   }

   public Optional<byte[]> getByteArray(String $$0) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.ByteArrayTag $$1 ? Optional.of($$1.getAsByteArray()) : Optional.empty();
   }

   public Optional<int[]> getIntArray(String $$0) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.IntArrayTag $$1 ? Optional.of($$1.getAsIntArray()) : Optional.empty();
   }

   public Optional<long[]> getLongArray(String $$0) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.LongArrayTag $$1 ? Optional.of($$1.getAsLongArray()) : Optional.empty();
   }

   public Optional<net.minecraft.nbt.CompoundTag> getCompound(String $$0) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.CompoundTag $$1 ? Optional.of($$1) : Optional.empty();
   }

   public net.minecraft.nbt.CompoundTag getCompoundOrEmpty(String $$0) {
      return this.getCompound($$0).orElseGet(net.minecraft.nbt.CompoundTag::new);
   }

   public Optional<net.minecraft.nbt.ListTag> getList(String $$0) {
      return this.tags.get($$0) instanceof net.minecraft.nbt.ListTag $$1 ? Optional.of($$1) : Optional.empty();
   }

   public net.minecraft.nbt.ListTag getListOrEmpty(String $$0) {
      return this.getList($$0).orElseGet(net.minecraft.nbt.ListTag::new);
   }

   public Optional<Boolean> getBoolean(String $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asBoolean);
   }

   public boolean getBooleanOr(String $$0, boolean $$1) {
      return this.getByteOr($$0, (byte)($$1 ? 1 : 0)) != 0;
   }

   
   public net.minecraft.nbt.Tag remove(String $$0) {
      return this.tags.remove($$0);
   }

   @Override
   public String toString() {
      net.minecraft.nbt.StringTagVisitor $$0 = new net.minecraft.nbt.StringTagVisitor();
      $$0.visitCompound(this);
      return $$0.build();
   }

   public boolean isEmpty() {
      return this.tags.isEmpty();
   }

   protected net.minecraft.nbt.CompoundTag shallowCopy() {
      return new net.minecraft.nbt.CompoundTag(new HashMap<>(this.tags));
   }

   public net.minecraft.nbt.CompoundTag copy() {
      HashMap<String, net.minecraft.nbt.Tag> $$0 = new HashMap<>();
      this.tags.forEach(($$1, $$2) -> $$0.put($$1, $$2.copy()));
      return new net.minecraft.nbt.CompoundTag($$0);
   }

   @Override
   public Optional<net.minecraft.nbt.CompoundTag> asCompound() {
      return Optional.of(this);
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof net.minecraft.nbt.CompoundTag && Objects.equals(this.tags, ((net.minecraft.nbt.CompoundTag)$$0).tags);
   }

   @Override
   public int hashCode() {
      return this.tags.hashCode();
   }

   private static void writeNamedTag(String $$0, net.minecraft.nbt.Tag $$1, DataOutput $$2) throws IOException {
      $$2.writeByte($$1.getId());
      if ($$1.getId() != 0) {
         $$2.writeUTF($$0);
         $$1.write($$2);
      }
   }

   static net.minecraft.nbt.Tag readNamedTagData(net.minecraft.nbt.TagType<?> $$0, String $$1, DataInput $$2, net.minecraft.nbt.NbtAccounter $$3) {
      try {
         return $$0.load($$2, $$3);
      } catch (IOException var7) {
         CrashReport $$5 = CrashReport.forThrowable(var7, "Loading NBT data");
         CrashReportCategory $$6 = $$5.addCategory("NBT Tag");
         $$6.setDetail("Tag name", $$1);
         $$6.setDetail("Tag type", $$0.getName());
         throw new net.minecraft.nbt.ReportedNbtException($$5);
      }
   }

   public net.minecraft.nbt.CompoundTag merge(net.minecraft.nbt.CompoundTag $$0) {
      for (String $$1 : $$0.tags.keySet()) {
         net.minecraft.nbt.Tag $$2 = $$0.tags.get($$1);
         if ($$2 instanceof net.minecraft.nbt.CompoundTag $$3 && this.tags.get($$1) instanceof net.minecraft.nbt.CompoundTag $$4) {
            $$4.merge($$3);
         } else {
            this.put($$1, $$2.copy());
         }
      }

      return this;
   }

   @Override
   public void accept(net.minecraft.nbt.TagVisitor $$0) {
      $$0.visitCompound(this);
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult accept(net.minecraft.nbt.StreamTagVisitor $$0) {
      for (Entry<String, net.minecraft.nbt.Tag> $$1 : this.tags.entrySet()) {
         net.minecraft.nbt.Tag $$2 = $$1.getValue();
         net.minecraft.nbt.TagType<?> $$3 = $$2.getType();
         net.minecraft.nbt.StreamTagVisitor.EntryResult $$4 = $$0.visitEntry($$3);
         switch ($$4) {
            case HALT:
               return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
            case BREAK:
               return $$0.visitContainerEnd();
            case SKIP:
               break;
            default:
               $$4 = $$0.visitEntry($$3, $$1.getKey());
               switch ($$4) {
                  case HALT:
                     return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
                  case BREAK:
                     return $$0.visitContainerEnd();
                  case SKIP:
                     break;
                  default:
                     net.minecraft.nbt.StreamTagVisitor.ValueResult $$5 = $$2.accept($$0);
                     switch ($$5) {
                        case HALT:
                           return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                           return $$0.visitContainerEnd();
                     }
               }
         }
      }

      return $$0.visitContainerEnd();
   }

   public <T> void store(String $$0, Codec<T> $$1, T $$2) {
      this.store($$0, $$1, net.minecraft.nbt.NbtOps.INSTANCE, $$2);
   }

   public <T> void storeNullable(String $$0, Codec<T> $$1, T $$2) {
      if ($$2 != null) {
         this.store($$0, $$1, $$2);
      }
   }

   public <T> void store(String $$0, Codec<T> $$1, DynamicOps<net.minecraft.nbt.Tag> $$2, T $$3) {
      this.put($$0, (net.minecraft.nbt.Tag)$$1.encodeStart($$2, $$3).getOrThrow());
   }

   public <T> void storeNullable(String $$0, Codec<T> $$1, DynamicOps<net.minecraft.nbt.Tag> $$2, T $$3) {
      if ($$3 != null) {
         this.store($$0, $$1, $$2, $$3);
      }
   }

   public <T> void store(MapCodec<T> $$0, T $$1) {
      this.store($$0, net.minecraft.nbt.NbtOps.INSTANCE, $$1);
   }

   public <T> void store(MapCodec<T> $$0, DynamicOps<net.minecraft.nbt.Tag> $$1, T $$2) {
      this.merge((net.minecraft.nbt.CompoundTag)$$0.encoder().encodeStart($$1, $$2).getOrThrow());
   }

   public <T> Optional<T> read(String $$0, Codec<T> $$1) {
      return this.read($$0, $$1, net.minecraft.nbt.NbtOps.INSTANCE);
   }

   public <T> Optional<T> read(String $$0, Codec<T> $$1, DynamicOps<net.minecraft.nbt.Tag> $$2) {
      net.minecraft.nbt.Tag $$3 = this.get($$0);
      return $$3 == null
         ? Optional.empty()
         : $$1.parse($$2, $$3).resultOrPartial($$2x -> LOGGER.error("Failed to read field ({}={}): {}", new Object[]{$$0, $$3, $$2x}));
   }

   public <T> Optional<T> read(MapCodec<T> $$0) {
      return this.read($$0, net.minecraft.nbt.NbtOps.INSTANCE);
   }

   public <T> Optional<T> read(MapCodec<T> $$0, DynamicOps<net.minecraft.nbt.Tag> $$1) {
      return $$0.decode($$1, (MapLike)$$1.getMap(this).getOrThrow()).resultOrPartial($$0x -> LOGGER.error("Failed to read value ({}): {}", this, $$0x));
   }
}
