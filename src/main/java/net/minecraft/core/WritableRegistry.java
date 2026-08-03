package net.minecraft.core;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface WritableRegistry<T> extends net.minecraft.core.Registry<T> {
   net.minecraft.core.Holder.Reference<T> register(ResourceKey<T> var1, T var2, net.minecraft.core.RegistrationInfo var3);

   void bindTag(TagKey<T> var1, List<net.minecraft.core.Holder<T>> var2);

   boolean isEmpty();

   net.minecraft.core.HolderGetter<T> createRegistrationLookup();
}
