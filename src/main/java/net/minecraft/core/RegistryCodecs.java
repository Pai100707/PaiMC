package net.minecraft.core;

import com.mojang.serialization.Codec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {
   public static <E> Codec<net.minecraft.core.HolderSet<E>> homogeneousList(ResourceKey<? extends net.minecraft.core.Registry<E>> $$0, Codec<E> $$1) {
      return homogeneousList($$0, $$1, false);
   }

   public static <E> Codec<net.minecraft.core.HolderSet<E>> homogeneousList(
      ResourceKey<? extends net.minecraft.core.Registry<E>> $$0, Codec<E> $$1, boolean $$2
   ) {
      return HolderSetCodec.create($$0, RegistryFileCodec.create($$0, $$1), $$2);
   }

   public static <E> Codec<net.minecraft.core.HolderSet<E>> homogeneousList(ResourceKey<? extends net.minecraft.core.Registry<E>> $$0) {
      return homogeneousList($$0, false);
   }

   public static <E> Codec<net.minecraft.core.HolderSet<E>> homogeneousList(ResourceKey<? extends net.minecraft.core.Registry<E>> $$0, boolean $$1) {
      return HolderSetCodec.create($$0, RegistryFixedCodec.create($$0), $$1);
   }
}
