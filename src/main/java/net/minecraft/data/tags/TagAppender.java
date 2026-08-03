package net.minecraft.data.tags;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public interface TagAppender<E, T> {
   TagAppender<E, T> add(E var1);

   default TagAppender<E, T> add(E... $$0) {
      return this.addAll(Arrays.stream($$0));
   }

   default TagAppender<E, T> addAll(Collection<E> $$0) {
      $$0.forEach(this::add);
      return this;
   }

   default TagAppender<E, T> addAll(Stream<E> $$0) {
      $$0.forEach(this::add);
      return this;
   }

   TagAppender<E, T> addOptional(E var1);

   TagAppender<E, T> addTag(TagKey<T> var1);

   TagAppender<E, T> addOptionalTag(TagKey<T> var1);

   static <T> TagAppender<ResourceKey<T>, T> forBuilder(final TagBuilder $$0) {
      return new TagAppender<ResourceKey<T>, T>() {
         public TagAppender<ResourceKey<T>, T> add(ResourceKey<T> $$0x) {
            $$0.addElement($$0.identifier());
            return this;
         }

         public TagAppender<ResourceKey<T>, T> addOptional(ResourceKey<T> $$0x) {
            $$0.addOptionalElement($$0.identifier());
            return this;
         }

         @Override
         public TagAppender<ResourceKey<T>, T> addTag(TagKey<T> $$0x) {
            $$0.addTag($$0.location());
            return this;
         }

         @Override
         public TagAppender<ResourceKey<T>, T> addOptionalTag(TagKey<T> $$0x) {
            $$0.addOptionalTag($$0.location());
            return this;
         }
      };
   }

   default <U> TagAppender<U, T> map(final Function<U, E> $$0) {
      final TagAppender<E, T> $$1 = this;
      return new TagAppender<U, T>() {
         @Override
         public TagAppender<U, T> add(U $$0x) {
            $$1.add($$0.apply($$0));
            return this;
         }

         @Override
         public TagAppender<U, T> addOptional(U $$0x) {
            $$1.add($$0.apply($$0));
            return this;
         }

         @Override
         public TagAppender<U, T> addTag(TagKey<T> $$0x) {
            $$1.addTag($$0);
            return this;
         }

         @Override
         public TagAppender<U, T> addOptionalTag(TagKey<T> $$0x) {
            $$1.addOptionalTag($$0);
            return this;
         }
      };
   }
}
