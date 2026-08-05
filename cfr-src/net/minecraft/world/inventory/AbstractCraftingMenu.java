/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class AbstractCraftingMenu
extends RecipeBookMenu {
    private final int width;
    private final int height;
    protected final CraftingContainer craftSlots;
    protected final ResultContainer resultSlots = new ResultContainer();

    public AbstractCraftingMenu(MenuType<?> $$0, int $$1, int $$2, int $$3) {
        super($$0, $$1);
        this.width = $$2;
        this.height = $$3;
        this.craftSlots = new TransientCraftingContainer(this, $$2, $$3);
    }

    protected Slot addResultSlot(Player $$0, int $$1, int $$2) {
        return this.addSlot(new ResultSlot($$0, this.craftSlots, this.resultSlots, 0, $$1, $$2));
    }

    protected void addCraftingGridSlots(int $$0, int $$1) {
        for (int $$2 = 0; $$2 < this.width; ++$$2) {
            for (int $$3 = 0; $$3 < this.height; ++$$3) {
                this.addSlot(new Slot(this.craftSlots, $$3 + $$2 * this.width, $$0 + $$3 * 18, $$1 + $$2 * 18));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public RecipeBookMenu.PostPlaceAction handlePlacement(boolean $$0, boolean $$1, RecipeHolder<?> $$2, ServerLevel $$3, Inventory $$4) {
        RecipeHolder<CraftingRecipe> $$5 = $$2;
        this.beginPlacingRecipe();
        try {
            List<Slot> $$6 = this.getInputGridSlots();
            RecipeBookMenu.PostPlaceAction postPlaceAction = ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<CraftingRecipe>(){

                @Override
                public void fillCraftSlotsStackedContents(StackedItemContents $$0) {
                    AbstractCraftingMenu.this.fillCraftSlotsStackedContents($$0);
                }

                @Override
                public void clearCraftingContent() {
                    AbstractCraftingMenu.this.resultSlots.clearContent();
                    AbstractCraftingMenu.this.craftSlots.clearContent();
                }

                @Override
                public boolean recipeMatches(RecipeHolder<CraftingRecipe> $$0) {
                    return $$0.value().matches(AbstractCraftingMenu.this.craftSlots.asCraftInput(), AbstractCraftingMenu.this.owner().level());
                }
            }, this.width, this.height, $$6, $$6, $$4, $$5, $$0, $$1);
            return postPlaceAction;
        }
        finally {
            this.finishPlacingRecipe($$3, $$5);
        }
    }

    protected void beginPlacingRecipe() {
    }

    protected void finishPlacingRecipe(ServerLevel $$0, RecipeHolder<CraftingRecipe> $$1) {
    }

    public abstract Slot getResultSlot();

    public abstract List<Slot> getInputGridSlots();

    public int getGridWidth() {
        return this.width;
    }

    public int getGridHeight() {
        return this.height;
    }

    protected abstract Player owner();

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents $$0) {
        this.craftSlots.fillStackedContents($$0);
    }
}

