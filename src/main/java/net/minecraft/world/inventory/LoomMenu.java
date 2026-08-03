package net.minecraft.world.inventory;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatternLayers.Builder;

public class LoomMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
   private static final int PATTERN_NOT_SET = -1;
   private static final int INV_SLOT_START = 4;
   private static final int INV_SLOT_END = 31;
   private static final int USE_ROW_SLOT_START = 31;
   private static final int USE_ROW_SLOT_END = 40;
   private final net.minecraft.world.inventory.ContainerLevelAccess access;
   final net.minecraft.world.inventory.DataSlot selectedBannerPatternIndex = net.minecraft.world.inventory.DataSlot.standalone();
   private List<Holder<BannerPattern>> selectablePatterns = List.of();
   Runnable slotUpdateListener = () -> {};
   private final HolderGetter<BannerPattern> patternGetter;
   final net.minecraft.world.inventory.Slot bannerSlot;
   final net.minecraft.world.inventory.Slot dyeSlot;
   private final net.minecraft.world.inventory.Slot patternSlot;
   private final net.minecraft.world.inventory.Slot resultSlot;
   long lastSoundTime;
   private final Container inputContainer = new SimpleContainer(3) {
      public void setChanged() {
         super.setChanged();
         LoomMenu.this.slotsChanged(this);
         LoomMenu.this.slotUpdateListener.run();
      }
   };
   private final Container outputContainer = new SimpleContainer(1) {
      public void setChanged() {
         super.setChanged();
         LoomMenu.this.slotUpdateListener.run();
      }
   };

   public LoomMenu(int $$0, Inventory $$1) {
      this($$0, $$1, net.minecraft.world.inventory.ContainerLevelAccess.NULL);
   }

   public LoomMenu(int $$0, Inventory $$1, final net.minecraft.world.inventory.ContainerLevelAccess $$2) {
      super(net.minecraft.world.inventory.MenuType.LOOM, $$0);
      this.access = $$2;
      this.bannerSlot = this.addSlot(new net.minecraft.world.inventory.Slot(this.inputContainer, 0, 13, 26) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return $$0.getItem() instanceof BannerItem;
         }
      });
      this.dyeSlot = this.addSlot(new net.minecraft.world.inventory.Slot(this.inputContainer, 1, 33, 26) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return $$0.getItem() instanceof DyeItem;
         }
      });
      this.patternSlot = this.addSlot(new net.minecraft.world.inventory.Slot(this.inputContainer, 2, 23, 45) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return $$0.has(DataComponents.PROVIDES_BANNER_PATTERNS);
         }
      });
      this.resultSlot = this.addSlot(new net.minecraft.world.inventory.Slot(this.outputContainer, 0, 143, 57) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return false;
         }

         @Override
         public void onTake(Player $$0, ItemStack $$1x) {
            LoomMenu.this.bannerSlot.remove(1);
            LoomMenu.this.dyeSlot.remove(1);
            if (!LoomMenu.this.bannerSlot.hasItem() || !LoomMenu.this.dyeSlot.hasItem()) {
               LoomMenu.this.selectedBannerPatternIndex.set(-1);
            }

            $$2.execute(($$0x, $$1xx) -> {
               long $$2xx = $$0x.getGameTime();
               if (LoomMenu.this.lastSoundTime != $$2xx) {
                  $$0x.playSound(null, $$1xx, SoundEvents.UI_LOOM_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  LoomMenu.this.lastSoundTime = $$2xx;
               }
            });
            super.onTake($$0, $$1x);
         }
      });
      this.addStandardInventorySlots($$1, 8, 84);
      this.addDataSlot(this.selectedBannerPatternIndex);
      this.patternGetter = $$1.player.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
   }

   @Override
   public boolean stillValid(Player $$0) {
      return stillValid(this.access, $$0, Blocks.LOOM);
   }

   @Override
   public boolean clickMenuButton(Player $$0, int $$1) {
      if ($$1 >= 0 && $$1 < this.selectablePatterns.size()) {
         this.selectedBannerPatternIndex.set($$1);
         this.setupResultSlot(this.selectablePatterns.get($$1));
         return true;
      } else {
         return false;
      }
   }

   private List<Holder<BannerPattern>> getSelectablePatterns(ItemStack $$0) {
      if ($$0.isEmpty()) {
         return this.patternGetter.get(BannerPatternTags.NO_ITEM_REQUIRED).<List<Holder<BannerPattern>>>map(ImmutableList::copyOf).orElse(ImmutableList.of());
      } else {
         TagKey<BannerPattern> $$1 = (TagKey<BannerPattern>)$$0.get(DataComponents.PROVIDES_BANNER_PATTERNS);
         return $$1 != null ? this.patternGetter.get($$1).<List<Holder<BannerPattern>>>map(ImmutableList::copyOf).orElse(ImmutableList.of()) : List.of();
      }
   }

   private boolean isValidPatternIndex(int $$0) {
      return $$0 >= 0 && $$0 < this.selectablePatterns.size();
   }

   @Override
   public void slotsChanged(Container $$0) {
      ItemStack $$1 = this.bannerSlot.getItem();
      ItemStack $$2 = this.dyeSlot.getItem();
      ItemStack $$3 = this.patternSlot.getItem();
      if (!$$1.isEmpty() && !$$2.isEmpty()) {
         int $$4 = this.selectedBannerPatternIndex.get();
         boolean $$5 = this.isValidPatternIndex($$4);
         List<Holder<BannerPattern>> $$6 = this.selectablePatterns;
         this.selectablePatterns = this.getSelectablePatterns($$3);
         Holder<BannerPattern> $$7;
         if (this.selectablePatterns.size() == 1) {
            this.selectedBannerPatternIndex.set(0);
            $$7 = this.selectablePatterns.get(0);
         } else if (!$$5) {
            this.selectedBannerPatternIndex.set(-1);
            $$7 = null;
         } else {
            Holder<BannerPattern> $$9 = $$6.get($$4);
            int $$10 = this.selectablePatterns.indexOf($$9);
            if ($$10 != -1) {
               $$7 = $$9;
               this.selectedBannerPatternIndex.set($$10);
            } else {
               $$7 = null;
               this.selectedBannerPatternIndex.set(-1);
            }
         }

         if ($$7 != null) {
            BannerPatternLayers $$13 = (BannerPatternLayers)$$1.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
            boolean $$14 = $$13.layers().size() >= 6;
            if ($$14) {
               this.selectedBannerPatternIndex.set(-1);
               this.resultSlot.set(ItemStack.EMPTY);
            } else {
               this.setupResultSlot($$7);
            }
         } else {
            this.resultSlot.set(ItemStack.EMPTY);
         }

         this.broadcastChanges();
      } else {
         this.resultSlot.set(ItemStack.EMPTY);
         this.selectablePatterns = List.of();
         this.selectedBannerPatternIndex.set(-1);
      }
   }

   public List<Holder<BannerPattern>> getSelectablePatterns() {
      return this.selectablePatterns;
   }

   public int getSelectedBannerPatternIndex() {
      return this.selectedBannerPatternIndex.get();
   }

   public void registerUpdateListener(Runnable $$0) {
      this.slotUpdateListener = $$0;
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
      if ($$3 != null && $$3.hasItem()) {
         ItemStack $$4 = $$3.getItem();
         $$2 = $$4.copy();
         if ($$1 == this.resultSlot.index) {
            if (!this.moveItemStackTo($$4, 4, 40, true)) {
               return ItemStack.EMPTY;
            }

            $$3.onQuickCraft($$4, $$2);
         } else if ($$1 != this.dyeSlot.index && $$1 != this.bannerSlot.index && $$1 != this.patternSlot.index) {
            if ($$4.getItem() instanceof BannerItem) {
               if (!this.moveItemStackTo($$4, this.bannerSlot.index, this.bannerSlot.index + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if ($$4.getItem() instanceof DyeItem) {
               if (!this.moveItemStackTo($$4, this.dyeSlot.index, this.dyeSlot.index + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if ($$4.has(DataComponents.PROVIDES_BANNER_PATTERNS)) {
               if (!this.moveItemStackTo($$4, this.patternSlot.index, this.patternSlot.index + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if ($$1 >= 4 && $$1 < 31) {
               if (!this.moveItemStackTo($$4, 31, 40, false)) {
                  return ItemStack.EMPTY;
               }
            } else if ($$1 >= 31 && $$1 < 40 && !this.moveItemStackTo($$4, 4, 31, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo($$4, 4, 40, false)) {
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

   @Override
   public void removed(Player $$0) {
      super.removed($$0);
      this.access.execute(($$1, $$2) -> this.clearContainer($$0, this.inputContainer));
   }

   private void setupResultSlot(Holder<BannerPattern> $$0) {
      ItemStack $$1 = this.bannerSlot.getItem();
      ItemStack $$2 = this.dyeSlot.getItem();
      ItemStack $$3 = ItemStack.EMPTY;
      if (!$$1.isEmpty() && !$$2.isEmpty()) {
         $$3 = $$1.copyWithCount(1);
         DyeColor $$4 = ((DyeItem)$$2.getItem()).getDyeColor();
         $$3.update(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY, $$2x -> new Builder().addAll($$2x).add($$0, $$4).build());
      }

      if (!ItemStack.matches($$3, this.resultSlot.getItem())) {
         this.resultSlot.set($$3);
      }
   }

   public net.minecraft.world.inventory.Slot getBannerSlot() {
      return this.bannerSlot;
   }

   public net.minecraft.world.inventory.Slot getDyeSlot() {
      return this.dyeSlot;
   }

   public net.minecraft.world.inventory.Slot getPatternSlot() {
      return this.patternSlot;
   }

   public net.minecraft.world.inventory.Slot getResultSlot() {
      return this.resultSlot;
   }
}
