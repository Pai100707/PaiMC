package net.minecraft.core.component.predicates;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface DataComponentPredicate {
   Codec<Map<DataComponentPredicate.Type<?>, DataComponentPredicate>> CODEC = Codec.dispatchedMap(
      DataComponentPredicate.Type.CODEC, DataComponentPredicate.Type::codec
   );
   StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate.Single<?>> SINGLE_STREAM_CODEC = DataComponentPredicate.Type.STREAM_CODEC
      .dispatch(DataComponentPredicate.Single::type, DataComponentPredicate.Type::singleStreamCodec);
   StreamCodec<RegistryFriendlyByteBuf, Map<DataComponentPredicate.Type<?>, DataComponentPredicate>> STREAM_CODEC = SINGLE_STREAM_CODEC.apply(
         ByteBufCodecs.list(64)
      )
      .map(
         $$0 -> $$0.stream().collect(Collectors.toMap(DataComponentPredicate.Single::type, DataComponentPredicate.Single::predicate)),
         $$0 -> $$0.entrySet().stream().map(DataComponentPredicate.Single::fromEntry).toList()
      );

   static MapCodec<DataComponentPredicate.Single<?>> singleCodec(String $$0) {
      return DataComponentPredicate.Type.CODEC.dispatchMap($$0, DataComponentPredicate.Single::type, DataComponentPredicate.Type::wrappedCodec);
   }

   boolean matches(DataComponentGetter var1);

   public static final class AnyValueType extends DataComponentPredicate.TypeBase<AnyValue> {
      private final AnyValue predicate;

      public AnyValueType(AnyValue $$0) {
         super(MapCodec.unitCodec($$0));
         this.predicate = $$0;
      }

      public AnyValue predicate() {
         return this.predicate;
      }

      public DataComponentType<?> componentType() {
         return this.predicate.type();
      }

      public static DataComponentPredicate.AnyValueType create(DataComponentType<?> $$0) {
         return new DataComponentPredicate.AnyValueType(new AnyValue($$0));
      }
   }

   public static final class ConcreteType<T extends DataComponentPredicate> extends DataComponentPredicate.TypeBase<T> {
      public ConcreteType(Codec<T> $$0) {
         super($$0);
      }
   }

   public record Single<T extends DataComponentPredicate>(DataComponentPredicate.Type<T> type, T predicate) {
      static <T extends DataComponentPredicate> MapCodec<DataComponentPredicate.Single<T>> wrapCodec(DataComponentPredicate.Type<T> $$0, Codec<T> $$1) {
         return RecordCodecBuilder.mapCodec(
            $$2 -> $$2.group($$1.fieldOf("value").forGetter(DataComponentPredicate.Single::predicate))
               .apply($$2, $$1xx -> new DataComponentPredicate.Single<>($$0, (T)$$1xx))
         );
      }

      private static <T extends DataComponentPredicate> DataComponentPredicate.Single<T> fromEntry(Entry<DataComponentPredicate.Type<?>, T> $$0) {
         return new DataComponentPredicate.Single<>((DataComponentPredicate.Type<T>)$$0.getKey(), $$0.getValue());
      }
   }

   public interface Type<T extends DataComponentPredicate> {
      Codec<DataComponentPredicate.Type<?>> CODEC = Codec.either(
            BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE.byNameCodec(), BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec()
         )
         .xmap(DataComponentPredicate.Type::copyOrCreateType, DataComponentPredicate.Type::unpackType);
      StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate.Type<?>> STREAM_CODEC = ByteBufCodecs.either(
            ByteBufCodecs.registry(Registries.DATA_COMPONENT_PREDICATE_TYPE), ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE)
         )
         .map(DataComponentPredicate.Type::copyOrCreateType, DataComponentPredicate.Type::unpackType);

      private static <T extends DataComponentPredicate.Type<?>> Either<T, DataComponentType<?>> unpackType(T $$0) {
         return $$0 instanceof DataComponentPredicate.AnyValueType $$1 ? Either.right($$1.componentType()) : Either.left($$0);
      }

      private static DataComponentPredicate.Type<?> copyOrCreateType(Either<DataComponentPredicate.Type<?>, DataComponentType<?>> $$0) {
         return (DataComponentPredicate.Type<?>)$$0.map($$0x -> $$0x, DataComponentPredicate.AnyValueType::create);
      }

      Codec<T> codec();

      MapCodec<DataComponentPredicate.Single<T>> wrappedCodec();

      StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate.Single<T>> singleStreamCodec();
   }

   public abstract static class TypeBase<T extends DataComponentPredicate> implements DataComponentPredicate.Type<T> {
      private final Codec<T> codec;
      private final MapCodec<DataComponentPredicate.Single<T>> wrappedCodec;
      private final StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate.Single<T>> singleStreamCodec;

      public TypeBase(Codec<T> $$0) {
         this.codec = $$0;
         this.wrappedCodec = DataComponentPredicate.Single.wrapCodec(this, $$0);
         this.singleStreamCodec = ByteBufCodecs.fromCodecWithRegistries($$0)
            .map($$0x -> new DataComponentPredicate.Single<>(this, (T)$$0x), DataComponentPredicate.Single::predicate);
      }

      @Override
      public Codec<T> codec() {
         return this.codec;
      }

      @Override
      public MapCodec<DataComponentPredicate.Single<T>> wrappedCodec() {
         return this.wrappedCodec;
      }

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate.Single<T>> singleStreamCodec() {
         return this.singleStreamCodec;
      }
   }
}
