/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InventoryMenu
extends AbstractCraftingMenu {
    public static final int CONTAINER_ID = 0;
    public static final int RESULT_SLOT = 0;
    private static final int CRAFTING_GRID_WIDTH = 2;
    private static final int CRAFTING_GRID_HEIGHT = 2;
    public static final int CRAFT_SLOT_START = 1;
    public static final int CRAFT_SLOT_COUNT = 4;
    public static final int CRAFT_SLOT_END = 5;
    public static final int ARMOR_SLOT_START = 5;
    public static final int ARMOR_SLOT_COUNT = 4;
    public static final int ARMOR_SLOT_END = 9;
    public static final int INV_SLOT_START = 9;
    public static final int INV_SLOT_END = 36;
    public static final int USE_ROW_SLOT_START = 36;
    public static final int USE_ROW_SLOT_END = 45;
    public static final int SHIELD_SLOT = 45;
    public static final Identifier EMPTY_ARMOR_SLOT_HELMET = Identifier.withDefaultNamespace("container/slot/helmet");
    public static final Identifier EMPTY_ARMOR_SLOT_CHESTPLATE = Identifier.withDefaultNamespace("container/slot/chestplate");
    public static final Identifier EMPTY_ARMOR_SLOT_LEGGINGS = Identifier.withDefaultNamespace("container/slot/leggings");
    public static final Identifier EMPTY_ARMOR_SLOT_BOOTS = Identifier.withDefaultNamespace("container/slot/boots");
    public static final Identifier EMPTY_ARMOR_SLOT_SHIELD = Identifier.withDefaultNamespace("container/slot/shield");
    private static final Map<EquipmentSlot, Identifier> TEXTURE_EMPTY_SLOTS = Map.of(EquipmentSlot.FEET, EMPTY_ARMOR_SLOT_BOOTS, EquipmentSlot.LEGS, EMPTY_ARMOR_SLOT_LEGGINGS, EquipmentSlot.CHEST, EMPTY_ARMOR_SLOT_CHESTPLATE, EquipmentSlot.HEAD, EMPTY_ARMOR_SLOT_HELMET);
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public final boolean active;
    private final Player owner;

    public InventoryMenu(Inventory $$0, boolean $$1, final Player $$2) {
        super(null, 0, 2, 2);
        this.active = $$1;
        this.owner = $$2;
        this.addResultSlot($$2, 154, 28);
        this.addCraftingGridSlots(98, 18);
        for (int $$3 = 0; $$3 < 4; ++$$3) {
            EquipmentSlot $$4 = SLOT_IDS[$$3];
            Identifier $$5 = TEXTURE_EMPTY_SLOTS.get($$4);
            this.addSlot(new ArmorSlot($$0, $$2, $$4, 39 - $$3, 8, 8 + $$3 * 18, $$5));
        }
        this.addStandardInventorySlots($$0, 8, 84);
        this.addSlot(new Slot(this, $$0, 40, 77, 62){

            @Override
            public void setByPlayer(ItemStack $$0, ItemStack $$1) {
                $$2.onEquipItem(EquipmentSlot.OFFHAND, $$1, $$0);
                super.setByPlayer($$0, $$1);
            }

            @Override
            public Identifier getNoItemIcon() {
                return EMPTY_ARMOR_SLOT_SHIELD;
            }
        });
    }

    public static boolean isHotbarSlot(int $$0) {
        return $$0 >= 36 && $$0 < 45 || $$0 == 45;
    }

    @Override
    public void slotsChanged(Container $$0) {
        Level level = this.owner.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            CraftingMenu.slotChangedCraftingGrid(this, $$1, this.owner, this.craftSlots, this.resultSlots, null);
        }
    }

    @Override
    public void removed(Player $$0) {
        super.removed($$0);
        this.resultSlots.clearContent();
        if ($$0.level().isClientSide()) {
            return;
        }
        this.clearContainer($$0, this.craftSlots);
    }

    @Override
    public boolean stillValid(Player $$0) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player $$0, int $$1) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = (Slot)this.slots.get($$1);
        if ($$3.hasItem()) {
            int $$6;
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            EquipmentSlot $$5 = $$0.getEquipmentSlotForItem($$2);
            if ($$1 == 0) {
                if (!this.moveItemStackTo($$4, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
                $$3.onQuickCraft($$4, $$2);
            } else if ($$1 >= 1 && $$1 < 5 ? !this.moveItemStackTo($$4, 9, 45, false) : ($$1 >= 5 && $$1 < 9 ? !this.moveItemStackTo($$4, 9, 45, false) : ($$5.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !((Slot)this.slots.get(8 - $$5.getIndex())).hasItem() ? !this.moveItemStackTo($$4, $$6 = 8 - $$5.getIndex(), $$6 + 1, false) : ($$5 == EquipmentSlot.OFFHAND && !((Slot)this.slots.get(45)).hasItem() ? !this.moveItemStackTo($$4, 45, 46, false) : ($$1 >= 9 && $$1 < 36 ? !this.moveItemStackTo($$4, 36, 45, false) : ($$1 >= 36 && $$1 < 45 ? !this.moveItemStackTo($$4, 9, 36, false) : !this.moveItemStackTo($$4, 9, 45, false))))))) {
                return ItemStack.EMPTY;
            }
            if ($$4.isEmpty()) {
                $$3.setByPlayer(ItemStack.EMPTY, $$2);
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
        return this.slots.subList(1, 5);
    }

    public CraftingContainer getCraftSlots() {
        return this.craftSlots;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    protected Player owner() {
        return this.owner;
    }
}

