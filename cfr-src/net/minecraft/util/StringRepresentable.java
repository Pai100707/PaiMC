/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Keyable
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public interface StringRepresentable {
    public static final int PRE_BUILT_MAP_THRESHOLD = 16;

    public String getSerializedName();

    public static <E extends Enum<E>> EnumCodec<E> fromEnum(Supplier<E[]> $$02) {
        return StringRepresentable.fromEnumWithMapping($$02, $$0 -> $$0);
    }

    public static <E extends Enum<E>> EnumCodec<E> fromEnumWithMapping(Supplier<E[]> $$0, Function<String, String> $$12) {
        Enum[] $$2 = (Enum[])$$0.get();
        Function<String, Enum> $$3 = StringRepresentable.createNameLookup($$2, $$1 -> (String)$$12.apply(((StringRepresentable)((Object)$$1)).getSerializedName()));
        return new EnumCodec($$2, $$3);
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public static <T extends StringRepresentable> Codec<T> fromValues(Supplier<T[]> $$0) {
        StringRepresentable[] $$1 = (StringRepresentable[])$$0.get();
        @Nullable Function $$2 = StringRepresentable.createNameLookup((StringRepresentable[])$$1);
        ToIntFunction<StringRepresentable> $$3 = Util.createIndexLookup(Arrays.asList($$1));
        return new StringRepresentableCodec($$1, $$2, $$3);
    }

    public static <T extends StringRepresentable> Function<String, @Nullable T> createNameLookup(T[] $$0) {
        return StringRepresentable.createNameLookup($$0, StringRepresentable::getSerializedName);
    }

    public static <T> Function<String, @Nullable T> createNameLookup(T[] $$02, Function<T, String> $$1) {
        if ($$02.length > 16) {
            Map<String, Object> $$22 = Arrays.stream($$02).collect(Collectors.toMap($$1, $$0 -> $$0));
            return $$22::get;
        }
        return $$2 -> {
            for (Object $$3 : $$02) {
                if (!((String)$$1.apply($$3)).equals($$2)) continue;
                return $$3;
            }
            return null;
        };
    }

    public static Keyable keys(final StringRepresentable[] $$0) {
        return new Keyable(){

            public <T> Stream<T> keys(DynamicOps<T> $$02) {
                return Arrays.stream($$0).map(StringRepresentable::getSerializedName).map(arg_0 -> $$02.createString(arg_0));
            }
        };
    }

    public static class EnumCodec<E extends Enum<E>>
    extends StringRepresentableCodec<E> {
        private final Function<String, @Nullable E> resolver;

        public EnumCodec(E[] $$02, Function<String, E> $$1) {
            super($$02, $$1, $$0 -> ((Enum)$$0).ordinal());
            this.resolver = $$1;
        }

        public @Nullable E byName(String $$0) {
            return (E)((Enum)this.resolver.apply($$0));
        }

        public E byName(String $$0, E $$1) {
            return (E)((Enum)Objects.requireNonNullElse(this.byName($$0), $$1));
        }

        public E byName(String $$0, Supplier<? extends E> $$1) {
            return (E)((Enum)Objects.requireNonNullElseGet(this.byName($$0), $$1));
        }
    }

    public static class StringRepresentableCodec<S extends StringRepresentable>
    implements Codec<S> {
        private final Codec<S> codec;

        public StringRepresentableCodec(S[] $$0, Function<String, @Nullable S> $$12, ToIntFunction<S> $$2) {
            this.codec = ExtraCodecs.orCompressed(Codec.stringResolver(StringRepresentable::getSerializedName, $$12), ExtraCodecs.idResolverCodec($$2, $$1 -> $$1 >= 0 && $$1 < $$0.length ? $$0[$$1] : null, -1));
        }

        public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> $$0, T $$1) {
            return this.codec.decode($$0, $$1);
        }

        public <T> DataResult<T> encode(S $$0, DynamicOps<T> $$1, T $$2) {
            return this.codec.encode($$0, $$1, $$2);
        }

        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((S)((StringRepresentable)object), (DynamicOps<T>)dynamicOps, (T)object2);
        }
    }
}

