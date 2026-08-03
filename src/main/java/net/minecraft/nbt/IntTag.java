package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record IntTag(int value) implements net.minecraft.nbt.NumericTag {
   private static final int SELF_SIZE_IN_BYTES = 12;
   public static final net.minecraft.nbt.TagType<net.minecraft.nbt.IntTag> TYPE = new net.minecraft.nbt.TagType.StaticSize<net.minecraft.nbt.IntTag>() {
      public net.minecraft.nbt.IntTag load(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         return net.minecraft.nbt.IntTag.valueOf(readAccounted($$0, $$1));
      }

      @Override
      public net.minecraft.nbt.StreamTagVisitor.ValueResult parse(DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
         return $$1.visit(readAccounted($$0, $$2));
      }

      private static int readAccounted(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$1.accountBytes(12L);
         return $$0.readInt();
      }

      @Override
      public int size() {
         return 4;
      }

      @Override
      public String getName() {
         return "INT";
      }

      @Override
      public String getPrettyName() {
         return "TAG_Int";
      }
   };

   @Deprecated(
      forRemoval = true
   )
   public IntTag(int value) {
      this.value = value;
   }

   public static net.minecraft.nbt.IntTag valueOf(int $$0) {
      return $$0 >= -128 && $$0 <= 1024 ? net.minecraft.nbt.IntTag.Cache.cache[$$0 - -128] : new net.minecraft.nbt.IntTag($$0);
   }

   @Override
   public void write(DataOutput $$0) throws IOException {
      $$0.writeInt(this.value);
   }

   @Override
   public int sizeInBytes() {
      return 12;
   }

   @Override
   public byte getId() {
      return 3;
   }

   @Override
   public net.minecraft.nbt.TagType<net.minecraft.nbt.IntTag> getType() {
      return TYPE;
   }

   public net.minecraft.nbt.IntTag copy() {
      return this;
   }

   @Override
   public void accept(net.minecraft.nbt.TagVisitor $$0) {
      $$0.visitInt(this);
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
      return (short)(this.value & 65535);
   }

   @Override
   public byte byteValue() {
      return (byte)(this.value & 0xFF);
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
      $$0.visitInt(this);
      return $$0.build();
   }

   static class Cache {
      private static final int HIGH = 1024;
      private static final int LOW = -128;
      static final net.minecraft.nbt.IntTag[] cache = new net.minecraft.nbt.IntTag[1153];

      private Cache() {
      }

      static {
         for (int $$0 = 0; $$0 < cache.length; $$0++) {
            cache[$$0] = new net.minecraft.nbt.IntTag(-128 + $$0);
         }
      }
   }
}
