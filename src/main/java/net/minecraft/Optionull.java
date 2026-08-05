package net.minecraft;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class Optionull {
   @Deprecated
   public static <T> T orElse(T $$0, T $$1) {
      return Objects.requireNonNullElse($$0, $$1);
   }

   
   public static <T, R> R map(T $$0, Function<T, R> $$1) {
      return $$0 == null ? null : $$1.apply($$0);
   }

   public static <T, R> R mapOrDefault(T $$0, Function<T, R> $$1, R $$2) {
      return $$0 == null ? $$2 : $$1.apply($$0);
   }

   public static <T, R> R mapOrElse(T $$0, Function<T, R> $$1, Supplier<R> $$2) {
      return $$0 == null ? $$2.get() : $$1.apply($$0);
   }

   
   public static <T> T first(Collection<T> $$0) {
      Iterator<T> $$1 = $$0.iterator();
      return $$1.hasNext() ? $$1.next() : null;
   }

   public static <T> T firstOrDefault(Collection<T> $$0, T $$1) {
      Iterator<T> $$2 = $$0.iterator();
      return $$2.hasNext() ? $$2.next() : $$1;
   }

   public static <T> T firstOrElse(Collection<T> $$0, Supplier<T> $$1) {
      Iterator<T> $$2 = $$0.iterator();
      return $$2.hasNext() ? $$2.next() : $$1.get();
   }

   public static <T> boolean isNullOrEmpty(T[] $$0) {
      return $$0 == null || $$0.length == 0;
   }

   public static boolean isNullOrEmpty(boolean[] $$0) {
      return $$0 == null || $$0.length == 0;
   }

   public static boolean isNullOrEmpty(byte[] $$0) {
      return $$0 == null || $$0.length == 0;
   }

   public static boolean isNullOrEmpty(char[] $$0) {
      return $$0 == null || $$0.length == 0;
   }

   public static boolean isNullOrEmpty(short[] $$0) {
      return $$0 == null || $$0.length == 0;
   }

   public static boolean isNullOrEmpty(int[] $$0) {
      return $$0 == null || $$0.length == 0;
   }

   public static boolean isNullOrEmpty(long[] $$0) {
      return $$0 == null || $$0.length == 0;
   }

   public static boolean isNullOrEmpty(float[] $$0) {
      return $$0 == null || $$0.length == 0;
   }

   public static boolean isNullOrEmpty(double[] $$0) {
      return $$0 == null || $$0.length == 0;
   }
}
