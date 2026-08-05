/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public class BeaconMenu
extends AbstractContainerMenu {
    private static final int PAYMENT_SLOT = 0;
    private static final int SLOT_COUNT = 1;
    private static final int DATA_COUNT = 3;
    private static final int INV_SLOT_START = 1;
    private static final int INV_SLOT_END = 28;
    private static final int USE_ROW_SLOT_START = 28;
    private static final int USE_ROW_SLOT_END = 37;
    private static final int NO_EFFECT = 0;
    private final Container beacon = new SimpleContainer(this, 1){

        @Override
        public boolean canPlaceItem(int $$0, ItemStack $$1) {
            return $$1.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };
    private final PaymentSlot paymentSlot;
    private final ContainerLevelAccess access;
    private final ContainerData beaconData;

    public BeaconMenu(int $$0, Container $$1) {
        this($$0, $$1, new SimpleContainerData(3), ContainerLevelAccess.NULL);
    }

    public BeaconMenu(int $$0, Container $$1, ContainerData $$2, ContainerLevelAccess $$3) {
        super(MenuType.BEACON, $$0);
        BeaconMenu.checkContainerDataCount($$2, 3);
        this.beaconData = $$2;
        this.access = $$3;
        this.paymentSlot = new PaymentSlot(this.beacon, 0, 136, 110);
        this.addSlot(this.paymentSlot);
        this.addDataSlots($$2);
        this.addStandardInventorySlots($$1, 36, 137);
    }

    @Override
    public void removed(Player $$0) {
        super.removed($$0);
        if ($$0.level().isClientSide()) {
            return;
        }
        ItemStack $$1 = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());
        if (!$$1.isEmpty()) {
            $$0.drop($$1, false);
        }
    }

    @Override
    public boolean stillValid(Player $$0) {
        return BeaconMenu.stillValid(this.access, $$0, Blocks.BEACON);
    }

    @Override
    public void setData(int $$0, int $$1) {
        super.setData($$0, $$1);
        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player $$0, int $$1) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = (Slot)this.slots.get($$1);
        if ($$3 != null && $$3.hasItem()) {
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            if ($$1 == 0) {
                if (!this.moveItemStackTo($$4, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                $$3.onQuickCraft($$4, $$2);
            } else if (!this.paymentSlot.hasItem() && this.paymentSlot.mayPlace($$4) && $$4.getCount() == 1 ? !this.moveItemStackTo($$4, 0, 1, false) : ($$1 >= 1 && $$1 < 28 ? !this.moveItemStackTo($$4, 28, 37, false) : ($$1 >= 28 && $$1 < 37 ? !this.moveItemStackTo($$4, 1, 28, false) : !this.moveItemStackTo($$4, 1, 37, false)))) {
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

    public int getLevels() {
        return this.beaconData.get(0);
    }

    public static int encodeEffect(@Nullable Holder<MobEffect> $$0) {
        return $$0 == null ? 0 : BuiltInRegistries.MOB_EFFECT.asHolderIdMap().getId($$0) + 1;
    }

    public static @Nullable Holder<MobEffect> decodeEffect(int $$0) {
        return $$0 == 0 ? null : BuiltInRegistries.MOB_EFFECT.asHolderIdMap().byId($$0 - 1);
    }

    public @Nullable Holder<MobEffect> getPrimaryEffect() {
        return BeaconMenu.decodeEffect(this.beaconData.get(1));
    }

    public @Nullable Holder<MobEffect> getSecondaryEffect() {
        return BeaconMenu.decodeEffect(this.beaconData.get(2));
    }

    public void updateEffects(Optional<Holder<MobEffect>> $$0, Optional<Holder<MobEffect>> $$1) {
        if (this.paymentSlot.hasItem()) {
            this.beaconData.set(1, BeaconMenu.encodeEffect($$0.orElse(null)));
            this.beaconData.set(2, BeaconMenu.encodeEffect($$1.orElse(null)));
            this.paymentSlot.remove(1);
            this.access.execute(Level::blockEntityChanged);
        }
    }

    public boolean hasPayment() {
        return !this.beacon.getItem(0).isEmpty();
    }

    static class PaymentSlot
    extends Slot {
        public PaymentSlot(Container $$0, int $$1, int $$2, int $$3) {
            super($$0, $$1, $$2, $$3);
        }

        @Override
        public boolean mayPlace(ItemStack $$0) {
            return $$0.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}

