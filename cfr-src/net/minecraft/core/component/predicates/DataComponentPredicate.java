/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.core.component.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.AnyValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface DataComponentPredicate {
    public static final Codec<Map<Type<?>, DataComponentPredicate>> CODEC = Codec.dispatchedMap(Type.CODEC, Type::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf, Single<?>> SINGLE_STREAM_CODEC = Type.STREAM_CODEC.dispatch(Single::type, Type::singleStreamCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, Map<Type<?>, DataComponentPredicate>> STREAM_CODEC = SINGLE_STREAM_CODEC.apply(ByteBufCodecs.list(64)).map($$0 -> $$0.stream().collect(Collectors.toMap(Single::type, Single::predicate)), $$0 -> $$0.entrySet().stream().map(Single::fromEntry).toList());

    public static MapCodec<Single<?>> singleCodec(String $$0) {
        return Type.CODEC.dispatchMap($$0, Single::type, Type::wrappedCodec);
    }

    public boolean matches(DataComponentGetter var1);

    public static interface Type<T extends DataComponentPredicate> {
        public static final Codec<Type<?>> CODEC = Codec.either(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE.byNameCodec(), BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec()).xmap(Type::copyOrCreateType, Type::unpackType);
        public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.registry(Registries.DATA_COMPONENT_PREDICATE_TYPE), ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE)).map(Type::copyOrCreateType, Type::unpackType);

        private static <T extends Type<?>> Either<T, DataComponentType<?>> unpackType(T $$0) {
            Either either;
            if ($$0 instanceof AnyValueType) {
                AnyValueType $$1 = (AnyValueType)$$0;
                either = Either.right($$1.componentType());
            } else {
                either = Either.left($$0);
            }
            return either;
        }

        private static Type<?> copyOrCreateType(Either<Type<?>, DataComponentType<?>> $$02) {
            return (Type)$$02.map($$0 -> $$0, AnyValueType::create);
        }

        public Codec<T> codec();

        public MapCodec<Single<T>> wrappedCodec();

        public StreamCodec<RegistryFriendlyByteBuf, Single<T>> singleStreamCodec();
    }

    public record Single<T extends DataComponentPredicate>(Type<T> type, T predicate) {
        static <T extends DataComponentPredicate> MapCodec<Single<T>> wrapCodec(Type<T> $$0, Codec<T> $$1) {
            return RecordCodecBuilder.mapCodec($$2 -> $$2.group((App)$$1.fieldOf("value").forGetter(Single::predicate)).apply((Applicative)$$2, $$1 -> new Single<DataComponentPredicate>($$0, (DataComponentPredicate)$$1)));
        }

        private static <T extends DataComponentPredicate> Single<T> fromEntry(Map.Entry<Type<?>, T> $$0) {
            return new Single<DataComponentPredicate>($$0.getKey(), (DataComponentPredicate)$$0.getValue());
        }
    }

    public static final class AnyValueType
    extends TypeBase<AnyValue> {
        private final AnyValue predicate;

        public AnyValueType(AnyValue $$0) {
            super(MapCodec.unitCodec((Object)$$0));
            this.predicate = $$0;
        }

        public AnyValue predicate() {
            return this.predicate;
        }

        public DataComponentType<?> componentType() {
            return this.predicate.type();
        }

        public static AnyValueType create(DataComponentType<?> $$0) {
            return new AnyValueType(new AnyValue($$0));
        }
    }

    public static final class ConcreteType<T extends DataComponentPredicate>
    extends TypeBase<T> {
        public ConcreteType(Codec<T> $$0) {
            super($$0);
        }
    }

    public static abstract class TypeBase<T extends DataComponentPredicate>
    implements Type<T> {
        private final Codec<T> codec;
        private final MapCodec<Single<T>> wrappedCodec;
        private final StreamCodec<RegistryFriendlyByteBuf, Single<T>> singleStreamCodec;

        public TypeBase(Codec<T> $$02) {
            this.codec = $$02;
            this.wrappedCodec = Single.wrapCodec(this, $$02);
            this.singleStreamCodec = ByteBufCodecs.fromCodecWithRegistries($$02).map($$0 -> new Single<DataComponentPredicate>(this, (DataComponentPredicate)$$0), Single::predicate);
        }

        @Override
        public Codec<T> codec() {
            return this.codec;
        }

        @Override
        public MapCodec<Single<T>> wrappedCodec() {
            return this.wrappedCodec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Single<T>> singleStreamCodec() {
            return this.singleStreamCodec;
        }
    }
}

