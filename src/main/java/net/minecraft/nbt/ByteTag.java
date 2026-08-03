package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record ByteTag(byte value) implements net.minecraft.nbt.NumericTag {
   private static final int SELF_SIZE_IN_BYTES = 9;
   public static final net.minecraft.nbt.TagType<net.minecraft.nbt.ByteTag> TYPE = new net.minecraft.nbt.TagType.StaticSize<net.minecraft.nbt.ByteTag>() {
      public net.minecraft.nbt.ByteTag load(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         return net.minecraft.nbt.ByteTag.valueOf(readAccounted($$0, $$1));
      }

      @Override
      public net.minecraft.nbt.StreamTagVisitor.ValueResult parse(DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
         return $$1.visit(readAccounted($$0, $$2));
      }

      private static byte readAccounted(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$1.accountBytes(9L);
         return $$0.readByte();
      }

      @Override
      public int size() {
         return 1;
      }

      @Override
      public String getName() {
         return "BYTE";
      }

      @Override
      public String getPrettyName() {
         return "TAG_Byte";
      }
   };
   public static final net.minecraft.nbt.ByteTag ZERO = valueOf((byte)0);
   public static final net.minecraft.nbt.ByteTag ONE = valueOf((byte)1);

   @Deprecated(
      forRemoval = true
   )
   public ByteTag(byte value) {
      this.value = value;
   }

   public static net.minecraft.nbt.ByteTag valueOf(byte $$0) {
      return net.minecraft.nbt.ByteTag.Cache.cache[128 + $$0];
   }

   public static net.minecraft.nbt.ByteTag valueOf(boolean $$0) {
      return $$0 ? ONE : ZERO;
   }

   @Override
   public void write(DataOutput $$0) throws IOException {
      $$0.writeByte(this.value);
   }

   @Override
   public int sizeInBytes() {
      return 9;
   }

   @Override
   public byte getId() {
      return 1;
   }

   @Override
   public net.minecraft.nbt.TagType<net.minecraft.nbt.ByteTag> getType() {
      return TYPE;
   }

   public net.minecraft.nbt.ByteTag copy() {
      return this;
   }

   @Override
   public void accept(net.minecraft.nbt.TagVisitor $$0) {
      $$0.visitByte(this);
   }

   @Override
   public long longValue() {
      return this.value;
   }

   @Override
   public int intValue() {
      return this.value;
   }

   @Override
   public short shortValue() {
      return this.value;
   }

   @Override
   public byte byteValue() {
      return this.value;
   }

   @Override
   public double doubleValue() {
      return this.value;
   }

   @Override
   public float floatValue() {
      return this.value;
   }

   @Override
   public Number box() {
      return this.value;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult accept(net.minecraft.nbt.StreamTagVisitor $$0) {
      return $$0.visit(this.value);
   }

   @Override
   public String toString() {
      net.minecraft.nbt.StringTagVisitor $$0 = new net.minecraft.nbt.StringTagVisitor();
      $$0.visitByte(this);
      return $$0.build();
   }

   static class Cache {
      static final net.minecraft.nbt.ByteTag[] cache = new net.minecraft.nbt.ByteTag[256];

      private Cache() {
      }

      static {
         for (int $$0 = 0; $$0 < cache.length; $$0++) {
            cache[$$0] = new net.minecraft.nbt.ByteTag((byte)($$0 - 128));
         }
      }
   }
}
