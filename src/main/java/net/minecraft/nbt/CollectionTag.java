package net.minecraft.nbt;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public sealed interface CollectionTag
   extends Iterable<net.minecraft.nbt.Tag>,
   net.minecraft.nbt.Tag
   permits net.minecraft.nbt.ListTag,
   net.minecraft.nbt.ByteArrayTag,
   net.minecraft.nbt.IntArrayTag,
   net.minecraft.nbt.LongArrayTag {
   void clear();

   boolean setTag(int var1, net.minecraft.nbt.Tag var2);

   boolean addTag(int var1, net.minecraft.nbt.Tag var2);

   net.minecraft.nbt.Tag remove(int var1);

   net.minecraft.nbt.Tag get(int var1);

   int size();

   default boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   default Iterator<net.minecraft.nbt.Tag> iterator() {
      return new Iterator<net.minecraft.nbt.Tag>() {
         private int index;

         @Override
         public boolean hasNext() {
            return this.index < CollectionTag.this.size();
         }

         public net.minecraft.nbt.Tag next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               return CollectionTag.this.get(this.index++);
            }
         }
      };
   }

   default Stream<net.minecraft.nbt.Tag> stream() {
      return StreamSupport.stream(this.spliterator(), false);
   }
}
