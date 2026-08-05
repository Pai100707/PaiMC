/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Optional;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentPredicate(Optional<HolderSet<Enchantment>> enchantments, MinMaxBounds.Ints level) {
    public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(EnchantmentPredicate::enchantments), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("levels", (Object)MinMaxBounds.Ints.ANY).forGetter(EnchantmentPredicate::level)).apply((Applicative)$$0, EnchantmentPredicate::new));

    public EnchantmentPredicate(Holder<Enchantment> $$0, MinMaxBounds.Ints $$1) {
        this(Optional.of(HolderSet.direct($$0)), $$1);
    }

    public EnchantmentPredicate(HolderSet<Enchantment> $$0, MinMaxBounds.Ints $$1) {
        this(Optional.of($$0), $$1);
    }

    public boolean containedIn(ItemEnchantments $$0) {
        if (this.enchantments.isPresent()) {
            for (Holder holder : this.enchantments.get()) {
                if (!this.matchesEnchantment($$0, holder)) continue;
                return true;
            }
            return false;
        }
        if (this.level != MinMaxBounds.Ints.ANY) {
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : $$0.entrySet()) {
                if (!this.level.matches(entry.getIntValue())) continue;
                return true;
            }
            return false;
        }
        return !$$0.isEmpty();
    }

    private boolean matchesEnchantment(ItemEnchantments $$0, Holder<Enchantment> $$1) {
        int $$2 = $$0.getLevel($$1);
        if ($$2 == 0) {
            return false;
        }
        if (this.level == MinMaxBounds.Ints.ANY) {
            return true;
        }
        return this.level.matches($$2);
    }
}

