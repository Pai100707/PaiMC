/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.CollectionContentsPredicate;
import net.minecraft.advancements.criterion.CollectionCountsPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;

public record CollectionPredicate<T, P extends Predicate<T>>(Optional<CollectionContentsPredicate<T, P>> contains, Optional<CollectionCountsPredicate<T, P>> counts, Optional<MinMaxBounds.Ints> size) implements Predicate<Iterable<T>>
{
    public static <T, P extends Predicate<T>> Codec<CollectionPredicate<T, P>> codec(Codec<P> $$0) {
        return RecordCodecBuilder.create($$1 -> $$1.group((App)CollectionContentsPredicate.codec($$0).optionalFieldOf("contains").forGetter(CollectionPredicate::contains), (App)CollectionCountsPredicate.codec($$0).optionalFieldOf("count").forGetter(CollectionPredicate::counts), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("size").forGetter(CollectionPredicate::size)).apply((Applicative)$$1, CollectionPredicate::new));
    }

    @Override
    public boolean test(Iterable<T> $$0) {
        if (this.contains.isPresent() && !this.contains.get().test($$0)) {
            return false;
        }
        if (this.counts.isPresent() && !this.counts.get().test($$0)) {
            return false;
        }
        return !this.size.isPresent() || this.size.get().matches(Iterables.size($$0));
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((Iterable)object);
    }
}

