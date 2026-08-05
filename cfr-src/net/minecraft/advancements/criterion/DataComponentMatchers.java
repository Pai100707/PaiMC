/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DataComponentMatchers(DataComponentExactPredicate exact, Map<DataComponentPredicate.Type<?>, DataComponentPredicate> partial) implements Predicate<DataComponentGetter>
{
    public static final DataComponentMatchers ANY = new DataComponentMatchers(DataComponentExactPredicate.EMPTY, Map.of());
    public static final MapCodec<DataComponentMatchers> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)DataComponentExactPredicate.CODEC.optionalFieldOf("components", (Object)DataComponentExactPredicate.EMPTY).forGetter(DataComponentMatchers::exact), (App)DataComponentPredicate.CODEC.optionalFieldOf("predicates", Map.of()).forGetter(DataComponentMatchers::partial)).apply((Applicative)$$0, DataComponentMatchers::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentMatchers> STREAM_CODEC = StreamCodec.composite(DataComponentExactPredicate.STREAM_CODEC, DataComponentMatchers::exact, DataComponentPredicate.STREAM_CODEC, DataComponentMatchers::partial, DataComponentMatchers::new);

    @Override
    public boolean test(DataComponentGetter $$0) {
        if (!this.exact.test($$0)) {
            return false;
        }
        for (DataComponentPredicate $$1 : this.partial.values()) {
            if ($$1.matches($$0)) continue;
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return this.exact.isEmpty() && this.partial.isEmpty();
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((DataComponentGetter)object);
    }

    public static class Builder {
        private DataComponentExactPredicate exact = DataComponentExactPredicate.EMPTY;
        private final ImmutableMap.Builder<DataComponentPredicate.Type<?>, DataComponentPredicate> partial = ImmutableMap.builder();

        private Builder() {
        }

        public static Builder components() {
            return new Builder();
        }

        public <T extends DataComponentType<?>> Builder any(DataComponentType<?> $$0) {
            DataComponentPredicate.AnyValueType $$1 = DataComponentPredicate.AnyValueType.create($$0);
            this.partial.put((Object)$$1, (Object)$$1.predicate());
            return this;
        }

        public <T extends DataComponentPredicate> Builder partial(DataComponentPredicate.Type<T> $$0, T $$1) {
            this.partial.put($$0, $$1);
            return this;
        }

        public Builder exact(DataComponentExactPredicate $$0) {
            this.exact = $$0;
            return this;
        }

        public DataComponentMatchers build() {
            return new DataComponentMatchers(this.exact, (Map<DataComponentPredicate.Type<?>, DataComponentPredicate>)this.partial.buildOrThrow());
        }
    }
}

