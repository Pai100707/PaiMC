/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.BuiltInExceptionProvider
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.advancements.criterion;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public interface MinMaxBounds<T extends Number> {
    public static final SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType((Message)Component.translatable("argument.range.empty"));
    public static final SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType((Message)Component.translatable("argument.range.swapped"));

    public Bounds<T> bounds();

    default public Optional<T> min() {
        return this.bounds().min;
    }

    default public Optional<T> max() {
        return this.bounds().max;
    }

    default public boolean isAny() {
        return this.bounds().isAny();
    }

    public static final class Bounds<T extends Number>
    extends Record {
        final Optional<T> min;
        final Optional<T> max;

        public Bounds(Optional<T> $$0, Optional<T> $$1) {
            this.min = $$0;
            this.max = $$1;
        }

        public boolean isAny() {
            return this.min().isEmpty() && this.max().isEmpty();
        }

        public DataResult<Bounds<T>> validateSwappedBoundsInCodec() {
            if (this.areSwapped()) {
                return DataResult.error(() -> "Swapped bounds in range: " + String.valueOf(this.min()) + " is higher than " + String.valueOf(this.max()));
            }
            return DataResult.success((Object)this);
        }

        public boolean areSwapped() {
            return this.min.isPresent() && this.max.isPresent() && ((Comparable)((Object)((Number)this.min.get()))).compareTo((Number)this.max.get()) > 0;
        }

        public Optional<T> asPoint() {
            Optional<T> $$1;
            Optional<T> $$0 = this.min();
            return $$0.equals($$1 = this.max()) ? $$0 : Optional.empty();
        }

        public static <T extends Number> Bounds<T> any() {
            return new Bounds(Optional.empty(), Optional.empty());
        }

        public static <T extends Number> Bounds<T> exactly(T $$0) {
            Optional<T> $$1 = Optional.of($$0);
            return new Bounds<T>($$1, $$1);
        }

        public static <T extends Number> Bounds<T> between(T $$0, T $$1) {
            return new Bounds<T>(Optional.of($$0), Optional.of($$1));
        }

        public static <T extends Number> Bounds<T> atLeast(T $$0) {
            return new Bounds<T>(Optional.of($$0), Optional.empty());
        }

        public static <T extends Number> Bounds<T> atMost(T $$0) {
            return new Bounds(Optional.empty(), Optional.of($$0));
        }

        public <U extends Number> Bounds<U> map(Function<T, U> $$0) {
            return new Bounds<U>(this.min.map($$0), this.max.map($$0));
        }

        static <T extends Number> Codec<Bounds<T>> createCodec(Codec<T> $$03) {
            Codec $$12 = RecordCodecBuilder.create($$1 -> $$1.group((App)$$03.optionalFieldOf("min").forGetter(Bounds::min), (App)$$03.optionalFieldOf("max").forGetter(Bounds::max)).apply((Applicative)$$1, Bounds::new));
            return Codec.either((Codec)$$12, $$03).xmap($$02 -> (Bounds)$$02.map($$0 -> $$0, $$0 -> Bounds.exactly((Number)$$0)), $$0 -> {
                Optional $$1 = $$0.asPoint();
                return $$1.isPresent() ? Either.right((Object)((Number)$$1.get())) : Either.left((Object)$$0);
            });
        }

        static <B extends ByteBuf, T extends Number> StreamCodec<B, Bounds<T>> createStreamCodec(final StreamCodec<B, T> $$0) {
            return new StreamCodec<B, Bounds<T>>(){
                private static final int MIN_FLAG = 1;
                private static final int MAX_FLAG = 2;

                @Override
                public Bounds<T> decode(B $$02) {
                    byte $$1 = $$02.readByte();
                    Optional $$2 = ($$1 & 1) != 0 ? Optional.of((Number)$$0.decode($$02)) : Optional.empty();
                    Optional $$3 = ($$1 & 2) != 0 ? Optional.of((Number)$$0.decode($$02)) : Optional.empty();
                    return new Bounds($$2, $$3);
                }

                @Override
                public void encode(B $$02, Bounds<T> $$1) {
                    Optional<Number> $$22 = $$1.min();
                    Optional<Number> $$3 = $$1.max();
                    $$02.writeByte(($$22.isPresent() ? 1 : 0) | ($$3.isPresent() ? 2 : 0));
                    $$22.ifPresent($$2 -> $$0.encode($$02, $$2));
                    $$3.ifPresent($$2 -> $$0.encode($$02, $$2));
                }

                @Override
                public /* synthetic */ void encode(Object object, Object object2) {
                    this.encode((Object)((ByteBuf)object), (Bounds)object2);
                }

                @Override
                public /* synthetic */ Object decode(Object object) {
                    return this.decode((B)((ByteBuf)object));
                }
            };
        }

        public static <T extends Number> Bounds<T> fromReader(StringReader $$0, Function<String, T> $$1, Supplier<DynamicCommandExceptionType> $$2) throws CommandSyntaxException {
            if (!$$0.canRead()) {
                throw ERROR_EMPTY.createWithContext((ImmutableStringReader)$$0);
            }
            int $$3 = $$0.getCursor();
            try {
                Optional<T> $$6;
                Optional<T> $$4 = Bounds.readNumber($$0, $$1, $$2);
                if ($$0.canRead(2) && $$0.peek() == '.' && $$0.peek(1) == '.') {
                    $$0.skip();
                    $$0.skip();
                    Optional<T> $$5 = Bounds.readNumber($$0, $$1, $$2);
                } else {
                    $$6 = $$4;
                }
                if ($$4.isEmpty() && $$6.isEmpty()) {
                    throw ERROR_EMPTY.createWithContext((ImmutableStringReader)$$0);
                }
                return new Bounds<T>($$4, $$6);
            }
            catch (CommandSyntaxException $$7) {
                $$0.setCursor($$3);
                throw new CommandSyntaxException($$7.getType(), $$7.getRawMessage(), $$7.getInput(), $$3);
            }
        }

        private static <T extends Number> Optional<T> readNumber(StringReader $$0, Function<String, T> $$1, Supplier<DynamicCommandExceptionType> $$2) throws CommandSyntaxException {
            int $$3 = $$0.getCursor();
            while ($$0.canRead() && Bounds.isAllowedInputChar($$0)) {
                $$0.skip();
            }
            String $$4 = $$0.getString().substring($$3, $$0.getCursor());
            if ($$4.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of((Number)$$1.apply($$4));
            }
            catch (NumberFormatException $$5) {
                throw $$2.get().createWithContext((ImmutableStringReader)$$0, (Object)$$4);
            }
        }

        private static boolean isAllowedInputChar(StringReader $$0) {
            char $$1 = $$0.peek();
            if ($$1 >= '0' && $$1 <= '9' || $$1 == '-') {
                return true;
            }
            if ($$1 == '.') {
                return !$$0.canRead(2) || $$0.peek(1) != '.';
            }
            return false;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Bounds.class, "min;max", "min", "max"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Bounds.class, "min;max", "min", "max"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Bounds.class, "min;max", "min", "max"}, this, $$0);
        }

        public Optional<T> min() {
            return this.min;
        }

        public Optional<T> max() {
            return this.max;
        }
    }

    public record FloatDegrees(Bounds<Float> bounds) implements MinMaxBounds<Float>
    {
        public static final FloatDegrees ANY = new FloatDegrees(Bounds.any());
        public static final Codec<FloatDegrees> CODEC = Bounds.createCodec(Codec.FLOAT).xmap(FloatDegrees::new, FloatDegrees::bounds);
        public static final StreamCodec<ByteBuf, FloatDegrees> STREAM_CODEC = Bounds.createStreamCodec(ByteBufCodecs.FLOAT).map(FloatDegrees::new, FloatDegrees::bounds);

        public static FloatDegrees fromReader(StringReader $$0) throws CommandSyntaxException {
            Bounds<Float> $$1 = Bounds.fromReader($$0, Float::parseFloat, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidFloat());
            return new FloatDegrees($$1);
        }
    }

    public record Doubles(Bounds<Double> bounds, Bounds<Double> boundsSqr) implements MinMaxBounds<Double>
    {
        public static final Doubles ANY = new Doubles(Bounds.any());
        public static final Codec<Doubles> CODEC = Bounds.createCodec(Codec.DOUBLE).validate(Bounds::validateSwappedBoundsInCodec).xmap(Doubles::new, Doubles::bounds);
        public static final StreamCodec<ByteBuf, Doubles> STREAM_CODEC = Bounds.createStreamCodec(ByteBufCodecs.DOUBLE).map(Doubles::new, Doubles::bounds);

        private Doubles(Bounds<Double> $$0) {
            this($$0, $$0.map(Mth::square));
        }

        public static Doubles exactly(double $$0) {
            return new Doubles(Bounds.exactly($$0));
        }

        public static Doubles between(double $$0, double $$1) {
            return new Doubles(Bounds.between($$0, $$1));
        }

        public static Doubles atLeast(double $$0) {
            return new Doubles(Bounds.atLeast($$0));
        }

        public static Doubles atMost(double $$0) {
            return new Doubles(Bounds.atMost($$0));
        }

        public boolean matches(double $$0) {
            if (this.bounds.min.isPresent() && (Double)this.bounds.min.get() > $$0) {
                return false;
            }
            return this.bounds.max.isEmpty() || !((Double)this.bounds.max.get() < $$0);
        }

        public boolean matchesSqr(double $$0) {
            if (this.boundsSqr.min.isPresent() && (Double)this.boundsSqr.min.get() > $$0) {
                return false;
            }
            return this.boundsSqr.max.isEmpty() || !((Double)this.boundsSqr.max.get() < $$0);
        }

        public static Doubles fromReader(StringReader $$0) throws CommandSyntaxException {
            int $$1 = $$0.getCursor();
            Bounds<Double> $$2 = Bounds.fromReader($$0, Double::parseDouble, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidDouble());
            if ($$2.areSwapped()) {
                $$0.setCursor($$1);
                throw ERROR_SWAPPED.createWithContext((ImmutableStringReader)$$0);
            }
            return new Doubles($$2);
        }
    }

    public record Ints(Bounds<Integer> bounds, Bounds<Long> boundsSqr) implements MinMaxBounds<Integer>
    {
        public static final Ints ANY = new Ints(Bounds.any());
        public static final Codec<Ints> CODEC = Bounds.createCodec(Codec.INT).validate(Bounds::validateSwappedBoundsInCodec).xmap(Ints::new, Ints::bounds);
        public static final StreamCodec<ByteBuf, Ints> STREAM_CODEC = Bounds.createStreamCodec(ByteBufCodecs.INT).map(Ints::new, Ints::bounds);

        private Ints(Bounds<Integer> $$02) {
            this($$02, $$02.map($$0 -> Mth.square($$0.longValue())));
        }

        public static Ints exactly(int $$0) {
            return new Ints(Bounds.exactly($$0));
        }

        public static Ints between(int $$0, int $$1) {
            return new Ints(Bounds.between($$0, $$1));
        }

        public static Ints atLeast(int $$0) {
            return new Ints(Bounds.atLeast($$0));
        }

        public static Ints atMost(int $$0) {
            return new Ints(Bounds.atMost($$0));
        }

        public boolean matches(int $$0) {
            if (this.bounds.min.isPresent() && (Integer)this.bounds.min.get() > $$0) {
                return false;
            }
            return this.bounds.max.isEmpty() || (Integer)this.bounds.max.get() >= $$0;
        }

        public boolean matchesSqr(long $$0) {
            if (this.boundsSqr.min.isPresent() && (Long)this.boundsSqr.min.get() > $$0) {
                return false;
            }
            return this.boundsSqr.max.isEmpty() || (Long)this.boundsSqr.max.get() >= $$0;
        }

        public static Ints fromReader(StringReader $$0) throws CommandSyntaxException {
            int $$1 = $$0.getCursor();
            Bounds<Integer> $$2 = Bounds.fromReader($$0, Integer::parseInt, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidInt());
            if ($$2.areSwapped()) {
                $$0.setCursor($$1);
                throw ERROR_SWAPPED.createWithContext((ImmutableStringReader)$$0);
            }
            return new Ints($$2);
        }
    }
}

