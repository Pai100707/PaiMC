/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tags;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

public abstract class EnchantmentTagsProvider
extends KeyTagProvider<Enchantment> {
    public EnchantmentTagsProvider(PackOutput $$0, CompletableFuture<HolderLookup.Provider> $$1) {
        super($$0, Registries.ENCHANTMENT, $$1);
    }

    protected void tooltipOrder(HolderLookup.Provider $$0, ResourceKey<Enchantment> ... $$12) {
        this.tag(EnchantmentTags.TOOLTIP_ORDER).add($$12);
        Set<ResourceKey<Enchantment>> $$2 = Set.of($$12);
        List $$3 = $$0.lookupOrThrow(Registries.ENCHANTMENT).listElements().filter($$1 -> !$$2.contains($$1.unwrapKey().get())).map(Holder::getRegisteredName).collect(Collectors.toList());
        if (!$$3.isEmpty()) {
            throw new IllegalStateException("Not all enchantments were registered for tooltip ordering. Missing: " + String.join((CharSequence)", ", $$3));
        }
    }
}

