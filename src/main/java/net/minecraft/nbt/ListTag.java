package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public final class ListTag extends AbstractList<net.minecraft.nbt.Tag> implements net.minecraft.nbt.CollectionTag {
   private static final String WRAPPER_MARKER = "";
   private static final int SELF_SIZE_IN_BYTES = 36;
   public static final net.minecraft.nbt.TagType<net.minecraft.nbt.ListTag> TYPE = new net.minecraft.nbt.TagType.VariableSize<net.minecraft.nbt.ListTag>() {
      public net.minecraft.nbt.ListTag load(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$1.pushDepth();

         net.minecraft.nbt.ListTag var3;
         try {
            var3 = loadList($$0, $$1);
         } finally {
            $$1.popDepth();
         }

         return var3;
      }

      private static net.minecraft.nbt.ListTag loadList(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$1.accountBytes(36L);
         byte $$2 = $$0.readByte();
         int $$3 = readListCount($$0);
         if ($$2 == 0 && $$3 > 0) {
            throw new net.minecraft.nbt.NbtFormatException("Missing type on ListTag");
         } else {
            $$1.accountBytes(4L, $$3);
            net.minecraft.nbt.TagType<?> $$4 = net.minecraft.nbt.TagTypes.getType($$2);
            net.minecraft.nbt.ListTag $$5 = new net.minecraft.nbt.ListTag(new ArrayList<>($$3));

            for (int $$6 = 0; $$6 < $$3; $$6++) {
               $$5.addAndUnwrap($$4.load($$0, $$1));
            }

            return $$5;
         }
      }

      @Override
      public net.minecraft.nbt.StreamTagVisitor.ValueResult parse(DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
         $$2.pushDepth();

         net.minecraft.nbt.StreamTagVisitor.ValueResult var4;
         try {
            var4 = parseList($$0, $$1, $$2);
         } finally {
            $$2.popDepth();
         }

         return var4;
      }

      private static net.minecraft.nbt.StreamTagVisitor.ValueResult parseList(
         DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2
      ) throws IOException {
         $$2.accountBytes(36L);
         net.minecraft.nbt.TagType<?> $$3 = net.minecraft.nbt.TagTypes.getType($$0.readByte());
         int $$4 = readListCount($$0);
         switch ($$1.visitList($$3, $$4)) {
            case HALT:
               return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
            case BREAK:
               $$3.skip($$0, $$4, $$2);
               return $$1.visitContainerEnd();
            default:
               $$2.accountBytes(4L, $$4);
               int $$5 = 0;

               while (true) {
                  label41: {
                     if ($$5 < $$4) {
                        switch ($$1.visitElement($$3, $$5)) {
                           case HALT:
                              return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
                           case BREAK:
                              $$3.skip($$0, $$2);
                              break;
                           case SKIP:
                              $$3.skip($$0, $$2);
                              break label41;
                           default:
                              switch ($$3.parse($$0, $$1, $$2)) {
                                 case HALT:
                                    return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
                                 case BREAK:
                                    break;
                                 default:
                                    break label41;
                              }
                        }
                     }

                     int $$6 = $$4 - 1 - $$5;
                     if ($$6 > 0) {
                        $$3.skip($$0, $$6, $$2);
                     }

                     return $$1.visitContainerEnd();
                  }

                  $$5++;
               }
         }
      }

      private static int readListCount(DataInput $$0) throws IOException {
         int $$1 = $$0.readInt();
         if ($$1 < 0) {
            throw new net.minecraft.nbt.NbtFormatException("ListTag length cannot be negative: " + $$1);
         } else {
            return $$1;
         }
      }

      @Override
      public void skip(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$1.pushDepth();

         try {
            net.minecraft.nbt.TagType<?> $$2 = net.minecraft.nbt.TagTypes.getType($$0.readByte());
            int $$3 = $$0.readInt();
            $$2.skip($$0, $$3, $$1);
         } finally {
            $$1.popDepth();
         }
      }

      @Override
      public String getName() {
         return "LIST";
      }

      @Override
      public String getPrettyName() {
         return "TAG_List";
      }
   };
   private final List<net.minecraft.nbt.Tag> list;

   public ListTag() {
      this(new ArrayList<>());
   }

   ListTag(List<net.minecraft.nbt.Tag> $$0) {
      this.list = $$0;
   }

   private static net.minecraft.nbt.Tag tryUnwrap(net.minecraft.nbt.CompoundTag $$0) {
      if ($$0.size() == 1) {
         net.minecraft.nbt.Tag $$1 = $$0.get("");
         if ($$1 != null) {
            return $$1;
         }
      }

      return $$0;
   }

   private static boolean isWrapper(net.minecraft.nbt.CompoundTag $$0) {
      return $$0.size() == 1 && $$0.contains("");
   }

   private static net.minecraft.nbt.Tag wrapIfNeeded(byte $$0, net.minecraft.nbt.Tag $$1) {
      if ($$0 != 10) {
         return $$1;
      } else {
         return $$1 instanceof net.minecraft.nbt.CompoundTag $$2 && !isWrapper($$2) ? $$2 : wrapElement($$1);
      }
   }

   private static net.minecraft.nbt.CompoundTag wrapElement(net.minecraft.nbt.Tag $$0) {
      return new net.minecraft.nbt.CompoundTag(Map.of("", $$0));
   }

   @Override
   public void write(DataOutput $$0) throws IOException {
      byte $$1 = this.identifyRawElementType();
      $$0.writeByte($$1);
      $$0.writeInt(this.list.size());

      for (net.minecraft.nbt.Tag $$2 : this.list) {
         wrapIfNeeded($$1, $$2).write($$0);
      }
   }

   @VisibleForTesting
   byte identifyRawElementType() {
      byte $$0 = 0;

      for (net.minecraft.nbt.Tag $$1 : this.list) {
         byte $$2 = $$1.getId();
         if ($$0 == 0) {
            $$0 = $$2;
         } else if ($$0 != $$2) {
            return 10;
         }
      }

      return $$0;
   }

   public void addAndUnwrap(net.minecraft.nbt.Tag $$0) {
      if ($$0 instanceof net.minecraft.nbt.CompoundTag $$1) {
         this.add(tryUnwrap($$1));
      } else {
         this.add($$0);
      }
   }

   @Override
   public int sizeInBytes() {
      int $$0 = 36;
      $$0 += 4 * this.list.size();

      for (net.minecraft.nbt.Tag $$1 : this.list) {
         $$0 += $$1.sizeInBytes();
      }

      return $$0;
   }

   @Override
   public byte getId() {
      return 9;
   }

   @Override
   public net.minecraft.nbt.TagType<net.minecraft.nbt.ListTag> getType() {
      return TYPE;
   }

   @Override
   public String toString() {
      net.minecraft.nbt.StringTagVisitor $$0 = new net.minecraft.nbt.StringTagVisitor();
      $$0.visitList(this);
      return $$0.build();
   }

   @Override
   public net.minecraft.nbt.Tag remove(int $$0) {
      return this.list.remove($$0);
   }

   @Override
   public boolean isEmpty() {
      return this.list.isEmpty();
   }

   public Optional<net.minecraft.nbt.CompoundTag> getCompound(int $$0) {
      return this.getNullable($$0) instanceof net.minecraft.nbt.CompoundTag $$1 ? Optional.of($$1) : Optional.empty();
   }

   public net.minecraft.nbt.CompoundTag getCompoundOrEmpty(int $$0) {
      return this.getCompound($$0).orElseGet(net.minecraft.nbt.CompoundTag::new);
   }

   public Optional<net.minecraft.nbt.ListTag> getList(int $$0) {
      return this.getNullable($$0) instanceof net.minecraft.nbt.ListTag $$1 ? Optional.of($$1) : Optional.empty();
   }

   public net.minecraft.nbt.ListTag getListOrEmpty(int $$0) {
      return this.getList($$0).orElseGet(net.minecraft.nbt.ListTag::new);
   }

   public Optional<Short> getShort(int $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asShort);
   }

   public short getShortOr(int $$0, short $$1) {
      return this.getNullable($$0) instanceof net.minecraft.nbt.NumericTag $$2 ? $$2.shortValue() : $$1;
   }

   public Optional<Integer> getInt(int $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asInt);
   }

   public int getIntOr(int $$0, int $$1) {
      return this.getNullable($$0) instanceof net.minecraft.nbt.NumericTag $$2 ? $$2.intValue() : $$1;
   }

   public Optional<int[]> getIntArray(int $$0) {
      return this.getNullable($$0) instanceof net.minecraft.nbt.IntArrayTag $$1 ? Optional.of($$1.getAsIntArray()) : Optional.empty();
   }

   public Optional<long[]> getLongArray(int $$0) {
      return this.getNullable($$0) instanceof net.minecraft.nbt.LongArrayTag $$1 ? Optional.of($$1.getAsLongArray()) : Optional.empty();
   }

   public Optional<Double> getDouble(int $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asDouble);
   }

   public double getDoubleOr(int $$0, double $$1) {
      return this.getNullable($$0) instanceof net.minecraft.nbt.NumericTag $$2 ? $$2.doubleValue() : $$1;
   }

   public Optional<Float> getFloat(int $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asFloat);
   }

   public float getFloatOr(int $$0, float $$1) {
      return this.getNullable($$0) instanceof net.minecraft.nbt.NumericTag $$2 ? $$2.floatValue() : $$1;
   }

   public Optional<String> getString(int $$0) {
      return this.getOptional($$0).flatMap(net.minecraft.nbt.Tag::asString);
   }

   public String getStringOr(int $$0, String $$1) {
      return this.getNullable($$0) instanceof net.minecraft.nbt.StringTag(String var8) ? var8 : $$1;
   }

   @Nullable
   private net.minecraft.nbt.Tag getNullable(int $$0) {
      return $$0 >= 0 && $$0 < this.list.size() ? this.list.get($$0) : null;
   }

   private Optional<net.minecraft.nbt.Tag> getOptional(int $$0) {
      return Optional.ofNullable(this.getNullable($$0));
   }

   @Override
   public int size() {
      return this.list.size();
   }

   @Override
   public net.minecraft.nbt.Tag get(int $$0) {
      return this.list.get($$0);
   }

   public net.minecraft.nbt.Tag set(int $$0, net.minecraft.nbt.Tag $$1) {
      return this.list.set($$0, $$1);
   }

   public void add(int $$0, net.minecraft.nbt.Tag $$1) {
      this.list.add($$0, $$1);
   }

   @Override
   public boolean setTag(int $$0, net.minecraft.nbt.Tag $$1) {
      this.list.set($$0, $$1);
      return true;
   }

   @Override
   public boolean addTag(int $$0, net.minecraft.nbt.Tag $$1) {
      this.list.add($$0, $$1);
      return true;
   }

   public net.minecraft.nbt.ListTag copy() {
      List<net.minecraft.nbt.Tag> $$0 = new ArrayList<>(this.list.size());

      for (net.minecraft.nbt.Tag $$1 : this.list) {
         $$0.add($$1.copy());
      }

      return new net.minecraft.nbt.ListTag($$0);
   }

   @Override
   public Optional<net.minecraft.nbt.ListTag> asList() {
      return Optional.of(this);
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof net.minecraft.nbt.ListTag && Objects.equals(this.list, ((net.minecraft.nbt.ListTag)$$0).list);
   }

   @Override
   public int hashCode() {
      return this.list.hashCode();
   }

   @Override
   public Stream<net.minecraft.nbt.Tag> stream() {
      return super.stream();
   }

   public Stream<net.minecraft.nbt.CompoundTag> compoundStream() {
      return this.stream().mapMulti(($$0, $$1) -> {
         if ($$0 instanceof net.minecraft.nbt.CompoundTag $$2) {
            $$1.accept($$2);
         }
      });
   }

   @Override
   public void accept(net.minecraft.nbt.TagVisitor $$0) {
      $$0.visitList(this);
   }

   @Override
   public void clear() {
      this.list.clear();
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult accept(net.minecraft.nbt.StreamTagVisitor $$0) {
      byte $$1 = this.identifyRawElementType();
      switch ($$0.visitList(net.minecraft.nbt.TagTypes.getType($$1), this.list.size())) {
         case HALT:
            return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
         case BREAK:
            return $$0.visitContainerEnd();
         default:
            int $$2 = 0;

            while ($$2 < this.list.size()) {
               net.minecraft.nbt.Tag $$3 = wrapIfNeeded($$1, this.list.get($$2));
               switch ($$0.visitElement($$3.getType(), $$2)) {
                  case HALT:
                     return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
                  case BREAK:
                     return $$0.visitContainerEnd();
                  default:
                     switch ($$3.accept($$0)) {
                        case HALT:
                           return net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                           return $$0.visitContainerEnd();
                     }
                  case SKIP:
                     $$2++;
               }
            }

            return $$0.visitContainerEnd();
      }
   }
}
