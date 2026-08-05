package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface Holder<T> {
   T value();

   boolean isBound();

   boolean is(Identifier var1);

   boolean is(ResourceKey<T> var1);

   boolean is(Predicate<ResourceKey<T>> var1);

   boolean is(TagKey<T> var1);

   @Deprecated
   boolean is(net.minecraft.core.Holder<T> var1);

   Stream<TagKey<T>> tags();

   Either<ResourceKey<T>, T> unwrap();

   Optional<ResourceKey<T>> unwrapKey();

   net.minecraft.core.Holder.Kind kind();

   boolean canSerializeIn(net.minecraft.core.HolderOwner<T> var1);

   default String getRegisteredName() {
      return this.unwrapKey().map($$0 -> $$0.identifier().toString()).orElse("[unregistered]");
   }

   static <T> net.minecraft.core.Holder<T> direct(T $$0) {
      return new net.minecraft.core.Holder.Direct<>($$0);
   }

   public record Direct<T>(T value) implements net.minecraft.core.Holder<T> {
      @Override
      public boolean isBound() {
         return true;
      }

      @Override
      public boolean is(Identifier $$0) {
         return false;
      }

      @Override
      public boolean is(ResourceKey<T> $$0) {
         return false;
      }

      @Override
      public boolean is(TagKey<T> $$0) {
         return false;
      }

      @Override
      public boolean is(net.minecraft.core.Holder<T> $$0) {
         return this.value.equals($$0.value());
      }

      @Override
      public boolean is(Predicate<ResourceKey<T>> $$0) {
         return false;
      }

      @Override
      public Either<ResourceKey<T>, T> unwrap() {
         return Either.right(this.value);
      }

      @Override
      public Optional<ResourceKey<T>> unwrapKey() {
         return Optional.empty();
      }

      @Override
      public net.minecraft.core.Holder.Kind kind() {
         return net.minecraft.core.Holder.Kind.DIRECT;
      }

      @Override
      public String toString() {
         return "Direct{" + this.value + "}";
      }

      @Override
      public boolean canSerializeIn(net.minecraft.core.HolderOwner<T> $$0) {
         return true;
      }

      @Override
      public Stream<TagKey<T>> tags() {
         return Stream.of();
      }
   }

   public static enum Kind {
      REFERENCE,
      DIRECT;
   }

   public static class Reference<T> implements net.minecraft.core.Holder<T> {
      private final net.minecraft.core.HolderOwner<T> owner;
      
      private Set<TagKey<T>> tags;
      private final net.minecraft.core.Holder.Reference.Type type;
      
      private ResourceKey<T> key;
      
      private T value;

      protected Reference(net.minecraft.core.Holder.Reference.Type $$0, net.minecraft.core.HolderOwner<T> $$1, ResourceKey<T> $$2, T $$3) {
         this.owner = $$1;
         this.type = $$0;
         this.key = $$2;
         this.value = $$3;
      }

      public static <T> net.minecraft.core.Holder.Reference<T> createStandAlone(net.minecraft.core.HolderOwner<T> $$0, ResourceKey<T> $$1) {
         return new net.minecraft.core.Holder.Reference<>(net.minecraft.core.Holder.Reference.Type.STAND_ALONE, $$0, $$1, null);
      }

      @Deprecated
      public static <T> net.minecraft.core.Holder.Reference<T> createIntrusive(net.minecraft.core.HolderOwner<T> $$0, T $$1) {
         return new net.minecraft.core.Holder.Reference<>(net.minecraft.core.Holder.Reference.Type.INTRUSIVE, $$0, null, $$1);
      }

      public ResourceKey<T> key() {
         if (this.key == null) {
            throw new IllegalStateException("Trying to access unbound value '" + this.value + "' from registry " + this.owner);
         } else {
            return this.key;
         }
      }

      @Override
      public T value() {
         if (this.value == null) {
            throw new IllegalStateException("Trying to access unbound value '" + this.key + "' from registry " + this.owner);
         } else {
            return this.value;
         }
      }

      @Override
      public boolean is(Identifier $$0) {
         return this.key().identifier().equals($$0);
      }

      @Override
      public boolean is(ResourceKey<T> $$0) {
         return this.key() == $$0;
      }

      private Set<TagKey<T>> boundTags() {
         if (this.tags == null) {
            throw new IllegalStateException("Tags not bound");
         } else {
            return this.tags;
         }
      }

      @Override
      public boolean is(TagKey<T> $$0) {
         return this.boundTags().contains($$0);
      }

      @Override
      public boolean is(net.minecraft.core.Holder<T> $$0) {
         return $$0.is(this.key());
      }

      @Override
      public boolean is(Predicate<ResourceKey<T>> $$0) {
         return $$0.test(this.key());
      }

      @Override
      public boolean canSerializeIn(net.minecraft.core.HolderOwner<T> $$0) {
         return this.owner.canSerializeIn($$0);
      }

      @Override
      public Either<ResourceKey<T>, T> unwrap() {
         return Either.left(this.key());
      }

      @Override
      public Optional<ResourceKey<T>> unwrapKey() {
         return Optional.of(this.key());
      }

      @Override
      public net.minecraft.core.Holder.Kind kind() {
         return net.minecraft.core.Holder.Kind.REFERENCE;
      }

      @Override
      public boolean isBound() {
         return this.key != null && this.value != null;
      }

      void bindKey(ResourceKey<T> $$0) {
         if (this.key != null && $$0 != this.key) {
            throw new IllegalStateException("Can't change holder key: existing=" + this.key + ", new=" + $$0);
         } else {
            this.key = $$0;
         }
      }

      protected void bindValue(T $$0) {
         if (this.type == net.minecraft.core.Holder.Reference.Type.INTRUSIVE && this.value != $$0) {
            throw new IllegalStateException("Can't change holder " + this.key + " value: existing=" + this.value + ", new=" + $$0);
         } else {
            this.value = $$0;
         }
      }

      void bindTags(Collection<TagKey<T>> $$0) {
         this.tags = Set.copyOf($$0);
      }

      @Override
      public Stream<TagKey<T>> tags() {
         return this.boundTags().stream();
      }

      @Override
      public String toString() {
         return "Reference{" + this.key + "=" + this.value + "}";
      }

      protected static enum Type {
         STAND_ALONE,
         INTRUSIVE;
      }
   }
}
