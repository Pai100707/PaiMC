package net.minecraft.nbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;

public sealed interface Tag permits net.minecraft.nbt.CompoundTag, net.minecraft.nbt.CollectionTag, net.minecraft.nbt.PrimitiveTag, net.minecraft.nbt.EndTag {
   int OBJECT_HEADER = 8;
   int ARRAY_HEADER = 12;
   int OBJECT_REFERENCE = 4;
   int STRING_SIZE = 28;
   byte TAG_END = 0;
   byte TAG_BYTE = 1;
   byte TAG_SHORT = 2;
   byte TAG_INT = 3;
   byte TAG_LONG = 4;
   byte TAG_FLOAT = 5;
   byte TAG_DOUBLE = 6;
   byte TAG_BYTE_ARRAY = 7;
   byte TAG_STRING = 8;
   byte TAG_LIST = 9;
   byte TAG_COMPOUND = 10;
   byte TAG_INT_ARRAY = 11;
   byte TAG_LONG_ARRAY = 12;
   int MAX_DEPTH = 512;

   void write(DataOutput var1) throws IOException;

   @Override
   String toString();

   byte getId();

   net.minecraft.nbt.TagType<?> getType();

   net.minecraft.nbt.Tag copy();

   int sizeInBytes();

   void accept(net.minecraft.nbt.TagVisitor var1);

   net.minecraft.nbt.StreamTagVisitor.ValueResult accept(net.minecraft.nbt.StreamTagVisitor var1);

   default void acceptAsRoot(net.minecraft.nbt.StreamTagVisitor $$0) {
      net.minecraft.nbt.StreamTagVisitor.ValueResult $$1 = $$0.visitRootEntry(this.getType());
      if ($$1 == net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE) {
         this.accept($$0);
      }
   }

   default Optional<String> asString() {
      return Optional.empty();
   }

   default Optional<Number> asNumber() {
      return Optional.empty();
   }

   default Optional<Byte> asByte() {
      return this.asNumber().map(Number::byteValue);
   }

   default Optional<Short> asShort() {
      return this.asNumber().map(Number::shortValue);
   }

   default Optional<Integer> asInt() {
      return this.asNumber().map(Number::intValue);
   }

   default Optional<Long> asLong() {
      return this.asNumber().map(Number::longValue);
   }

   default Optional<Float> asFloat() {
      return this.asNumber().map(Number::floatValue);
   }

   default Optional<Double> asDouble() {
      return this.asNumber().map(Number::doubleValue);
   }

   default Optional<Boolean> asBoolean() {
      return this.asByte().map($$0 -> $$0 != 0);
   }

   default Optional<byte[]> asByteArray() {
      return Optional.empty();
   }

   default Optional<int[]> asIntArray() {
      return Optional.empty();
   }

   default Optional<long[]> asLongArray() {
      return Optional.empty();
   }

   default Optional<net.minecraft.nbt.CompoundTag> asCompound() {
      return Optional.empty();
   }

   default Optional<net.minecraft.nbt.ListTag> asList() {
      return Optional.empty();
   }
}
