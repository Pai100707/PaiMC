/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Hash$Strategy
 *  it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemStackLinkedSet {
    private static final Hash.Strategy<? super ItemStack> TYPE_AND_TAG = new Hash.Strategy<ItemStack>(){

        public int hashCode(@Nullable ItemStack $$0) {
            return ItemStack.hashItemAndComponents($$0);
        }

        public boolean equals(@Nullable ItemStack $$0, @Nullable ItemStack $$1) {
            return $$0 == $$1 || $$0 != null && $$1 != null && $$0.isEmpty() == $$1.isEmpty() && ItemStack.isSameItemSameComponents($$0, $$1);
        }

        public /* synthetic */ boolean equals(@Nullable Object object, @Nullable Object object2) {
            return this.equals((ItemStack)object, (ItemStack)object2);
        }

        public /* synthetic */ int hashCode(@Nullable Object object) {
            return this.hashCode((ItemStack)object);
        }
    };

    public static Set<ItemStack> createTypeAndComponentsSet() {
        return new ObjectLinkedOpenCustomHashSet(TYPE_AND_TAG);
    }
}

