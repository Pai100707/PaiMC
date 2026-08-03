package net.minecraft.util;

import java.util.Arrays;
import java.util.function.IntConsumer;
import org.apache.commons.lang3.Validate;

public class ZeroBitStorage implements net.minecraft.util.BitStorage {
   public static final long[] RAW = new long[0];
   private final int size;

   public ZeroBitStorage(int $$0) {
      this.size = $$0;
   }

   @Override
   public int getAndSet(int $$0, int $$1) {
      Validate.inclusiveBetween(0L, this.size - 1, $$0);
      Validate.inclusiveBetween(0L, 0L, $$1);
      return 0;
   }

   @Override
   public void set(int $$0, int $$1) {
      Validate.inclusiveBetween(0L, this.size - 1, $$0);
      Validate.inclusiveBetween(0L, 0L, $$1);
   }

   @Override
   public int get(int $$0) {
      Validate.inclusiveBetween(0L, this.size - 1, $$0);
      return 0;
   }

   @Override
   public long[] getRaw() {
      return RAW;
   }

   @Override
   public int getSize() {
      return this.size;
   }

   @Override
   public int getBits() {
      return 0;
   }

   @Override
   public void getAll(IntConsumer $$0) {
      for (int $$1 = 0; $$1 < this.size; $$1++) {
         $$0.accept(0);
      }
   }

   @Override
   public void unpack(int[] $$0) {
      Arrays.fill($$0, 0, this.size, 0);
   }

   @Override
   public net.minecraft.util.BitStorage copy() {
      return this;
   }
}
