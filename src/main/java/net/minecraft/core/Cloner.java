package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public class Cloner<T> {
   private final Codec<T> directCodec;

   Cloner(Codec<T> $$0) {
      this.directCodec = $$0;
   }

   public T clone(T $$0, net.minecraft.core.HolderLookup.Provider $$1, net.minecraft.core.HolderLookup.Provider $$2) {
      DynamicOps<Object> $$3 = $$1.<Object>createSerializationContext(JavaOps.INSTANCE);
      DynamicOps<Object> $$4 = $$2.<Object>createSerializationContext(JavaOps.INSTANCE);
      Object $$5 = this.directCodec.encodeStart($$3, $$0).getOrThrow($$0x -> new IllegalStateException("Failed to encode: " + $$0x));
      return (T)this.directCodec.parse($$4, $$5).getOrThrow($$0x -> new IllegalStateException("Failed to decode: " + $$0x));
   }

   public static class Factory {
      private final Map<ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.core.Cloner<?>> codecs = new HashMap<>();

      public <T> net.minecraft.core.Cloner.Factory addCodec(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$0, Codec<T> $$1) {
         this.codecs.put($$0, new net.minecraft.core.Cloner($$1));
         return this;
      }

      @Nullable
      public <T> net.minecraft.core.Cloner<T> cloner(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$0) {
         return (net.minecraft.core.Cloner<T>)this.codecs.get($$0);
      }
   }
}
