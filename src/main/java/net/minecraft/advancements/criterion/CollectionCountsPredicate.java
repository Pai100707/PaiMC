package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionCountsPredicate<T, P extends Predicate<T>> extends Predicate<Iterable<T>> {
   List<CollectionCountsPredicate.Entry<T, P>> unpack();

   static <T, P extends Predicate<T>> Codec<CollectionCountsPredicate<T, P>> codec(Codec<P> $$0) {
      return CollectionCountsPredicate.Entry.codec($$0).listOf().xmap(CollectionCountsPredicate::of, CollectionCountsPredicate::unpack);
   }

   @SafeVarargs
   static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> of(CollectionCountsPredicate.Entry<T, P>... $$0) {
      return of(List.of($$0));
   }

   static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> of(List<CollectionCountsPredicate.Entry<T, P>> $$0) {
      return (CollectionCountsPredicate<T, P>)(switch ($$0.size()) {
         case 0 -> new CollectionCountsPredicate.Zero();
         case 1 -> new CollectionCountsPredicate.Single($$0.getFirst());
         default -> new CollectionCountsPredicate.Multiple($$0);
      });
   }

   public record Entry<T, P extends Predicate<T>>(P test, MinMaxBounds.Ints count) {
      public static <T, P extends Predicate<T>> Codec<CollectionCountsPredicate.Entry<T, P>> codec(Codec<P> $$0) {
         return RecordCodecBuilder.create(
            $$1 -> $$1.group(
                  $$0.fieldOf("test").forGetter(CollectionCountsPredicate.Entry::test),
                  MinMaxBounds.Ints.CODEC.fieldOf("count").forGetter(CollectionCountsPredicate.Entry::count)
               )
               .apply($$1, CollectionCountsPredicate.Entry::new)
         );
      }

      public boolean test(Iterable<T> $$0) {
         int $$1 = 0;

         for (T $$2 : $$0) {
            if (this.test.test($$2)) {
               $$1++;
            }
         }

         return this.count.matches($$1);
      }
   }

   public record Multiple<T, P extends Predicate<T>>(List<CollectionCountsPredicate.Entry<T, P>> entries) implements CollectionCountsPredicate<T, P> {
      public boolean test(Iterable<T> $$0) {
         for (CollectionCountsPredicate.Entry<T, P> $$1 : this.entries) {
            if (!$$1.test($$0)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public List<CollectionCountsPredicate.Entry<T, P>> unpack() {
         return this.entries;
      }
   }

   public record Single<T, P extends Predicate<T>>(CollectionCountsPredicate.Entry<T, P> entry) implements CollectionCountsPredicate<T, P> {
      public boolean test(Iterable<T> $$0) {
         return this.entry.test($$0);
      }

      @Override
      public List<CollectionCountsPredicate.Entry<T, P>> unpack() {
         return List.of(this.entry);
      }
   }

   public static class Zero<T, P extends Predicate<T>> implements CollectionCountsPredicate<T, P> {
      public boolean test(Iterable<T> $$0) {
         return true;
      }

      @Override
      public List<CollectionCountsPredicate.Entry<T, P>> unpack() {
         return List.of();
      }
   }
}
