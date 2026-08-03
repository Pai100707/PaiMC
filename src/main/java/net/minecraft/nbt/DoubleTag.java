package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.Mth;

public record DoubleTag(double value) implements net.minecraft.nbt.NumericTag {
   private static final int SELF_SIZE_IN_BYTES = 16;
   public static final net.minecraft.nbt.DoubleTag ZERO = new net.minecraft.nbt.DoubleTag(0.0);
   public static final net.minecraft.nbt.TagType<net.minecraft.nbt.DoubleTag> TYPE = new net.minecraft.nbt.TagType.StaticSize<net.minecraft.nbt.DoubleTag>() {
      public net.minecraft.nbt.DoubleTag load(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         return net.minecraft.nbt.DoubleTag.valueOf(readAccounted($$0, $$1));
      }

      @Override
      public net.minecraft.nbt.StreamTagVisitor.ValueResult parse(DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
         return $$1.visit(readAccounted($$0, $$2));
      }

      private static double readAccounted(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$1.accountBytes(16L);
         return $$0.readDouble();
      }

      @Override
      public int size() {
         return 8;
      }

      @Override
      public String getName() {
         return "DOUBLE";
      }

      @Override
      public String getPrettyName() {
         return "TAG_Double";
      }
   };

   @Deprecated(
      forRemoval = true
   )
   public DoubleTag(double value) {
      this.value = value;
   }

   public static net.minecraft.nbt.DoubleTag valueOf(double $$0) {
      return $$0 == 0.0 ? ZERO : new net.minecraft.nbt.DoubleTag($$0);
   }

   @Override
   public void write(DataOutput $$0) throws IOException {
      $$0.writeDouble(this.value);
   }

   @Override
   public int sizeInBytes() {
      return 16;
   }

   @Override
   public byte getId() {
      return 6;
   }

   @Override
   public net.minecraft.nbt.TagType<net.minecraft.nbt.DoubleTag> getType() {
      return TYPE;
   }

   public net.minecraft.nbt.DoubleTag copy() {
      return this;
   }

   @Override
   public void accept(net.minecraft.nbt.TagVisitor $$0) {
      $$0.visitDouble(this);
   }

   @Override
   public long longValue() {
      return (long)Math.floor(this.value);
   }

   @Override
   public int intValue() {
      return Mth.floor(this.value);
   }

   @Override
   public short shortValue() {
      return (short)(Mth.floor(this.value) & 65535);
   }

   @Override
   public byte byteValue() {
      return (byte)(Mth.floor(this.value) & 0xFF);
   }

   @Override
   public double doubleValue() {
      return this.value;
   }

   @Override
   public float floatValue() {
      return (float)this.value;
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
      $$0.visitDouble(this);
      return $$0.build();
   }
}
