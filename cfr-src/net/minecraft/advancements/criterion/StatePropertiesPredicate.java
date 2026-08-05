/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public record StatePropertiesPredicate(List<PropertyMatcher> properties) {
    private static final Codec<List<PropertyMatcher>> PROPERTIES_CODEC = Codec.unboundedMap((Codec)Codec.STRING, ValueMatcher.CODEC).xmap($$02 -> $$02.entrySet().stream().map($$0 -> new PropertyMatcher((String)$$0.getKey(), (ValueMatcher)$$0.getValue())).toList(), $$0 -> $$0.stream().collect(Collectors.toMap(PropertyMatcher::name, PropertyMatcher::valueMatcher)));
    public static final Codec<StatePropertiesPredicate> CODEC = PROPERTIES_CODEC.xmap(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);
    public static final StreamCodec<ByteBuf, StatePropertiesPredicate> STREAM_CODEC = PropertyMatcher.STREAM_CODEC.apply(ByteBufCodecs.list()).map(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);

    public <S extends StateHolder<?, S>> boolean matches(StateDefinition<?, S> $$0, S $$1) {
        for (PropertyMatcher $$2 : this.properties) {
            if ($$2.match($$0, $$1)) continue;
            return false;
        }
        return true;
    }

    public boolean matches(BlockState $$0) {
        return this.matches($$0.getBlock().getStateDefinition(), $$0);
    }

    public boolean matches(FluidState $$0) {
        return this.matches($$0.getType().getStateDefinition(), $$0);
    }

    public Optional<String> checkState(StateDefinition<?, ?> $$0) {
        for (PropertyMatcher $$1 : this.properties) {
            Optional<String> $$2 = $$1.checkState($$0);
            if (!$$2.isPresent()) continue;
            return $$2;
        }
        return Optional.empty();
    }

    record PropertyMatcher(String name, ValueMatcher valueMatcher) {
        public static final StreamCodec<ByteBuf, PropertyMatcher> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PropertyMatcher::name, ValueMatcher.STREAM_CODEC, PropertyMatcher::valueMatcher, PropertyMatcher::new);

        public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> $$0, S $$1) {
            Property<?> $$2 = $$0.getProperty(this.name);
            return $$2 != null && this.valueMatcher.match($$1, $$2);
        }

        public Optional<String> checkState(StateDefinition<?, ?> $$0) {
            Property<?> $$1 = $$0.getProperty(this.name);
            return $$1 != null ? Optional.empty() : Optional.of(this.name);
        }
    }

    static interface ValueMatcher {
        public static final Codec<ValueMatcher> CODEC = Codec.either(ExactMatcher.CODEC, RangedMatcher.CODEC).xmap(Either::unwrap, $$0 -> {
            if ($$0 instanceof ExactMatcher) {
                ExactMatcher $$1 = (ExactMatcher)$$0;
                return Either.left((Object)$$1);
            }
            if ($$0 instanceof RangedMatcher) {
                RangedMatcher $$2 = (RangedMatcher)$$0;
                return Either.right((Object)$$2);
            }
            throw new UnsupportedOperationException();
        });
        public static final StreamCodec<ByteBuf, ValueMatcher> STREAM_CODEC = ByteBufCodecs.either(ExactMatcher.STREAM_CODEC, RangedMatcher.STREAM_CODEC).map(Either::unwrap, $$0 -> {
            if ($$0 instanceof ExactMatcher) {
                ExactMatcher $$1 = (ExactMatcher)$$0;
                return Either.left((Object)$$1);
            }
            if ($$0 instanceof RangedMatcher) {
                RangedMatcher $$2 = (RangedMatcher)$$0;
                return Either.right((Object)$$2);
            }
            throw new UnsupportedOperationException();
        });

        public <T extends Comparable<T>> boolean match(StateHolder<?, ?> var1, Property<T> var2);
    }

    public static class Builder {
        private final ImmutableList.Builder<PropertyMatcher> matchers = ImmutableList.builder();

        private Builder() {
        }

        public static Builder properties() {
            return new Builder();
        }

        public Builder hasProperty(Property<?> $$0, String $$1) {
            this.matchers.add((Object)new PropertyMatcher($$0.getName(), new ExactMatcher($$1)));
            return this;
        }

        public Builder hasProperty(Property<Integer> $$0, int $$1) {
            return this.hasProperty((Property)$$0, (Comparable<T> & StringRepresentable)Integer.toString($$1));
        }

        public Builder hasProperty(Property<Boolean> $$0, boolean $$1) {
            return this.hasProperty((Property)$$0, (Comparable<T> & StringRepresentable)Boolean.toString($$1));
        }

        public <T extends Comparable<T> & StringRepresentable> Builder hasProperty(Property<T> $$0, T $$1) {
            return this.hasProperty($$0, (T)((StringRepresentable)$$1).getSerializedName());
        }

        public Optional<StatePropertiesPredicate> build() {
            return Optional.of(new StatePropertiesPredicate((List<PropertyMatcher>)this.matchers.build()));
        }
    }

    record RangedMatcher(Optional<String> minValue, Optional<String> maxValue) implements ValueMatcher
    {
        public static final Codec<RangedMatcher> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.STRING.optionalFieldOf("min").forGetter(RangedMatcher::minValue), (App)Codec.STRING.optionalFieldOf("max").forGetter(RangedMatcher::maxValue)).apply((Applicative)$$0, RangedMatcher::new));
        public static final StreamCodec<ByteBuf, RangedMatcher> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), RangedMatcher::minValue, ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), RangedMatcher::maxValue, RangedMatcher::new);

        @Override
        public <T extends Comparable<T>> boolean match(StateHolder<?, ?> $$0, Property<T> $$1) {
            Optional<T> $$4;
            Optional<T> $$3;
            Comparable $$2 = $$0.getValue($$1);
            if (this.minValue.isPresent() && (($$3 = $$1.getValue(this.minValue.get())).isEmpty() || $$2.compareTo((Comparable)((Comparable)$$3.get())) < 0)) {
                return false;
            }
            return !this.maxValue.isPresent() || !($$4 = $$1.getValue(this.maxValue.get())).isEmpty() && $$2.compareTo((Comparable)((Comparable)$$4.get())) <= 0;
        }
    }

    record ExactMatcher(String value) implements ValueMatcher
    {
        public static final Codec<ExactMatcher> CODEC = Codec.STRING.xmap(ExactMatcher::new, ExactMatcher::value);
        public static final StreamCodec<ByteBuf, ExactMatcher> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ExactMatcher::new, ExactMatcher::value);

        @Override
        public <T extends Comparable<T>> boolean match(StateHolder<?, ?> $$0, Property<T> $$1) {
            Comparable $$2 = $$0.getValue($$1);
            Optional<T> $$3 = $$1.getValue(this.value);
            return $$3.isPresent() && $$2.compareTo((Comparable)((Comparable)$$3.get())) == 0;
        }
    }
}

