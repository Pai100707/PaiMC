package net.minecraft.data.registries;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder.PatchedRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.providers.TradeRebalanceEnchantmentProviders;

public class TradeRebalanceRegistries {
   private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
      .add(Registries.ENCHANTMENT_PROVIDER, TradeRebalanceEnchantmentProviders::bootstrap);

   public static CompletableFuture<PatchedRegistries> createLookup(CompletableFuture<Provider> $$0) {
      return RegistryPatchGenerator.createLookup($$0, BUILDER);
   }
}
