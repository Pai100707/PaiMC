/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionContentsPredicate<T, P extends Predicate<T>>
extends Predicate<Iterable<T>> {
    public List<P> unpack();

    public static <T, P extends Predicate<T>> Codec<CollectionContentsPredicate<T, P>> codec(Codec<P> $$0) {
        return $$0.listOf().xmap(CollectionContentsPredicate::of, CollectionContentsPredicate::unpack);
    }

    @SafeVarargs
    public static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(P ... $$0) {
        return CollectionContentsPredicate.of(List.of($$0));
    }

    public static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(List<P> $$0) {
        return switch ($$0.size()) {
            case 0 -> new Zero();
            case 1 -> new Single((Predicate)$$0.getFirst());
            default -> new Multiple($$0);
        };
    }

    public static class Zero<T, P extends Predicate<T>>
    implements CollectionContentsPredicate<T, P> {
        @Override
        public boolean test(Iterable<T> $$0) {
            return true;
        }

        @Override
        public List<P> unpack() {
            return List.of();
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }

    public record Single<T, P extends Predicate<T>>(P test) implements CollectionContentsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> $$0) {
            for (T $$1 : $$0) {
                if (!this.test.test($$1)) continue;
                return true;
            }
            return false;
        }

        @Override
        public List<P> unpack() {
            return List.of(this.test);
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }

    public record Multiple<T, P extends Predicate<T>>(List<P> tests) implements CollectionContentsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> $$0) {
            ArrayList<P> $$12 = new ArrayList<P>(this.tests);
            for (Object $$2 : $$0) {
                $$12.removeIf($$1 -> $$1.test($$2));
                if (!$$12.isEmpty()) continue;
                return true;
            }
            return false;
        }

        @Override
        public List<P> unpack() {
            return this.tests;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }
}

