/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tags;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public interface TagAppender<E, T> {
    public TagAppender<E, T> add(E var1);

    default public TagAppender<E, T> add(E ... $$0) {
        return this.addAll(Arrays.stream($$0));
    }

    default public TagAppender<E, T> addAll(Collection<E> $$0) {
        $$0.forEach(this::add);
        return this;
    }

    default public TagAppender<E, T> addAll(Stream<E> $$0) {
        $$0.forEach(this::add);
        return this;
    }

    public TagAppender<E, T> addOptional(E var1);

    public TagAppender<E, T> addTag(TagKey<T> var1);

    public TagAppender<E, T> addOptionalTag(TagKey<T> var1);

    public static <T> TagAppender<ResourceKey<T>, T> forBuilder(final TagBuilder $$0) {
        return new TagAppender<ResourceKey<T>, T>(){

            @Override
            public TagAppender<ResourceKey<T>, T> add(ResourceKey<T> $$02) {
                $$0.addElement($$02.identifier());
                return this;
            }

            @Override
            public TagAppender<ResourceKey<T>, T> addOptional(ResourceKey<T> $$02) {
                $$0.addOptionalElement($$02.identifier());
                return this;
            }

            @Override
            public TagAppender<ResourceKey<T>, T> addTag(TagKey<T> $$02) {
                $$0.addTag($$02.location());
                return this;
            }

            @Override
            public TagAppender<ResourceKey<T>, T> addOptionalTag(TagKey<T> $$02) {
                $$0.addOptionalTag($$02.location());
                return this;
            }
        };
    }

    default public <U> TagAppender<U, T> map(final Function<U, E> $$0) {
        final TagAppender $$1 = this;
        return new TagAppender<U, T>(this){

            @Override
            public TagAppender<U, T> add(U $$02) {
                $$1.add($$0.apply($$02));
                return this;
            }

            @Override
            public TagAppender<U, T> addOptional(U $$02) {
                $$1.add($$0.apply($$02));
                return this;
            }

            @Override
            public TagAppender<U, T> addTag(TagKey<T> $$02) {
                $$1.addTag($$02);
                return this;
            }

            @Override
            public TagAppender<U, T> addOptionalTag(TagKey<T> $$02) {
                $$1.addOptionalTag($$02);
                return this;
            }
        };
    }
}

