package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionContentsPredicate<T, P extends Predicate<T>> extends Predicate<Iterable<T>> {
   List<P> unpack();

   static <T, P extends Predicate<T>> Codec<CollectionContentsPredicate<T, P>> codec(Codec<P> $$0) {
      return $$0.listOf().xmap(CollectionContentsPredicate::of, CollectionContentsPredicate::unpack);
   }

   @SafeVarargs
   static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(P... $$0) {
      return of(List.of($$0));
   }

   static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(List<P> $$0) {
      return (CollectionContentsPredicate<T, P>)(switch ($$0.size()) {
         case 0 -> new CollectionContentsPredicate.Zero();
         case 1 -> new CollectionContentsPredicate.Single($$0.getFirst());
         default -> new CollectionContentsPredicate.Multiple($$0);
      });
   }

   public record Multiple<T, P extends Predicate<T>>(List<P> tests) implements CollectionContentsPredicate<T, P> {
      public boolean test(Iterable<T> $$0) {
         List<Predicate<T>> $$1 = new ArrayList<>(this.tests);

         for (T $$2 : $$0) {
            $$1.removeIf($$1x -> $$1x.test($$2));
            if ($$1.isEmpty()) {
               return true;
            }
         }

         return false;
      }

      @Override
      public List<P> unpack() {
         return this.tests;
      }
   }

   public record Single<T, P extends Predicate<T>>(P test) implements CollectionContentsPredicate<T, P> {
      public boolean test(Iterable<T> $$0) {
         for (T $$1 : $$0) {
            if (this.test.test($$1)) {
               return true;
            }
         }

         return false;
      }

      @Override
      public List<P> unpack() {
         return List.of(this.test);
      }
   }

   public static class Zero<T, P extends Predicate<T>> implements CollectionContentsPredicate<T, P> {
      public boolean test(Iterable<T> $$0) {
         return true;
      }

      @Override
      public List<P> unpack() {
         return List.of();
      }
   }
}
