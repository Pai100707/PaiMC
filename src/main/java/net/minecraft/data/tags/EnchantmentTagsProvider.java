package net.minecraft.data.tags;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

public abstract class EnchantmentTagsProvider extends KeyTagProvider<Enchantment> {
   public EnchantmentTagsProvider(net.minecraft.data.PackOutput $$0, CompletableFuture<Provider> $$1) {
      super($$0, Registries.ENCHANTMENT, $$1);
   }

   protected void tooltipOrder(Provider $$0, ResourceKey<Enchantment>... $$1) {
      this.tag(EnchantmentTags.TOOLTIP_ORDER).add($$1);
      Set<ResourceKey<Enchantment>> $$2 = Set.of($$1);
      List<String> $$3 = $$0.lookupOrThrow(Registries.ENCHANTMENT)
         .listElements()
         .filter($$1x -> !$$2.contains($$1x.unwrapKey().get()))
         .<String>map(Holder::getRegisteredName)
         .collect(Collectors.toList());
      if (!$$3.isEmpty()) {
         throw new IllegalStateException("Not all enchantments were registered for tooltip ordering. Missing: " + String.join(", ", $$3));
      }
   }
}
