/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.math.Fraction
 */
package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;

public class BundleItem
extends Item {
    public static final int MAX_SHOWN_GRID_ITEMS_X = 4;
    public static final int MAX_SHOWN_GRID_ITEMS_Y = 3;
    public static final int MAX_SHOWN_GRID_ITEMS = 12;
    public static final int OVERFLOWING_MAX_SHOWN_GRID_ITEMS = 11;
    private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0f, 1.0f, 0.33f, 0.33f);
    private static final int BAR_COLOR = ARGB.colorFromFloat(1.0f, 0.44f, 0.53f, 1.0f);
    private static final int TICKS_AFTER_FIRST_THROW = 10;
    private static final int TICKS_BETWEEN_THROWS = 2;
    private static final int TICKS_MAX_THROW_DURATION = 200;

    public BundleItem(Item.Properties $$0) {
        super($$0);
    }

    public static float getFullnessDisplay(ItemStack $$0) {
        BundleContents $$1 = $$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return $$1.weight().floatValue();
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack $$0, Slot $$1, ClickAction $$2, Player $$3) {
        BundleContents $$4 = $$0.get(DataComponents.BUNDLE_CONTENTS);
        if ($$4 == null) {
            return false;
        }
        ItemStack $$5 = $$1.getItem();
        BundleContents.Mutable $$6 = new BundleContents.Mutable($$4);
        if ($$2 == ClickAction.PRIMARY && !$$5.isEmpty()) {
            if ($$6.tryTransfer($$1, $$3) > 0) {
                BundleItem.playInsertSound($$3);
            } else {
                BundleItem.playInsertFailSound($$3);
            }
            $$0.set(DataComponents.BUNDLE_CONTENTS, $$6.toImmutable());
            this.broadcastChangesOnContainerMenu($$3);
            return true;
        }
        if ($$2 == ClickAction.SECONDARY && $$5.isEmpty()) {
            ItemStack $$7 = $$6.removeOne();
            if ($$7 != null) {
                ItemStack $$8 = $$1.safeInsert($$7);
                if ($$8.getCount() > 0) {
                    $$6.tryInsert($$8);
                } else {
                    BundleItem.playRemoveOneSound($$3);
                }
            }
            $$0.set(DataComponents.BUNDLE_CONTENTS, $$6.toImmutable());
            this.broadcastChangesOnContainerMenu($$3);
            return true;
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack $$0, ItemStack $$1, Slot $$2, ClickAction $$3, Player $$4, SlotAccess $$5) {
        if ($$3 == ClickAction.PRIMARY && $$1.isEmpty()) {
            BundleItem.toggleSelectedItem($$0, -1);
            return false;
        }
        BundleContents $$6 = $$0.get(DataComponents.BUNDLE_CONTENTS);
        if ($$6 == null) {
            return false;
        }
        BundleContents.Mutable $$7 = new BundleContents.Mutable($$6);
        if ($$3 == ClickAction.PRIMARY && !$$1.isEmpty()) {
            if ($$2.allowModification($$4) && $$7.tryInsert($$1) > 0) {
                BundleItem.playInsertSound($$4);
            } else {
                BundleItem.playInsertFailSound($$4);
            }
            $$0.set(DataComponents.BUNDLE_CONTENTS, $$7.toImmutable());
            this.broadcastChangesOnContainerMenu($$4);
            return true;
        }
        if ($$3 == ClickAction.SECONDARY && $$1.isEmpty()) {
            ItemStack $$8;
            if ($$2.allowModification($$4) && ($$8 = $$7.removeOne()) != null) {
                BundleItem.playRemoveOneSound($$4);
                $$5.set($$8);
            }
            $$0.set(DataComponents.BUNDLE_CONTENTS, $$7.toImmutable());
            this.broadcastChangesOnContainerMenu($$4);
            return true;
        }
        BundleItem.toggleSelectedItem($$0, -1);
        return false;
    }

    @Override
    public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
        $$1.startUsingItem($$2);
        return InteractionResult.SUCCESS;
    }

    private void dropContent(Level $$0, Player $$1, ItemStack $$2) {
        if (this.dropContent($$2, $$1)) {
            BundleItem.playDropContentsSound($$0, $$1);
            $$1.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack $$0) {
        BundleContents $$1 = $$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return $$1.weight().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getBarWidth(ItemStack $$0) {
        BundleContents $$1 = $$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return Math.min(1 + Mth.mulAndTruncate($$1.weight(), 12), 13);
    }

    @Override
    public int getBarColor(ItemStack $$0) {
        BundleContents $$1 = $$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return $$1.weight().compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
    }

    public static void toggleSelectedItem(ItemStack $$0, int $$1) {
        BundleContents $$2 = $$0.get(DataComponents.BUNDLE_CONTENTS);
        if ($$2 == null) {
            return;
        }
        BundleContents.Mutable $$3 = new BundleContents.Mutable($$2);
        $$3.toggleSelectedItem($$1);
        $$0.set(DataComponents.BUNDLE_CONTENTS, $$3.toImmutable());
    }

    public static boolean hasSelectedItem(ItemStack $$0) {
        BundleContents $$1 = $$0.get(DataComponents.BUNDLE_CONTENTS);
        return $$1 != null && $$1.getSelectedItem() != -1;
    }

    public static int getSelectedItem(ItemStack $$0) {
        BundleContents $$1 = $$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return $$1.getSelectedItem();
    }

    public static ItemStack getSelectedItemStack(ItemStack $$0) {
        BundleContents $$1 = $$0.get(DataComponents.BUNDLE_CONTENTS);
        if ($$1 != null && $$1.getSelectedItem() != -1) {
            return $$1.getItemUnsafe($$1.getSelectedItem());
        }
        return ItemStack.EMPTY;
    }

    public static int getNumberOfItemsToShow(ItemStack $$0) {
        BundleContents $$1 = $$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return $$1.getNumberOfItemsToShow();
    }

    private boolean dropContent(ItemStack $$0, Player $$1) {
        BundleContents $$2 = $$0.get(DataComponents.BUNDLE_CONTENTS);
        if ($$2 == null || $$2.isEmpty()) {
            return false;
        }
        Optional<ItemStack> $$3 = BundleItem.removeOneItemFromBundle($$0, $$1, $$2);
        if ($$3.isPresent()) {
            $$1.drop($$3.get(), true);
            return true;
        }
        return false;
    }

    private static Optional<ItemStack> removeOneItemFromBundle(ItemStack $$0, Player $$1, BundleContents $$2) {
        BundleContents.Mutable $$3 = new BundleContents.Mutable($$2);
        ItemStack $$4 = $$3.removeOne();
        if ($$4 != null) {
            BundleItem.playRemoveOneSound($$1);
            $$0.set(DataComponents.BUNDLE_CONTENTS, $$3.toImmutable());
            return Optional.of($$4);
        }
        return Optional.empty();
    }

    @Override
    public void onUseTick(Level $$0, LivingEntity $$1, ItemStack $$2, int $$3) {
        if ($$1 instanceof Player) {
            boolean $$6;
            Player $$4 = (Player)$$1;
            int $$5 = this.getUseDuration($$2, $$1);
            boolean bl = $$6 = $$3 == $$5;
            if ($$6 || $$3 < $$5 - 10 && $$3 % 2 == 0) {
                this.dropContent($$0, $$4, $$2);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack $$0, LivingEntity $$1) {
        return 200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack $$0) {
        return ItemUseAnimation.BUNDLE;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack $$0) {
        TooltipDisplay $$1 = $$0.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        if (!$$1.shows(DataComponents.BUNDLE_CONTENTS)) {
            return Optional.empty();
        }
        return Optional.ofNullable($$0.get(DataComponents.BUNDLE_CONTENTS)).map(BundleTooltip::new);
    }

    @Override
    public void onDestroyed(ItemEntity $$0) {
        BundleContents $$1 = $$0.getItem().get(DataComponents.BUNDLE_CONTENTS);
        if ($$1 == null) {
            return;
        }
        $$0.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        ItemUtils.onContainerDestroyed($$0, $$1.itemsCopy());
    }

    public static List<BundleItem> getAllBundleItemColors() {
        return Stream.of(Items.BUNDLE, Items.WHITE_BUNDLE, Items.ORANGE_BUNDLE, Items.MAGENTA_BUNDLE, Items.LIGHT_BLUE_BUNDLE, Items.YELLOW_BUNDLE, Items.LIME_BUNDLE, Items.PINK_BUNDLE, Items.GRAY_BUNDLE, Items.LIGHT_GRAY_BUNDLE, Items.CYAN_BUNDLE, Items.BLACK_BUNDLE, Items.BROWN_BUNDLE, Items.GREEN_BUNDLE, Items.RED_BUNDLE, Items.BLUE_BUNDLE, Items.PURPLE_BUNDLE).map($$0 -> (BundleItem)$$0).toList();
    }

    public static Item getByColor(DyeColor $$0) {
        return switch ($$0) {
            default -> throw new MatchException(null, null);
            case DyeColor.WHITE -> Items.WHITE_BUNDLE;
            case DyeColor.ORANGE -> Items.ORANGE_BUNDLE;
            case DyeColor.MAGENTA -> Items.MAGENTA_BUNDLE;
            case DyeColor.LIGHT_BLUE -> Items.LIGHT_BLUE_BUNDLE;
            case DyeColor.YELLOW -> Items.YELLOW_BUNDLE;
            case DyeColor.LIME -> Items.LIME_BUNDLE;
            case DyeColor.PINK -> Items.PINK_BUNDLE;
            case DyeColor.GRAY -> Items.GRAY_BUNDLE;
            case DyeColor.LIGHT_GRAY -> Items.LIGHT_GRAY_BUNDLE;
            case DyeColor.CYAN -> Items.CYAN_BUNDLE;
            case DyeColor.BLUE -> Items.BLUE_BUNDLE;
            case DyeColor.BROWN -> Items.BROWN_BUNDLE;
            case DyeColor.GREEN -> Items.GREEN_BUNDLE;
            case DyeColor.RED -> Items.RED_BUNDLE;
            case DyeColor.BLACK -> Items.BLACK_BUNDLE;
            case DyeColor.PURPLE -> Items.PURPLE_BUNDLE;
        };
    }

    private static void playRemoveOneSound(Entity $$0) {
        $$0.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 0.8f + $$0.level().getRandom().nextFloat() * 0.4f);
    }

    private static void playInsertSound(Entity $$0) {
        $$0.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + $$0.level().getRandom().nextFloat() * 0.4f);
    }

    private static void playInsertFailSound(Entity $$0) {
        $$0.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0f, 1.0f);
    }

    private static void playDropContentsSound(Level $$0, Entity $$1) {
        $$0.playSound(null, $$1.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8f, 0.8f + $$1.level().getRandom().nextFloat() * 0.4f);
    }

    private void broadcastChangesOnContainerMenu(Player $$0) {
        AbstractContainerMenu $$1 = $$0.containerMenu;
        if ($$1 != null) {
            $$1.slotsChanged($$0.getInventory());
        }
    }
}

