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
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;

public class BundleItem extends net.minecraft.world.item.Item {
   public static final int MAX_SHOWN_GRID_ITEMS_X = 4;
   public static final int MAX_SHOWN_GRID_ITEMS_Y = 3;
   public static final int MAX_SHOWN_GRID_ITEMS = 12;
   public static final int OVERFLOWING_MAX_SHOWN_GRID_ITEMS = 11;
   private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
   private static final int BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);
   private static final int TICKS_AFTER_FIRST_THROW = 10;
   private static final int TICKS_BETWEEN_THROWS = 2;
   private static final int TICKS_MAX_THROW_DURATION = 200;

   public BundleItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   public static float getFullnessDisplay(net.minecraft.world.item.ItemStack $$0) {
      BundleContents $$1 = (BundleContents)$$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
      return $$1.weight().floatValue();
   }

   @Override
   public boolean overrideStackedOnOther(net.minecraft.world.item.ItemStack $$0, Slot $$1, ClickAction $$2, Player $$3) {
      BundleContents $$4 = (BundleContents)$$0.get(DataComponents.BUNDLE_CONTENTS);
      if ($$4 == null) {
         return false;
      } else {
         net.minecraft.world.item.ItemStack $$5 = $$1.getItem();
         BundleContents.Mutable $$6 = new BundleContents.Mutable($$4);
         if ($$2 == ClickAction.PRIMARY && !$$5.isEmpty()) {
            if ($$6.tryTransfer($$1, $$3) > 0) {
               playInsertSound($$3);
            } else {
               playInsertFailSound($$3);
            }

            $$0.set(DataComponents.BUNDLE_CONTENTS, $$6.toImmutable());
            this.broadcastChangesOnContainerMenu($$3);
            return true;
         } else if ($$2 == ClickAction.SECONDARY && $$5.isEmpty()) {
            net.minecraft.world.item.ItemStack $$7 = $$6.removeOne();
            if ($$7 != null) {
               net.minecraft.world.item.ItemStack $$8 = $$1.safeInsert($$7);
               if ($$8.getCount() > 0) {
                  $$6.tryInsert($$8);
               } else {
                  playRemoveOneSound($$3);
               }
            }

            $$0.set(DataComponents.BUNDLE_CONTENTS, $$6.toImmutable());
            this.broadcastChangesOnContainerMenu($$3);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean overrideOtherStackedOnMe(
      net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1, Slot $$2, ClickAction $$3, Player $$4, SlotAccess $$5
   ) {
      if ($$3 == ClickAction.PRIMARY && $$1.isEmpty()) {
         toggleSelectedItem($$0, -1);
         return false;
      } else {
         BundleContents $$6 = (BundleContents)$$0.get(DataComponents.BUNDLE_CONTENTS);
         if ($$6 == null) {
            return false;
         } else {
            BundleContents.Mutable $$7 = new BundleContents.Mutable($$6);
            if ($$3 == ClickAction.PRIMARY && !$$1.isEmpty()) {
               if ($$2.allowModification($$4) && $$7.tryInsert($$1) > 0) {
                  playInsertSound($$4);
               } else {
                  playInsertFailSound($$4);
               }

               $$0.set(DataComponents.BUNDLE_CONTENTS, $$7.toImmutable());
               this.broadcastChangesOnContainerMenu($$4);
               return true;
            } else if ($$3 == ClickAction.SECONDARY && $$1.isEmpty()) {
               if ($$2.allowModification($$4)) {
                  net.minecraft.world.item.ItemStack $$8 = $$7.removeOne();
                  if ($$8 != null) {
                     playRemoveOneSound($$4);
                     $$5.set($$8);
                  }
               }

               $$0.set(DataComponents.BUNDLE_CONTENTS, $$7.toImmutable());
               this.broadcastChangesOnContainerMenu($$4);
               return true;
            } else {
               toggleSelectedItem($$0, -1);
               return false;
            }
         }
      }
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      $$1.startUsingItem($$2);
      return InteractionResult.SUCCESS;
   }

   private void dropContent(Level $$0, Player $$1, net.minecraft.world.item.ItemStack $$2) {
      if (this.dropContent($$2, $$1)) {
         playDropContentsSound($$0, $$1);
         $$1.awardStat(Stats.ITEM_USED.get(this));
      }
   }

   @Override
   public boolean isBarVisible(net.minecraft.world.item.ItemStack $$0) {
      BundleContents $$1 = (BundleContents)$$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
      return $$1.weight().compareTo(Fraction.ZERO) > 0;
   }

   @Override
   public int getBarWidth(net.minecraft.world.item.ItemStack $$0) {
      BundleContents $$1 = (BundleContents)$$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
      return Math.min(1 + Mth.mulAndTruncate($$1.weight(), 12), 13);
   }

   @Override
   public int getBarColor(net.minecraft.world.item.ItemStack $$0) {
      BundleContents $$1 = (BundleContents)$$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
      return $$1.weight().compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
   }

   public static void toggleSelectedItem(net.minecraft.world.item.ItemStack $$0, int $$1) {
      BundleContents $$2 = (BundleContents)$$0.get(DataComponents.BUNDLE_CONTENTS);
      if ($$2 != null) {
         BundleContents.Mutable $$3 = new BundleContents.Mutable($$2);
         $$3.toggleSelectedItem($$1);
         $$0.set(DataComponents.BUNDLE_CONTENTS, $$3.toImmutable());
      }
   }

   public static boolean hasSelectedItem(net.minecraft.world.item.ItemStack $$0) {
      BundleContents $$1 = (BundleContents)$$0.get(DataComponents.BUNDLE_CONTENTS);
      return $$1 != null && $$1.getSelectedItem() != -1;
   }

   public static int getSelectedItem(net.minecraft.world.item.ItemStack $$0) {
      BundleContents $$1 = (BundleContents)$$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
      return $$1.getSelectedItem();
   }

   public static net.minecraft.world.item.ItemStack getSelectedItemStack(net.minecraft.world.item.ItemStack $$0) {
      BundleContents $$1 = (BundleContents)$$0.get(DataComponents.BUNDLE_CONTENTS);
      return $$1 != null && $$1.getSelectedItem() != -1 ? $$1.getItemUnsafe($$1.getSelectedItem()) : net.minecraft.world.item.ItemStack.EMPTY;
   }

   public static int getNumberOfItemsToShow(net.minecraft.world.item.ItemStack $$0) {
      BundleContents $$1 = (BundleContents)$$0.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
      return $$1.getNumberOfItemsToShow();
   }

   private boolean dropContent(net.minecraft.world.item.ItemStack $$0, Player $$1) {
      BundleContents $$2 = (BundleContents)$$0.get(DataComponents.BUNDLE_CONTENTS);
      if ($$2 != null && !$$2.isEmpty()) {
         Optional<net.minecraft.world.item.ItemStack> $$3 = removeOneItemFromBundle($$0, $$1, $$2);
         if ($$3.isPresent()) {
            $$1.drop($$3.get(), true);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static Optional<net.minecraft.world.item.ItemStack> removeOneItemFromBundle(net.minecraft.world.item.ItemStack $$0, Player $$1, BundleContents $$2) {
      BundleContents.Mutable $$3 = new BundleContents.Mutable($$2);
      net.minecraft.world.item.ItemStack $$4 = $$3.removeOne();
      if ($$4 != null) {
         playRemoveOneSound($$1);
         $$0.set(DataComponents.BUNDLE_CONTENTS, $$3.toImmutable());
         return Optional.of($$4);
      } else {
         return Optional.empty();
      }
   }

   @Override
   public void onUseTick(Level $$0, LivingEntity $$1, net.minecraft.world.item.ItemStack $$2, int $$3) {
      if ($$1 instanceof Player $$4) {
         int $$5 = this.getUseDuration($$2, $$1);
         boolean $$6 = $$3 == $$5;
         if ($$6 || $$3 < $$5 - 10 && $$3 % 2 == 0) {
            this.dropContent($$0, $$4, $$2);
         }
      }
   }

   @Override
   public int getUseDuration(net.minecraft.world.item.ItemStack $$0, LivingEntity $$1) {
      return 200;
   }

   @Override
   public net.minecraft.world.item.ItemUseAnimation getUseAnimation(net.minecraft.world.item.ItemStack $$0) {
      return net.minecraft.world.item.ItemUseAnimation.BUNDLE;
   }

   @Override
   public Optional<TooltipComponent> getTooltipImage(net.minecraft.world.item.ItemStack $$0) {
      TooltipDisplay $$1 = (TooltipDisplay)$$0.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
      return !$$1.shows(DataComponents.BUNDLE_CONTENTS)
         ? Optional.empty()
         : Optional.ofNullable((BundleContents)$$0.get(DataComponents.BUNDLE_CONTENTS)).map(BundleTooltip::new);
   }

   @Override
   public void onDestroyed(ItemEntity $$0) {
      BundleContents $$1 = (BundleContents)$$0.getItem().get(DataComponents.BUNDLE_CONTENTS);
      if ($$1 != null) {
         $$0.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
         net.minecraft.world.item.ItemUtils.onContainerDestroyed($$0, $$1.itemsCopy());
      }
   }

   public static List<net.minecraft.world.item.BundleItem> getAllBundleItemColors() {
      return Stream.of(
            net.minecraft.world.item.Items.BUNDLE,
            net.minecraft.world.item.Items.WHITE_BUNDLE,
            net.minecraft.world.item.Items.ORANGE_BUNDLE,
            net.minecraft.world.item.Items.MAGENTA_BUNDLE,
            net.minecraft.world.item.Items.LIGHT_BLUE_BUNDLE,
            net.minecraft.world.item.Items.YELLOW_BUNDLE,
            net.minecraft.world.item.Items.LIME_BUNDLE,
            net.minecraft.world.item.Items.PINK_BUNDLE,
            net.minecraft.world.item.Items.GRAY_BUNDLE,
            net.minecraft.world.item.Items.LIGHT_GRAY_BUNDLE,
            net.minecraft.world.item.Items.CYAN_BUNDLE,
            net.minecraft.world.item.Items.BLACK_BUNDLE,
            net.minecraft.world.item.Items.BROWN_BUNDLE,
            net.minecraft.world.item.Items.GREEN_BUNDLE,
            net.minecraft.world.item.Items.RED_BUNDLE,
            net.minecraft.world.item.Items.BLUE_BUNDLE,
            net.minecraft.world.item.Items.PURPLE_BUNDLE
         )
         .map($$0 -> (net.minecraft.world.item.BundleItem)$$0)
         .toList();
   }

   public static net.minecraft.world.item.Item getByColor(net.minecraft.world.item.DyeColor $$0) {
      return switch ($$0) {
         case WHITE -> net.minecraft.world.item.Items.WHITE_BUNDLE;
         case ORANGE -> net.minecraft.world.item.Items.ORANGE_BUNDLE;
         case MAGENTA -> net.minecraft.world.item.Items.MAGENTA_BUNDLE;
         case LIGHT_BLUE -> net.minecraft.world.item.Items.LIGHT_BLUE_BUNDLE;
         case YELLOW -> net.minecraft.world.item.Items.YELLOW_BUNDLE;
         case LIME -> net.minecraft.world.item.Items.LIME_BUNDLE;
         case PINK -> net.minecraft.world.item.Items.PINK_BUNDLE;
         case GRAY -> net.minecraft.world.item.Items.GRAY_BUNDLE;
         case LIGHT_GRAY -> net.minecraft.world.item.Items.LIGHT_GRAY_BUNDLE;
         case CYAN -> net.minecraft.world.item.Items.CYAN_BUNDLE;
         case BLUE -> net.minecraft.world.item.Items.BLUE_BUNDLE;
         case BROWN -> net.minecraft.world.item.Items.BROWN_BUNDLE;
         case GREEN -> net.minecraft.world.item.Items.GREEN_BUNDLE;
         case RED -> net.minecraft.world.item.Items.RED_BUNDLE;
         case BLACK -> net.minecraft.world.item.Items.BLACK_BUNDLE;
         case PURPLE -> net.minecraft.world.item.Items.PURPLE_BUNDLE;
      };
   }

   private static void playRemoveOneSound(Entity $$0) {
      $$0.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + $$0.level().getRandom().nextFloat() * 0.4F);
   }

   private static void playInsertSound(Entity $$0) {
      $$0.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + $$0.level().getRandom().nextFloat() * 0.4F);
   }

   private static void playInsertFailSound(Entity $$0) {
      $$0.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
   }

   private static void playDropContentsSound(Level $$0, Entity $$1) {
      $$0.playSound(null, $$1.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 0.8F + $$1.level().getRandom().nextFloat() * 0.4F);
   }

   private void broadcastChangesOnContainerMenu(Player $$0) {
      AbstractContainerMenu $$1 = $$0.containerMenu;
      if ($$1 != null) {
         $$1.slotsChanged($$0.getInventory());
      }
   }
}
