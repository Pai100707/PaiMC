/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.player;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public class StackedItemContents {
    private final StackedContents<Holder<Item>> raw = new StackedContents();

    public void accountSimpleStack(ItemStack $$0) {
        if (Inventory.isUsableForCrafting($$0)) {
            this.accountStack($$0);
        }
    }

    public void accountStack(ItemStack $$0) {
        this.accountStack($$0, $$0.getMaxStackSize());
    }

    public void accountStack(ItemStack $$0, int $$1) {
        if (!$$0.isEmpty()) {
            int $$2 = Math.min($$1, $$0.getCount());
            this.raw.account($$0.getItemHolder(), $$2);
        }
    }

    public boolean canCraft(Recipe<?> $$0, @Nullable StackedContents.Output<Holder<Item>> $$1) {
        return this.canCraft($$0, 1, $$1);
    }

    public boolean canCraft(Recipe<?> $$0, int $$1, @Nullable StackedContents.Output<Holder<Item>> $$2) {
        PlacementInfo $$3 = $$0.placementInfo();
        if ($$3.isImpossibleToPlace()) {
            return false;
        }
        return this.canCraft($$3.ingredients(), $$1, $$2);
    }

    public boolean canCraft(List<? extends StackedContents.IngredientInfo<Holder<Item>>> $$0, @Nullable StackedContents.Output<Holder<Item>> $$1) {
        return this.canCraft($$0, 1, $$1);
    }

    private boolean canCraft(List<? extends StackedContents.IngredientInfo<Holder<Item>>> $$0, int $$1, @Nullable StackedContents.Output<Holder<Item>> $$2) {
        return this.raw.tryPick($$0, $$1, $$2);
    }

    public int getBiggestCraftableStack(Recipe<?> $$0, @Nullable StackedContents.Output<Holder<Item>> $$1) {
        return this.getBiggestCraftableStack($$0, Integer.MAX_VALUE, $$1);
    }

    public int getBiggestCraftableStack(Recipe<?> $$0, int $$1, @Nullable StackedContents.Output<Holder<Item>> $$2) {
        return this.raw.tryPickAll($$0.placementInfo().ingredients(), $$1, $$2);
    }

    public void clear() {
        this.raw.clear();
    }
}

