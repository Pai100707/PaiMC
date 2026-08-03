package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class KeyTagProvider<T> extends TagsProvider<T> {
   protected KeyTagProvider(net.minecraft.data.PackOutput $$0, ResourceKey<? extends Registry<T>> $$1, CompletableFuture<Provider> $$2) {
      super($$0, $$1, $$2);
   }

   protected TagAppender<ResourceKey<T>, T> tag(TagKey<T> $$0) {
      TagBuilder $$1 = this.getOrCreateRawBuilder($$0);
      return TagAppender.forBuilder($$1);
   }
}
