package net.minecraft.core;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

public interface HolderSet<T> extends Iterable<net.minecraft.core.Holder<T>> {
   Stream<net.minecraft.core.Holder<T>> stream();

   int size();

   boolean isBound();

   Either<TagKey<T>, List<net.minecraft.core.Holder<T>>> unwrap();

   Optional<net.minecraft.core.Holder<T>> getRandomElement(RandomSource var1);

   net.minecraft.core.Holder<T> get(int var1);

   boolean contains(net.minecraft.core.Holder<T> var1);

   boolean canSerializeIn(net.minecraft.core.HolderOwner<T> var1);

   Optional<TagKey<T>> unwrapKey();

   @Deprecated
   @VisibleForTesting
   static <T> net.minecraft.core.HolderSet.Named<T> emptyNamed(net.minecraft.core.HolderOwner<T> $$0, TagKey<T> $$1) {
      return new net.minecraft.core.HolderSet.Named<T>($$0, $$1) {
         @Override
         protected List<net.minecraft.core.Holder<T>> contents() {
            throw new UnsupportedOperationException("Tag " + this.key() + " can't be dereferenced during construction");
         }
      };
   }

   static <T> net.minecraft.core.HolderSet<T> empty() {
      return (net.minecraft.core.HolderSet<T>)net.minecraft.core.HolderSet.Direct.EMPTY;
   }

   @SafeVarargs
   static <T> net.minecraft.core.HolderSet.Direct<T> direct(net.minecraft.core.Holder<T>... $$0) {
      return new net.minecraft.core.HolderSet.Direct<>(List.of($$0));
   }

   static <T> net.minecraft.core.HolderSet.Direct<T> direct(List<? extends net.minecraft.core.Holder<T>> $$0) {
      return new net.minecraft.core.HolderSet.Direct<>(List.copyOf($$0));
   }

   @SafeVarargs
   static <E, T> net.minecraft.core.HolderSet.Direct<T> direct(Function<E, net.minecraft.core.Holder<T>> $$0, E... $$1) {
      return direct(Stream.of($$1).map($$0).toList());
   }

   static <E, T> net.minecraft.core.HolderSet.Direct<T> direct(Function<E, net.minecraft.core.Holder<T>> $$0, Collection<E> $$1) {
      return direct($$1.stream().map($$0).toList());
   }

   public static final class Direct<T> extends net.minecraft.core.HolderSet.ListBacked<T> {
      static final net.minecraft.core.HolderSet.Direct<?> EMPTY = new net.minecraft.core.HolderSet.Direct(List.of());
      private final List<net.minecraft.core.Holder<T>> contents;
      
      private Set<net.minecraft.core.Holder<T>> contentsSet;

      Direct(List<net.minecraft.core.Holder<T>> $$0) {
         this.contents = $$0;
      }

      @Override
      protected List<net.minecraft.core.Holder<T>> contents() {
         return this.contents;
      }

      @Override
      public boolean isBound() {
         return true;
      }

      @Override
      public Either<TagKey<T>, List<net.minecraft.core.Holder<T>>> unwrap() {
         return Either.right(this.contents);
      }

      @Override
      public Optional<TagKey<T>> unwrapKey() {
         return Optional.empty();
      }

      @Override
      public boolean contains(net.minecraft.core.Holder<T> $$0) {
         if (this.contentsSet == null) {
            this.contentsSet = Set.copyOf(this.contents);
         }

         return this.contentsSet.contains($$0);
      }

      @Override
      public String toString() {
         return "DirectSet[" + this.contents + "]";
      }

      @Override
      public boolean equals(Object $$0) {
         return this == $$0 ? true : $$0 instanceof net.minecraft.core.HolderSet.Direct<?> $$1 && this.contents.equals($$1.contents);
      }

      @Override
      public int hashCode() {
         return this.contents.hashCode();
      }
   }

   public abstract static class ListBacked<T> implements net.minecraft.core.HolderSet<T> {
      protected abstract List<net.minecraft.core.Holder<T>> contents();

      @Override
      public int size() {
         return this.contents().size();
      }

      @Override
      public Spliterator<net.minecraft.core.Holder<T>> spliterator() {
         return this.contents().spliterator();
      }

      @Override
      public Iterator<net.minecraft.core.Holder<T>> iterator() {
         return this.contents().iterator();
      }

      @Override
      public Stream<net.minecraft.core.Holder<T>> stream() {
         return this.contents().stream();
      }

      @Override
      public Optional<net.minecraft.core.Holder<T>> getRandomElement(RandomSource $$0) {
         return Util.getRandomSafe(this.contents(), $$0);
      }

      @Override
      public net.minecraft.core.Holder<T> get(int $$0) {
         return this.contents().get($$0);
      }

      @Override
      public boolean canSerializeIn(net.minecraft.core.HolderOwner<T> $$0) {
         return true;
      }
   }

   public static class Named<T> extends net.minecraft.core.HolderSet.ListBacked<T> {
      private final net.minecraft.core.HolderOwner<T> owner;
      private final TagKey<T> key;
      
      private List<net.minecraft.core.Holder<T>> contents;

      Named(net.minecraft.core.HolderOwner<T> $$0, TagKey<T> $$1) {
         this.owner = $$0;
         this.key = $$1;
      }

      void bind(List<net.minecraft.core.Holder<T>> $$0) {
         this.contents = List.copyOf($$0);
      }

      public TagKey<T> key() {
         return this.key;
      }

      @Override
      protected List<net.minecraft.core.Holder<T>> contents() {
         if (this.contents == null) {
            throw new IllegalStateException("Trying to access unbound tag '" + this.key + "' from registry " + this.owner);
         } else {
            return this.contents;
         }
      }

      @Override
      public boolean isBound() {
         return this.contents != null;
      }

      @Override
      public Either<TagKey<T>, List<net.minecraft.core.Holder<T>>> unwrap() {
         return Either.left(this.key);
      }

      @Override
      public Optional<TagKey<T>> unwrapKey() {
         return Optional.of(this.key);
      }

      @Override
      public boolean contains(net.minecraft.core.Holder<T> $$0) {
         return $$0.is(this.key);
      }

      @Override
      public String toString() {
         return "NamedSet(" + this.key + ")[" + this.contents + "]";
      }

      @Override
      public boolean canSerializeIn(net.minecraft.core.HolderOwner<T> $$0) {
         return this.owner.canSerializeIn($$0);
      }
   }
}
