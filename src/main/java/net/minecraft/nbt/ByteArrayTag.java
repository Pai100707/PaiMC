package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;

public final class ByteArrayTag implements net.minecraft.nbt.CollectionTag {
   private static final int SELF_SIZE_IN_BYTES = 24;
   public static final net.minecraft.nbt.TagType<net.minecraft.nbt.ByteArrayTag> TYPE = new net.minecraft.nbt.TagType.VariableSize<net.minecraft.nbt.ByteArrayTag>() {
      public net.minecraft.nbt.ByteArrayTag load(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         return new net.minecraft.nbt.ByteArrayTag(readAccounted($$0, $$1));
      }

      @Override
      public net.minecraft.nbt.StreamTagVisitor.ValueResult parse(DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
         return $$1.visit(readAccounted($$0, $$2));
      }

      private static byte[] readAccounted(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$1.accountBytes(24L);
         int $$2 = $$0.readInt();
         $$1.accountBytes(1L, $$2);
         byte[] $$3 = new byte[$$2];
         $$0.readFully($$3);
         return $$3;
      }

      @Override
      public void skip(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$0.skipBytes($$0.readInt() * 1);
      }

      @Override
      public String getName() {
         return "BYTE[]";
      }

      @Override
      public String getPrettyName() {
         return "TAG_Byte_Array";
      }
   };
   private byte[] data;

   public ByteArrayTag(byte[] $$0) {
      this.data = $$0;
   }

   @Override
   public void write(DataOutput $$0) throws IOException {
      $$0.writeInt(this.data.length);
      $$0.write(this.data);
   }

   @Override
   public int sizeInBytes() {
      return 24 + 1 * this.data.length;
   }

   @Override
   public byte getId() {
      return 7;
   }

   @Override
   public net.minecraft.nbt.TagType<net.minecraft.nbt.ByteArrayTag> getType() {
      return TYPE;
   }

   @Override
   public String toString() {
      net.minecraft.nbt.StringTagVisitor $$0 = new net.minecraft.nbt.StringTagVisitor();
      $$0.visitByteArray(this);
      return $$0.build();
   }

   @Override
   public net.minecraft.nbt.Tag copy() {
      byte[] $$0 = new byte[this.data.length];
      System.arraycopy(this.data, 0, $$0, 0, this.data.length);
      return new net.minecraft.nbt.ByteArrayTag($$0);
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof net.minecraft.nbt.ByteArrayTag && Arrays.equals(this.data, ((net.minecraft.nbt.ByteArrayTag)$$0).data);
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.data);
   }

   @Override
   public void accept(net.minecraft.nbt.TagVisitor $$0) {
      $$0.visitByteArray(this);
   }

   public byte[] getAsByteArray() {
      return this.data;
   }

   @Override
   public int size() {
      return this.data.length;
   }

   public net.minecraft.nbt.ByteTag get(int $$0) {
      return net.minecraft.nbt.ByteTag.valueOf(this.data[$$0]);
   }

   @Override
   public boolean setTag(int $$0, net.minecraft.nbt.Tag $$1) {
      if ($$1 instanceof net.minecraft.nbt.NumericTag $$2) {
         this.data[$$0] = $$2.byteValue();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean addTag(int $$0, net.minecraft.nbt.Tag $$1) {
      if ($$1 instanceof net.minecraft.nbt.NumericTag $$2) {
         this.data = ArrayUtils.add(this.data, $$0, $$2.byteValue());
         return true;
      } else {
         return false;
      }
   }

   public net.minecraft.nbt.ByteTag remove(int $$0) {
      byte $$1 = this.data[$$0];
      this.data = ArrayUtils.remove(this.data, $$0);
      return net.minecraft.nbt.ByteTag.valueOf($$1);
   }

   @Override
   public void clear() {
      this.data = new byte[0];
   }

   @Override
   public Optional<byte[]> asByteArray() {
      return Optional.of(this.data);
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult accept(net.minecraft.nbt.StreamTagVisitor $$0) {
      return $$0.visit(this.data);
   }
}
