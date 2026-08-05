/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Function;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public abstract class EnchantmentsPredicate
implements SingleComponentItemPredicate<ItemEnchantments> {
    private final List<EnchantmentPredicate> enchantments;

    protected EnchantmentsPredicate(List<EnchantmentPredicate> $$0) {
        this.enchantments = $$0;
    }

    public static <T extends EnchantmentsPredicate> Codec<T> codec(Function<List<EnchantmentPredicate>, T> $$0) {
        return EnchantmentPredicate.CODEC.listOf().xmap($$0, EnchantmentsPredicate::enchantments);
    }

    protected List<EnchantmentPredicate> enchantments() {
        return this.enchantments;
    }

    @Override
    public boolean matches(ItemEnchantments $$0) {
        for (EnchantmentPredicate $$1 : this.enchantments) {
            if ($$1.containedIn($$0)) continue;
            return false;
        }
        return true;
    }

    public static Enchantments enchantments(List<EnchantmentPredicate> $$0) {
        return new Enchantments($$0);
    }

    public static StoredEnchantments storedEnchantments(List<EnchantmentPredicate> $$0) {
        return new StoredEnchantments($$0);
    }

    public static class Enchantments
    extends EnchantmentsPredicate {
        public static final Codec<Enchantments> CODEC = Enchantments.codec(Enchantments::new);

        protected Enchantments(List<EnchantmentPredicate> $$0) {
            super($$0);
        }

        @Override
        public DataComponentType<ItemEnchantments> componentType() {
            return DataComponents.ENCHANTMENTS;
        }
    }

    public static class StoredEnchantments
    extends EnchantmentsPredicate {
        public static final Codec<StoredEnchantments> CODEC = StoredEnchantments.codec(StoredEnchantments::new);

        protected StoredEnchantments(List<EnchantmentPredicate> $$0) {
            super($$0);
        }

        @Override
        public DataComponentType<ItemEnchantments> componentType() {
            return DataComponents.STORED_ENCHANTMENTS;
        }
    }
}

