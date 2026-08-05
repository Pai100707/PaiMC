/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

public record PotionsPredicate(HolderSet<Potion> potions) implements SingleComponentItemPredicate<PotionContents>
{
    public static final Codec<PotionsPredicate> CODEC = RegistryCodecs.homogeneousList(Registries.POTION).xmap(PotionsPredicate::new, PotionsPredicate::potions);

    @Override
    public DataComponentType<PotionContents> componentType() {
        return DataComponents.POTION_CONTENTS;
    }

    @Override
    public boolean matches(PotionContents $$0) {
        Optional<Holder<Potion>> $$1 = $$0.potion();
        return !$$1.isEmpty() && this.potions.contains($$1.get());
    }

    public static DataComponentPredicate potions(HolderSet<Potion> $$0) {
        return new PotionsPredicate($$0);
    }
}

