package net.minecraft.network.syncher;

import net.minecraft.network.codec.StreamCodec;

public interface EntityDataSerializer<T> {
   StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> codec();

   default EntityDataAccessor<T> createAccessor(int $$0) {
      return new EntityDataAccessor<>($$0, this);
   }

   T copy(T var1);

   static <T> EntityDataSerializer<T> forValueType(StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> $$0) {
      return () -> $$0;
   }

   public interface ForValueType<T> extends EntityDataSerializer<T> {
      @Override
      default T copy(T $$0) {
         return $$0;
      }
   }
}
