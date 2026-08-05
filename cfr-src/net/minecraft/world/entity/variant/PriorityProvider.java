/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.variant;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

public interface PriorityProvider<Context, Condition extends SelectorCondition<Context>> {
    public List<Selector<Context, Condition>> selectors();

    public static <C, T> Stream<T> select(Stream<T> $$0, Function<T, PriorityProvider<C, ?>> $$1, C $$22) {
        ArrayList $$3 = new ArrayList();
        $$0.forEach($$2 -> {
            PriorityProvider $$3 = (PriorityProvider)$$1.apply($$2);
            for (Selector $$4 : $$3.selectors()) {
                $$3.add(new UnpackedEntry($$2, $$4.priority(), (SelectorCondition)DataFixUtils.orElseGet($$4.condition(), SelectorCondition::alwaysTrue)));
            }
        });
        $$3.sort(UnpackedEntry.HIGHEST_PRIORITY_FIRST);
        Iterator $$4 = $$3.iterator();
        int $$5 = Integer.MIN_VALUE;
        while ($$4.hasNext()) {
            UnpackedEntry $$6 = (UnpackedEntry)$$4.next();
            if ($$6.priority < $$5) {
                $$4.remove();
                continue;
            }
            if ($$6.condition.test($$22)) {
                $$5 = $$6.priority;
                continue;
            }
            $$4.remove();
        }
        return $$3.stream().map(UnpackedEntry::entry);
    }

    public static <C, T> Optional<T> pick(Stream<T> $$0, Function<T, PriorityProvider<C, ?>> $$1, RandomSource $$2, C $$3) {
        List<T> $$4 = PriorityProvider.select($$0, $$1, $$3).toList();
        return Util.getRandomSafe($$4, $$2);
    }

    public static <Context, Condition extends SelectorCondition<Context>> List<Selector<Context, Condition>> single(Condition $$0, int $$1) {
        return List.of(new Selector($$0, $$1));
    }

    public static <Context, Condition extends SelectorCondition<Context>> List<Selector<Context, Condition>> alwaysTrue(int $$0) {
        return List.of(new Selector(Optional.empty(), $$0));
    }

    public static final class UnpackedEntry<C, T>
    extends Record {
        private final T entry;
        final int priority;
        final SelectorCondition<C> condition;
        public static final Comparator<UnpackedEntry<?, ?>> HIGHEST_PRIORITY_FIRST = Comparator.comparingInt(UnpackedEntry::priority).reversed();

        public UnpackedEntry(T $$0, int $$1, SelectorCondition<C> $$2) {
            this.entry = $$0;
            this.priority = $$1;
            this.condition = $$2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{UnpackedEntry.class, "entry;priority;condition", "entry", "priority", "condition"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{UnpackedEntry.class, "entry;priority;condition", "entry", "priority", "condition"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{UnpackedEntry.class, "entry;priority;condition", "entry", "priority", "condition"}, this, $$0);
        }

        public T entry() {
            return this.entry;
        }

        public int priority() {
            return this.priority;
        }

        public SelectorCondition<C> condition() {
            return this.condition;
        }
    }

    @FunctionalInterface
    public static interface SelectorCondition<C>
    extends Predicate<C> {
        public static <C> SelectorCondition<C> alwaysTrue() {
            return $$0 -> true;
        }
    }

    public record Selector<Context, Condition extends SelectorCondition<Context>>(Optional<Condition> condition, int priority) {
        public Selector(Condition $$0, int $$1) {
            this(Optional.of($$0), $$1);
        }

        public Selector(int $$0) {
            this(Optional.empty(), $$0);
        }

        public static <Context, Condition extends SelectorCondition<Context>> Codec<Selector<Context, Condition>> codec(Codec<Condition> $$0) {
            return RecordCodecBuilder.create($$1 -> $$1.group((App)$$0.optionalFieldOf("condition").forGetter(Selector::condition), (App)Codec.INT.fieldOf("priority").forGetter(Selector::priority)).apply((Applicative)$$1, Selector::new));
        }
    }
}

