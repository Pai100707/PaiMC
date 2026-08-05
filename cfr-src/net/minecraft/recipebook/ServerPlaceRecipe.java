/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class ServerPlaceRecipe<R extends Recipe<?>> {
    private static final int ITEM_NOT_FOUND = -1;
    private final Inventory inventory;
    private final CraftingMenuAccess<R> menu;
    private final boolean useMaxItems;
    private final int gridWidth;
    private final int gridHeight;
    private final List<Slot> inputGridSlots;
    private final List<Slot> slotsToClear;

    public static <I extends RecipeInput, R extends Recipe<I>> RecipeBookMenu.PostPlaceAction placeRecipe(CraftingMenuAccess<R> $$0, int $$1, int $$2, List<Slot> $$3, List<Slot> $$4, Inventory $$5, RecipeHolder<R> $$6, boolean $$7, boolean $$8) {
        ServerPlaceRecipe<R> $$9 = new ServerPlaceRecipe<R>($$0, $$5, $$7, $$1, $$2, $$3, $$4);
        if (!$$8 && !$$9.testClearGrid()) {
            return RecipeBookMenu.PostPlaceAction.NOTHING;
        }
        StackedItemContents $$10 = new StackedItemContents();
        $$5.fillStackedContents($$10);
        $$0.fillCraftSlotsStackedContents($$10);
        return $$9.tryPlaceRecipe($$6, $$10);
    }

    private ServerPlaceRecipe(CraftingMenuAccess<R> $$0, Inventory $$1, boolean $$2, int $$3, int $$4, List<Slot> $$5, List<Slot> $$6) {
        this.menu = $$0;
        this.inventory = $$1;
        this.useMaxItems = $$2;
        this.gridWidth = $$3;
        this.gridHeight = $$4;
        this.inputGridSlots = $$5;
        this.slotsToClear = $$6;
    }

    private RecipeBookMenu.PostPlaceAction tryPlaceRecipe(RecipeHolder<R> $$0, StackedItemContents $$1) {
        if ($$1.canCraft((Recipe<?>)$$0.value(), null)) {
            this.placeRecipe($$0, $$1);
            this.inventory.setChanged();
            return RecipeBookMenu.PostPlaceAction.NOTHING;
        }
        this.clearGrid();
        this.inventory.setChanged();
        return RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE;
    }

    private void clearGrid() {
        for (Slot $$0 : this.slotsToClear) {
            ItemStack $$1 = $$0.getItem().copy();
            this.inventory.placeItemBackInInventory($$1, false);
            $$0.set($$1);
        }
        this.menu.clearCraftingContent();
    }

    private void placeRecipe(RecipeHolder<R> $$0, StackedItemContents $$1) {
        boolean $$22 = this.menu.recipeMatches($$0);
        int $$32 = $$1.getBiggestCraftableStack((Recipe<?>)$$0.value(), null);
        if ($$22) {
            for (Slot $$42 : this.inputGridSlots) {
                ItemStack $$52 = $$42.getItem();
                if ($$52.isEmpty() || Math.min($$32, $$52.getMaxStackSize()) >= $$52.getCount() + 1) continue;
                return;
            }
        }
        int $$6 = this.calculateAmountToCraft($$32, $$22);
        ArrayList<Holder<Item>> $$7 = new ArrayList<Holder<Item>>();
        if (!$$1.canCraft((Recipe<?>)$$0.value(), $$6, $$7::add)) {
            return;
        }
        int $$8 = ServerPlaceRecipe.clampToMaxStackSize($$6, $$7);
        if ($$8 != $$6) {
            $$7.clear();
            if (!$$1.canCraft((Recipe<?>)$$0.value(), $$8, $$7::add)) {
                return;
            }
        }
        this.clearGrid();
        PlaceRecipeHelper.placeRecipe(this.gridWidth, this.gridHeight, $$0.value(), $$0.value().placementInfo().slotsToIngredientIndex(), ($$2, $$3, $$4, $$5) -> {
            if ($$2 == -1) {
                return;
            }
            Slot $$6 = this.inputGridSlots.get($$3);
            Holder $$7 = (Holder)$$7.get((int)$$2);
            int $$8 = $$8;
            while ($$8 > 0) {
                if (($$8 = this.moveItemToGrid($$6, $$7, $$8)) != -1) continue;
                return;
            }
        });
    }

    private static int clampToMaxStackSize(int $$0, List<Holder<Item>> $$1) {
        for (Holder<Item> $$2 : $$1) {
            $$0 = Math.min($$0, $$2.value().getDefaultMaxStackSize());
        }
        return $$0;
    }

    private int calculateAmountToCraft(int $$0, boolean $$1) {
        if (this.useMaxItems) {
            return $$0;
        }
        if ($$1) {
            int $$2 = Integer.MAX_VALUE;
            for (Slot $$3 : this.inputGridSlots) {
                ItemStack $$4 = $$3.getItem();
                if ($$4.isEmpty() || $$2 <= $$4.getCount()) continue;
                $$2 = $$4.getCount();
            }
            if ($$2 != Integer.MAX_VALUE) {
                ++$$2;
            }
            return $$2;
        }
        return 1;
    }

    private int moveItemToGrid(Slot $$0, Holder<Item> $$1, int $$2) {
        ItemStack $$7;
        ItemStack $$3 = $$0.getItem();
        int $$4 = this.inventory.findSlotMatchingCraftingIngredient($$1, $$3);
        if ($$4 == -1) {
            return -1;
        }
        ItemStack $$5 = this.inventory.getItem($$4);
        if ($$2 < $$5.getCount()) {
            ItemStack $$6 = this.inventory.removeItem($$4, $$2);
        } else {
            $$7 = this.inventory.removeItemNoUpdate($$4);
        }
        int $$8 = $$7.getCount();
        if ($$3.isEmpty()) {
            $$0.set($$7);
        } else {
            $$3.grow($$8);
        }
        return $$2 - $$8;
    }

    private boolean testClearGrid() {
        ArrayList $$0 = Lists.newArrayList();
        int $$1 = this.getAmountOfFreeSlotsInInventory();
        for (Slot $$2 : this.inputGridSlots) {
            ItemStack $$3 = $$2.getItem().copy();
            if ($$3.isEmpty()) continue;
            int $$4 = this.inventory.getSlotWithRemainingSpace($$3);
            if ($$4 == -1 && $$0.size() <= $$1) {
                for (ItemStack $$5 : $$0) {
                    if (!ItemStack.isSameItem($$5, $$3) || $$5.getCount() == $$5.getMaxStackSize() || $$5.getCount() + $$3.getCount() > $$5.getMaxStackSize()) continue;
                    $$5.grow($$3.getCount());
                    $$3.setCount(0);
                    break;
                }
                if ($$3.isEmpty()) continue;
                if ($$0.size() < $$1) {
                    $$0.add($$3);
                    continue;
                }
                return false;
            }
            if ($$4 != -1) continue;
            return false;
        }
        return true;
    }

    private int getAmountOfFreeSlotsInInventory() {
        int $$0 = 0;
        for (ItemStack $$1 : this.inventory.getNonEquipmentItems()) {
            if (!$$1.isEmpty()) continue;
            ++$$0;
        }
        return $$0;
    }

    public static interface CraftingMenuAccess<T extends Recipe<?>> {
        public void fillCraftSlotsStackedContents(StackedItemContents var1);

        public void clearCraftingContent();

        public boolean recipeMatches(RecipeHolder<T> var1);
    }
}

