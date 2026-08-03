package net.minecraft.core;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface DefaultedRegistry<T> extends net.minecraft.core.Registry<T> {
   @NonNull
   @Override
   Identifier getKey(T var1);

   @NonNull
   @Override
   T getValue(@Nullable Identifier var1);

   @NonNull
   @Override
   T byId(int var1);

   Identifier getDefaultKey();
}
