package net.minecraft.util.debug;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class DebugSubscription<T> {
   public static final int DOES_NOT_EXPIRE = 0;
   
   final StreamCodec<? super RegistryFriendlyByteBuf, T> valueStreamCodec;
   private final int expireAfterTicks;

   public DebugSubscription(StreamCodec<? super RegistryFriendlyByteBuf, T> $$0, int $$1) {
      this.valueStreamCodec = $$0;
      this.expireAfterTicks = $$1;
   }

   public DebugSubscription(StreamCodec<? super RegistryFriendlyByteBuf, T> $$0) {
      this($$0, 0);
   }

   public DebugSubscription.Update<T> packUpdate(T $$0) {
      return new DebugSubscription.Update<>(this, Optional.ofNullable($$0));
   }

   public DebugSubscription.Update<T> emptyUpdate() {
      return new DebugSubscription.Update<>(this, Optional.empty());
   }

   public DebugSubscription.Event<T> packEvent(T $$0) {
      return new DebugSubscription.Event<>(this, $$0);
   }

   @Override
   public String toString() {
      return net.minecraft.util.Util.getRegisteredName(BuiltInRegistries.DEBUG_SUBSCRIPTION, this);
   }

   
   public StreamCodec<? super RegistryFriendlyByteBuf, T> valueStreamCodec() {
      return this.valueStreamCodec;
   }

   public int expireAfterTicks() {
      return this.expireAfterTicks;
   }

   public record Event<T>(DebugSubscription<T> subscription, T value) {
      public static final StreamCodec<RegistryFriendlyByteBuf, DebugSubscription.Event<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.DEBUG_SUBSCRIPTION)
         .dispatch(DebugSubscription.Event::subscription, DebugSubscription.Event::streamCodec);

      private static <T> StreamCodec<? super RegistryFriendlyByteBuf, DebugSubscription.Event<T>> streamCodec(DebugSubscription<T> $$0) {
         return Objects.requireNonNull($$0.valueStreamCodec).map($$1 -> new DebugSubscription.Event<>($$0, (T)$$1), DebugSubscription.Event::value);
      }
   }

   public record Update<T>(DebugSubscription<T> subscription, Optional<T> value) {
      public static final StreamCodec<RegistryFriendlyByteBuf, DebugSubscription.Update<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.DEBUG_SUBSCRIPTION)
         .dispatch(DebugSubscription.Update::subscription, DebugSubscription.Update::streamCodec);

      private static <T> StreamCodec<? super RegistryFriendlyByteBuf, DebugSubscription.Update<T>> streamCodec(DebugSubscription<T> $$0) {
         return ByteBufCodecs.optional(Objects.requireNonNull($$0.valueStreamCodec))
            .map($$1 -> new DebugSubscription.Update<>($$0, $$1), DebugSubscription.Update::value);
      }
   }
}
