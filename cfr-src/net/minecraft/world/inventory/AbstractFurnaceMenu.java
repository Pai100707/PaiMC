/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public abstract class AbstractFurnaceMenu
extends RecipeBookMenu {
    public static final int INGREDIENT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int DATA_COUNT = 4;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    final Container container;
    private final ContainerData data;
    protected final Level level;
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;
    private final RecipePropertySet acceptedInputs;
    private final RecipeBookType recipeBookType;

    protected AbstractFurnaceMenu(MenuType<?> $$0, RecipeType<? extends AbstractCookingRecipe> $$1, ResourceKey<RecipePropertySet> $$2, RecipeBookType $$3, int $$4, Inventory $$5) {
        this($$0, $$1, $$2, $$3, $$4, $$5, new SimpleContainer(3), new SimpleContainerData(4));
    }

    protected AbstractFurnaceMenu(MenuType<?> $$0, RecipeType<? extends AbstractCookingRecipe> $$1, ResourceKey<RecipePropertySet> $$2, RecipeBookType $$3, int $$4, Inventory $$5, Container $$6, ContainerData $$7) {
        super($$0, $$4);
        this.recipeType = $$1;
        this.recipeBookType = $$3;
        AbstractFurnaceMenu.checkContainerSize($$6, 3);
        AbstractFurnaceMenu.checkContainerDataCount($$7, 4);
        this.container = $$6;
        this.data = $$7;
        this.level = $$5.player.level();
        this.acceptedInputs = this.level.recipeAccess().propertySet($$2);
        this.addSlot(new Slot($$6, 0, 56, 17));
        this.addSlot(new FurnaceFuelSlot(this, $$6, 1, 56, 53));
        this.addSlot(new FurnaceResultSlot($$5.player, $$6, 2, 116, 35));
        this.addStandardInventorySlots($$5, 8, 84);
        this.addDataSlots($$7);
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents $$0) {
        if (this.container instanceof StackedContentsCompatible) {
            ((StackedContentsCompatible)((Object)this.container)).fillStackedContents($$0);
        }
    }

    public Slot getResultSlot() {
        return (Slot)this.slots.get(2);
    }

    @Override
    public boolean stillValid(Player $$0) {
        return this.container.stillValid($$0);
    }

    @Override
    public ItemStack quickMoveStack(Player $$0, int $$1) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = (Slot)this.slots.get($$1);
        if ($$3 != null && $$3.hasItem()) {
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            if ($$1 == 2) {
                if (!this.moveItemStackTo($$4, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                $$3.onQuickCraft($$4, $$2);
            } else if ($$1 == 1 || $$1 == 0 ? !this.moveItemStackTo($$4, 3, 39, false) : (this.canSmelt($$4) ? !this.moveItemStackTo($$4, 0, 1, false) : (this.isFuel($$4) ? !this.moveItemStackTo($$4, 1, 2, false) : ($$1 >= 3 && $$1 < 30 ? !this.moveItemStackTo($$4, 30, 39, false) : $$1 >= 30 && $$1 < 39 && !this.moveItemStackTo($$4, 3, 30, false))))) {
                return ItemStack.EMPTY;
            }
            if ($$4.isEmpty()) {
                $$3.setByPlayer(ItemStack.EMPTY);
            } else {
                $$3.setChanged();
            }
            if ($$4.getCount() == $$2.getCount()) {
                return ItemStack.EMPTY;
            }
            $$3.onTake($$0, $$4);
        }
        return $$2;
    }

    protected boolean canSmelt(ItemStack $$0) {
        return this.acceptedInputs.test($$0);
    }

    protected boolean isFuel(ItemStack $$0) {
        return this.level.fuelValues().isFuel($$0);
    }

    public float getBurnProgress() {
        int $$0 = this.data.get(2);
        int $$1 = this.data.get(3);
        if ($$1 == 0 || $$0 == 0) {
            return 0.0f;
        }
        return Mth.clamp((float)$$0 / (float)$$1, 0.0f, 1.0f);
    }

    public float getLitProgress() {
        int $$0 = this.data.get(1);
        if ($$0 == 0) {
            $$0 = 200;
        }
        return Mth.clamp((float)this.data.get(0) / (float)$$0, 0.0f, 1.0f);
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return this.recipeBookType;
    }

    @Override
    public RecipeBookMenu.PostPlaceAction handlePlacement(boolean $$0, boolean $$1, RecipeHolder<?> $$2, final ServerLevel $$3, Inventory $$4) {
        final List<Slot> $$5 = List.of(this.getSlot(0), this.getSlot(2));
        RecipeHolder<?> $$6 = $$2;
        return ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<AbstractCookingRecipe>(){

            @Override
            public void fillCraftSlotsStackedContents(StackedItemContents $$0) {
                AbstractFurnaceMenu.this.fillCraftSlotsStackedContents($$0);
            }

            @Override
            public void clearCraftingContent() {
                $$5.forEach($$0 -> $$0.set(ItemStack.EMPTY));
            }

            @Override
            public boolean recipeMatches(RecipeHolder<AbstractCookingRecipe> $$0) {
                return $$0.value().matches(new SingleRecipeInput(AbstractFurnaceMenu.this.container.getItem(0)), (Level)$$3);
            }
        }, 1, 1, List.of(this.getSlot(0)), $$5, $$4, $$6, $$0, $$1);
    }
}

