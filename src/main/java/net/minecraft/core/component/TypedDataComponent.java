package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Map.Entry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record TypedDataComponent<T>(DataComponentType<T> type, T value) {
   public static final StreamCodec<RegistryFriendlyByteBuf, TypedDataComponent<?>> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, TypedDataComponent<?>>() {
      public TypedDataComponent<?> decode(RegistryFriendlyByteBuf $$0) {
         DataComponentType<?> $$1 = (DataComponentType<?>)DataComponentType.STREAM_CODEC.decode($$0);
         return decodeTyped($$0, (DataComponentType<T>)$$1);
      }

      private static <T> TypedDataComponent<T> decodeTyped(RegistryFriendlyByteBuf $$0, DataComponentType<T> $$1) {
         return new TypedDataComponent<>($$1, (T)$$1.streamCodec().decode($$0));
      }

      public void encode(RegistryFriendlyByteBuf $$0, TypedDataComponent<?> $$1) {
         encodeCap($$0, (TypedDataComponent<T>)$$1);
      }

      private static <T> void encodeCap(RegistryFriendlyByteBuf $$0, TypedDataComponent<T> $$1) {
         DataComponentType.STREAM_CODEC.encode($$0, $$1.type());
         $$1.type().streamCodec().encode($$0, $$1.value());
      }
   };

   static TypedDataComponent<?> fromEntryUnchecked(Entry<DataComponentType<?>, Object> $$0) {
      return createUnchecked($$0.getKey(), $$0.getValue());
   }

   public static <T> TypedDataComponent<T> createUnchecked(DataComponentType<T> $$0, Object $$1) {
      return new TypedDataComponent<>($$0, (T)$$1);
   }

   public void applyTo(PatchedDataComponentMap $$0) {
      $$0.set(this.type, this.value);
   }

   public <D> DataResult<D> encodeValue(DynamicOps<D> $$0) {
      Codec<T> $$1 = this.type.codec();
      return $$1 == null ? DataResult.error(() -> "Component of type " + this.type + " is not encodable") : $$1.encodeStart($$0, this.value);
   }

   @Override
   public String toString() {
      return this.type + "=>" + this.value;
   }
}
