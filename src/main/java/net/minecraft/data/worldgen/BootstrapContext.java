package net.minecraft.data.worldgen;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.resources.ResourceKey;

public interface BootstrapContext<T> {
   Reference<T> register(ResourceKey<T> var1, T var2, Lifecycle var3);

   default Reference<T> register(ResourceKey<T> $$0, T $$1) {
      return this.register($$0, $$1, Lifecycle.stable());
   }

   <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> var1);
}
