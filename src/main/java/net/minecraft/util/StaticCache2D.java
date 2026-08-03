package net.minecraft.util;

import java.util.Locale;
import java.util.function.Consumer;

public class StaticCache2D<T> {
   private final int minX;
   private final int minZ;
   private final int sizeX;
   private final int sizeZ;
   private final Object[] cache;

   public static <T> net.minecraft.util.StaticCache2D<T> create(int $$0, int $$1, int $$2, net.minecraft.util.StaticCache2D.Initializer<T> $$3) {
      int $$4 = $$0 - $$2;
      int $$5 = $$1 - $$2;
      int $$6 = 2 * $$2 + 1;
      return new net.minecraft.util.StaticCache2D<>($$4, $$5, $$6, $$6, $$3);
   }

   private StaticCache2D(int $$0, int $$1, int $$2, int $$3, net.minecraft.util.StaticCache2D.Initializer<T> $$4) {
      this.minX = $$0;
      this.minZ = $$1;
      this.sizeX = $$2;
      this.sizeZ = $$3;
      this.cache = new Object[this.sizeX * this.sizeZ];

      for (int $$5 = $$0; $$5 < $$0 + $$2; $$5++) {
         for (int $$6 = $$1; $$6 < $$1 + $$3; $$6++) {
            this.cache[this.getIndex($$5, $$6)] = $$4.get($$5, $$6);
         }
      }
   }

   public void forEach(Consumer<T> $$0) {
      for (Object $$1 : this.cache) {
         $$0.accept((T)$$1);
      }
   }

   public T get(int $$0, int $$1) {
      if (!this.contains($$0, $$1)) {
         throw new IllegalArgumentException("Requested out of range value (" + $$0 + "," + $$1 + ") from " + this);
      } else {
         return (T)this.cache[this.getIndex($$0, $$1)];
      }
   }

   public boolean contains(int $$0, int $$1) {
      int $$2 = $$0 - this.minX;
      int $$3 = $$1 - this.minZ;
      return $$2 >= 0 && $$2 < this.sizeX && $$3 >= 0 && $$3 < this.sizeZ;
   }

   @Override
   public String toString() {
      return String.format(Locale.ROOT, "StaticCache2D[%d, %d, %d, %d]", this.minX, this.minZ, this.minX + this.sizeX, this.minZ + this.sizeZ);
   }

   private int getIndex(int $$0, int $$1) {
      int $$2 = $$0 - this.minX;
      int $$3 = $$1 - this.minZ;
      return $$2 * this.sizeZ + $$3;
   }

   @FunctionalInterface
   public interface Initializer<T> {
      T get(int var1, int var2);
   }
}
