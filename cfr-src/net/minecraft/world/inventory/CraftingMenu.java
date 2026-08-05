/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public class CraftingMenu
extends AbstractCraftingMenu {
    private static final int CRAFTING_GRID_WIDTH = 3;
    private static final int CRAFTING_GRID_HEIGHT = 3;
    public static final int RESULT_SLOT = 0;
    private static final int CRAFT_SLOT_START = 1;
    private static final int CRAFT_SLOT_COUNT = 9;
    private static final int CRAFT_SLOT_END = 10;
    private static final int INV_SLOT_START = 10;
    private static final int INV_SLOT_END = 37;
    private static final int USE_ROW_SLOT_START = 37;
    private static final int USE_ROW_SLOT_END = 46;
    private final ContainerLevelAccess access;
    private final Player player;
    private boolean placingRecipe;

    public CraftingMenu(int $$0, Inventory $$1) {
        this($$0, $$1, ContainerLevelAccess.NULL);
    }

    public CraftingMenu(int $$0, Inventory $$1, ContainerLevelAccess $$2) {
        super(MenuType.CRAFTING, $$0, 3, 3);
        this.access = $$2;
        this.player = $$1.player;
        this.addResultSlot(this.player, 124, 35);
        this.addCraftingGridSlots(30, 17);
        this.addStandardInventorySlots($$1, 8, 84);
    }

    protected static void slotChangedCraftingGrid(AbstractContainerMenu $$0, ServerLevel $$1, Player $$2, CraftingContainer $$3, ResultContainer $$4, @Nullable RecipeHolder<CraftingRecipe> $$5) {
        CraftingInput $$6 = $$3.asCraftInput();
        ServerPlayer $$7 = (ServerPlayer)$$2;
        ItemStack $$8 = ItemStack.EMPTY;
        Optional<RecipeHolder<CraftingRecipe>> $$9 = $$1.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, $$6, (Level)$$1, $$5);
        if ($$9.isPresent()) {
            ItemStack $$12;
            RecipeHolder<CraftingRecipe> $$10 = $$9.get();
            CraftingRecipe $$11 = $$10.value();
            if ($$4.setRecipeUsed($$7, $$10) && ($$12 = $$11.assemble($$6, $$1.registryAccess())).isItemEnabled($$1.enabledFeatures())) {
                $$8 = $$12;
            }
        }
        $$4.setItem(0, $$8);
        $$0.setRemoteSlot(0, $$8);
        $$7.connection.send(new ClientboundContainerSetSlotPacket($$0.containerId, $$0.incrementStateId(), 0, $$8));
    }

    @Override
    public void slotsChanged(Container $$02) {
        if (!this.placingRecipe) {
            this.access.execute(($$0, $$1) -> {
                if ($$0 instanceof ServerLevel) {
                    ServerLevel $$2 = (ServerLevel)$$0;
                    CraftingMenu.slotChangedCraftingGrid(this, $$2, this.player, this.craftSlots, this.resultSlots, null);
                }
            });
        }
    }

    @Override
    public void beginPlacingRecipe() {
        this.placingRecipe = true;
    }

    @Override
    public void finishPlacingRecipe(ServerLevel $$0, RecipeHolder<CraftingRecipe> $$1) {
        this.placingRecipe = false;
        CraftingMenu.slotChangedCraftingGrid(this, $$0, this.player, this.craftSlots, this.resultSlots, $$1);
    }

    @Override
    public void removed(Player $$0) {
        super.removed($$0);
        this.access.execute(($$1, $$2) -> this.clearContainer($$0, this.craftSlots));
    }

    @Override
    public boolean stillValid(Player $$0) {
        return CraftingMenu.stillValid(this.access, $$0, Blocks.CRAFTING_TABLE);
    }

    @Override
    public ItemStack quickMoveStack(Player $$0, int $$1) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = (Slot)this.slots.get($$1);
        if ($$3 != null && $$3.hasItem()) {
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            if ($$1 == 0) {
                $$4.getItem().onCraftedBy($$4, $$0);
                if (!this.moveItemStackTo($$4, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                $$3.onQuickCraft($$4, $$2);
            } else if ($$1 >= 10 && $$1 < 46 ? !this.moveItemStackTo($$4, 1, 10, false) && ($$1 < 37 ? !this.moveItemStackTo($$4, 37, 46, false) : !this.moveItemStackTo($$4, 10, 37, false)) : !this.moveItemStackTo($$4, 10, 46, false)) {
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
            if ($$1 == 0) {
                $$0.drop($$4, false);
            }
        }
        return $$2;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack $$0, Slot $$1) {
        return $$1.container != this.resultSlots && super.canTakeItemForPickAll($$0, $$1);
    }

    @Override
    public Slot getResultSlot() {
        return (Slot)this.slots.get(0);
    }

    @Override
    public List<Slot> getInputGridSlots() {
        return this.slots.subList(1, 10);
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    protected Player owner() {
        return this.player;
    }
}

