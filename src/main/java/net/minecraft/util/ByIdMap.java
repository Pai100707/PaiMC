package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public class ByIdMap {
   private static <T> IntFunction<T> createMap(ToIntFunction<T> $$0, T[] $$1) {
      if ($$1.length == 0) {
         throw new IllegalArgumentException("Empty value list");
      } else {
         Int2ObjectMap<T> $$2 = new Int2ObjectOpenHashMap();

         for (T $$3 : $$1) {
            int $$4 = $$0.applyAsInt($$3);
            T $$5 = (T)$$2.put($$4, $$3);
            if ($$5 != null) {
               throw new IllegalArgumentException("Duplicate entry on id " + $$4 + ": current=" + $$3 + ", previous=" + $$5);
            }
         }

         return $$2;
      }
   }

   public static <T> IntFunction<T> sparse(ToIntFunction<T> $$0, T[] $$1, T $$2) {
      IntFunction<T> $$3 = createMap($$0, $$1);
      return $$2x -> Objects.requireNonNullElse($$3.apply($$2x), $$2);
   }

   private static <T> T[] createSortedArray(ToIntFunction<T> $$0, T[] $$1) {
      int $$2 = $$1.length;
      if ($$2 == 0) {
         throw new IllegalArgumentException("Empty value list");
      } else {
         T[] $$3 = (T[])$$1.clone();
         Arrays.fill($$3, null);

         for (T $$4 : $$1) {
            int $$5 = $$0.applyAsInt($$4);
            if ($$5 < 0 || $$5 >= $$2) {
               throw new IllegalArgumentException("Values are not continous, found index " + $$5 + " for value " + $$4);
            }

            T $$6 = $$3[$$5];
            if ($$6 != null) {
               throw new IllegalArgumentException("Duplicate entry on id " + $$5 + ": current=" + $$4 + ", previous=" + $$6);
            }

            $$3[$$5] = $$4;
         }

         for (int $$7 = 0; $$7 < $$2; $$7++) {
            if ($$3[$$7] == null) {
               throw new IllegalArgumentException("Missing value at index: " + $$7);
            }
         }

         return $$3;
      }
   }

   public static <T> IntFunction<T> continuous(ToIntFunction<T> $$0, T[] $$1, net.minecraft.util.ByIdMap.OutOfBoundsStrategy $$2) {
      T[] $$3 = createSortedArray($$0, $$1);
      int $$4 = $$3.length;

      return switch ($$2) {
         case ZERO -> {
            T $$5 = $$3[0];
            yield $$3x -> $$3x >= 0 && $$3x < $$4 ? $$3[$$3x] : $$5;
         }
         case WRAP -> $$2x -> $$3[net.minecraft.util.Mth.positiveModulo($$2x, $$4)];
         case CLAMP -> $$2x -> $$3[net.minecraft.util.Mth.clamp($$2x, 0, $$4 - 1)];
      };
   }

   public static enum OutOfBoundsStrategy {
      ZERO,
      WRAP,
      CLAMP;
   }
}
