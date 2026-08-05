/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.registries;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.world.item.enchantment.providers.TradeRebalanceEnchantmentProviders;

public class TradeRebalanceRegistries {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder().add(Registries.ENCHANTMENT_PROVIDER, TradeRebalanceEnchantmentProviders::bootstrap);

    public static CompletableFuture<RegistrySetBuilder.PatchedRegistries> createLookup(CompletableFuture<HolderLookup.Provider> $$0) {
        return RegistryPatchGenerator.createLookup($$0, BUILDER);
    }
}

