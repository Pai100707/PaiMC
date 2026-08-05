/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 */
package net.minecraft.world.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class GrindstoneMenu
extends AbstractContainerMenu {
    public static final int MAX_NAME_LENGTH = 35;
    public static final int INPUT_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    private final Container resultSlots = new ResultContainer();
    final Container repairSlots = new SimpleContainer(2){

        @Override
        public void setChanged() {
            super.setChanged();
            GrindstoneMenu.this.slotsChanged(this);
        }
    };
    private final ContainerLevelAccess access;

    public GrindstoneMenu(int $$0, Inventory $$1) {
        this($$0, $$1, ContainerLevelAccess.NULL);
    }

    public GrindstoneMenu(int $$0, Inventory $$1, final ContainerLevelAccess $$2) {
        super(MenuType.GRINDSTONE, $$0);
        this.access = $$2;
        this.addSlot(new Slot(this, this.repairSlots, 0, 49, 19){

            @Override
            public boolean mayPlace(ItemStack $$0) {
                return $$0.isDamageableItem() || EnchantmentHelper.hasAnyEnchantments($$0);
            }
        });
        this.addSlot(new Slot(this, this.repairSlots, 1, 49, 40){

            @Override
            public boolean mayPlace(ItemStack $$0) {
                return $$0.isDamageableItem() || EnchantmentHelper.hasAnyEnchantments($$0);
            }
        });
        this.addSlot(new Slot(this.resultSlots, 2, 129, 34){

            @Override
            public boolean mayPlace(ItemStack $$0) {
                return false;
            }

            @Override
            public void onTake(Player $$02, ItemStack $$12) {
                $$2.execute(($$0, $$1) -> {
                    if ($$0 instanceof ServerLevel) {
                        ExperienceOrb.award((ServerLevel)$$0, Vec3.atCenterOf($$1), this.getExperienceAmount((Level)$$0));
                    }
                    $$0.levelEvent(1042, (BlockPos)$$1, 0);
                });
                GrindstoneMenu.this.repairSlots.setItem(0, ItemStack.EMPTY);
                GrindstoneMenu.this.repairSlots.setItem(1, ItemStack.EMPTY);
            }

            private int getExperienceAmount(Level $$0) {
                int $$1 = 0;
                $$1 += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(0));
                if (($$1 += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(1))) > 0) {
                    int $$22 = (int)Math.ceil((double)$$1 / 2.0);
                    return $$22 + $$0.random.nextInt($$22);
                }
                return 0;
            }

            private int getExperienceFromItem(ItemStack $$0) {
                int $$1 = 0;
                ItemEnchantments $$22 = EnchantmentHelper.getEnchantmentsForCrafting($$0);
                for (Object2IntMap.Entry<Holder<Enchantment>> $$3 : $$22.entrySet()) {
                    Holder $$4 = (Holder)$$3.getKey();
                    int $$5 = $$3.getIntValue();
                    if ($$4.is(EnchantmentTags.CURSE)) continue;
                    $$1 += ((Enchantment)$$4.value()).getMinCost($$5);
                }
                return $$1;
            }
        });
        this.addStandardInventorySlots($$1, 8, 84);
    }

    @Override
    public void slotsChanged(Container $$0) {
        super.slotsChanged($$0);
        if ($$0 == this.repairSlots) {
            this.createResult();
        }
    }

    private void createResult() {
        this.resultSlots.setItem(0, this.computeResult(this.repairSlots.getItem(0), this.repairSlots.getItem(1)));
        this.broadcastChanges();
    }

    private ItemStack computeResult(ItemStack $$0, ItemStack $$1) {
        boolean $$3;
        boolean $$2;
        boolean bl = $$2 = !$$0.isEmpty() || !$$1.isEmpty();
        if (!$$2) {
            return ItemStack.EMPTY;
        }
        if ($$0.getCount() > 1 || $$1.getCount() > 1) {
            return ItemStack.EMPTY;
        }
        boolean bl2 = $$3 = !$$0.isEmpty() && !$$1.isEmpty();
        if (!$$3) {
            ItemStack $$4;
            ItemStack itemStack = $$4 = !$$0.isEmpty() ? $$0 : $$1;
            if (!EnchantmentHelper.hasAnyEnchantments($$4)) {
                return ItemStack.EMPTY;
            }
            return this.removeNonCursesFrom($$4.copy());
        }
        return this.mergeItems($$0, $$1);
    }

    private ItemStack mergeItems(ItemStack $$0, ItemStack $$1) {
        ItemStack $$7;
        if (!$$0.is($$1.getItem())) {
            return ItemStack.EMPTY;
        }
        int $$2 = Math.max($$0.getMaxDamage(), $$1.getMaxDamage());
        int $$3 = $$0.getMaxDamage() - $$0.getDamageValue();
        int $$4 = $$1.getMaxDamage() - $$1.getDamageValue();
        int $$5 = $$3 + $$4 + $$2 * 5 / 100;
        int $$6 = 1;
        if (!$$0.isDamageableItem()) {
            if ($$0.getMaxStackSize() < 2 || !ItemStack.matches($$0, $$1)) {
                return ItemStack.EMPTY;
            }
            $$6 = 2;
        }
        if (($$7 = $$0.copyWithCount($$6)).isDamageableItem()) {
            $$7.set(DataComponents.MAX_DAMAGE, $$2);
            $$7.setDamageValue(Math.max($$2 - $$5, 0));
        }
        this.mergeEnchantsFrom($$7, $$1);
        return this.removeNonCursesFrom($$7);
    }

    private void mergeEnchantsFrom(ItemStack $$0, ItemStack $$12) {
        EnchantmentHelper.updateEnchantments($$0, $$1 -> {
            ItemEnchantments $$2 = EnchantmentHelper.getEnchantmentsForCrafting($$12);
            for (Object2IntMap.Entry<Holder<Enchantment>> $$3 : $$2.entrySet()) {
                Holder $$4 = (Holder)$$3.getKey();
                if ($$4.is(EnchantmentTags.CURSE) && $$1.getLevel($$4) != 0) continue;
                $$1.upgrade($$4, $$3.getIntValue());
            }
        });
    }

    private ItemStack removeNonCursesFrom(ItemStack $$0) {
        ItemEnchantments $$1 = EnchantmentHelper.updateEnchantments($$0, $$02 -> $$02.removeIf($$0 -> !$$0.is(EnchantmentTags.CURSE)));
        if ($$0.is(Items.ENCHANTED_BOOK) && $$1.isEmpty()) {
            $$0 = $$0.transmuteCopy(Items.BOOK);
        }
        int $$2 = 0;
        for (int $$3 = 0; $$3 < $$1.size(); ++$$3) {
            $$2 = AnvilMenu.calculateIncreasedRepairCost($$2);
        }
        $$0.set(DataComponents.REPAIR_COST, $$2);
        return $$0;
    }

    @Override
    public void removed(Player $$0) {
        super.removed($$0);
        this.access.execute(($$1, $$2) -> this.clearContainer($$0, this.repairSlots));
    }

    @Override
    public boolean stillValid(Player $$0) {
        return GrindstoneMenu.stillValid(this.access, $$0, Blocks.GRINDSTONE);
    }

    @Override
    public ItemStack quickMoveStack(Player $$0, int $$1) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = (Slot)this.slots.get($$1);
        if ($$3 != null && $$3.hasItem()) {
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            ItemStack $$5 = this.repairSlots.getItem(0);
            ItemStack $$6 = this.repairSlots.getItem(1);
            if ($$1 == 2) {
                if (!this.moveItemStackTo($$4, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                $$3.onQuickCraft($$4, $$2);
            } else if ($$1 == 0 || $$1 == 1 ? !this.moveItemStackTo($$4, 3, 39, false) : ($$5.isEmpty() || $$6.isEmpty() ? !this.moveItemStackTo($$4, 0, 2, false) : ($$1 >= 3 && $$1 < 30 ? !this.moveItemStackTo($$4, 30, 39, false) : $$1 >= 30 && $$1 < 39 && !this.moveItemStackTo($$4, 3, 30, false)))) {
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
}

