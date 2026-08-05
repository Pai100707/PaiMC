/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.MinMaxBounds;

public interface CollectionCountsPredicate<T, P extends Predicate<T>>
extends Predicate<Iterable<T>> {
    public List<Entry<T, P>> unpack();

    public static <T, P extends Predicate<T>> Codec<CollectionCountsPredicate<T, P>> codec(Codec<P> $$0) {
        return Entry.codec($$0).listOf().xmap(CollectionCountsPredicate::of, CollectionCountsPredicate::unpack);
    }

    @SafeVarargs
    public static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> of(Entry<T, P> ... $$0) {
        return CollectionCountsPredicate.of(List.of($$0));
    }

    public static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> of(List<Entry<T, P>> $$0) {
        return switch ($$0.size()) {
            case 0 -> new Zero();
            case 1 -> new Single<T, P>($$0.getFirst());
            default -> new Multiple<T, P>($$0);
        };
    }

    public record Entry<T, P extends Predicate<T>>(P test, MinMaxBounds.Ints count) {
        public static <T, P extends Predicate<T>> Codec<Entry<T, P>> codec(Codec<P> $$0) {
            return RecordCodecBuilder.create($$1 -> $$1.group((App)$$0.fieldOf("test").forGetter(Entry::test), (App)MinMaxBounds.Ints.CODEC.fieldOf("count").forGetter(Entry::count)).apply((Applicative)$$1, Entry::new));
        }

        public boolean test(Iterable<T> $$0) {
            int $$1 = 0;
            for (T $$2 : $$0) {
                if (!this.test.test($$2)) continue;
                ++$$1;
            }
            return this.count.matches($$1);
        }
    }

    public static class Zero<T, P extends Predicate<T>>
    implements CollectionCountsPredicate<T, P> {
        @Override
        public boolean test(Iterable<T> $$0) {
            return true;
        }

        @Override
        public List<Entry<T, P>> unpack() {
            return List.of();
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }

    public record Single<T, P extends Predicate<T>>(Entry<T, P> entry) implements CollectionCountsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> $$0) {
            return this.entry.test($$0);
        }

        @Override
        public List<Entry<T, P>> unpack() {
            return List.of(this.entry);
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }

    public record Multiple<T, P extends Predicate<T>>(List<Entry<T, P>> entries) implements CollectionCountsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> $$0) {
            for (Entry<T, P> $$1 : this.entries) {
                if ($$1.test($$0)) continue;
                return false;
            }
            return true;
        }

        @Override
        public List<Entry<T, P>> unpack() {
            return this.entries;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }
}

