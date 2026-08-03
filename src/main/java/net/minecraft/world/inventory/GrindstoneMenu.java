package net.minecraft.world.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class GrindstoneMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
   public static final int MAX_NAME_LENGTH = 35;
   public static final int INPUT_SLOT = 0;
   public static final int ADDITIONAL_SLOT = 1;
   public static final int RESULT_SLOT = 2;
   private static final int INV_SLOT_START = 3;
   private static final int INV_SLOT_END = 30;
   private static final int USE_ROW_SLOT_START = 30;
   private static final int USE_ROW_SLOT_END = 39;
   private final Container resultSlots = new net.minecraft.world.inventory.ResultContainer();
   final Container repairSlots = new SimpleContainer(2) {
      public void setChanged() {
         super.setChanged();
         GrindstoneMenu.this.slotsChanged(this);
      }
   };
   private final net.minecraft.world.inventory.ContainerLevelAccess access;

   public GrindstoneMenu(int $$0, Inventory $$1) {
      this($$0, $$1, net.minecraft.world.inventory.ContainerLevelAccess.NULL);
   }

   public GrindstoneMenu(int $$0, Inventory $$1, final net.minecraft.world.inventory.ContainerLevelAccess $$2) {
      super(net.minecraft.world.inventory.MenuType.GRINDSTONE, $$0);
      this.access = $$2;
      this.addSlot(new net.minecraft.world.inventory.Slot(this.repairSlots, 0, 49, 19) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return $$0.isDamageableItem() || EnchantmentHelper.hasAnyEnchantments($$0);
         }
      });
      this.addSlot(new net.minecraft.world.inventory.Slot(this.repairSlots, 1, 49, 40) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return $$0.isDamageableItem() || EnchantmentHelper.hasAnyEnchantments($$0);
         }
      });
      this.addSlot(new net.minecraft.world.inventory.Slot(this.resultSlots, 2, 129, 34) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return false;
         }

         @Override
         public void onTake(Player $$0, ItemStack $$1x) {
            $$2.execute(($$0x, $$1xx) -> {
               if ($$0x instanceof ServerLevel) {
                  ExperienceOrb.award((ServerLevel)$$0x, Vec3.atCenterOf($$1xx), this.getExperienceAmount($$0x));
               }

               $$0x.levelEvent(1042, $$1xx, 0);
            });
            GrindstoneMenu.this.repairSlots.setItem(0, ItemStack.EMPTY);
            GrindstoneMenu.this.repairSlots.setItem(1, ItemStack.EMPTY);
         }

         private int getExperienceAmount(Level $$0) {
            int $$1x = 0;
            $$1x += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(0));
            $$1x += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(1));
            if ($$1x > 0) {
               int $$2x = (int)Math.ceil($$1x / 2.0);
               return $$2x + $$0.random.nextInt($$2x);
            } else {
               return 0;
            }
         }

         private int getExperienceFromItem(ItemStack $$0) {
            int $$1x = 0;
            ItemEnchantments $$2x = EnchantmentHelper.getEnchantmentsForCrafting($$0);

            for (Entry<Holder<Enchantment>> $$3 : $$2x.entrySet()) {
               Holder<Enchantment> $$4 = (Holder<Enchantment>)$$3.getKey();
               int $$5 = $$3.getIntValue();
               if (!$$4.is(EnchantmentTags.CURSE)) {
                  $$1x += ((Enchantment)$$4.value()).getMinCost($$5);
               }
            }

            return $$1x;
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
      boolean $$2 = !$$0.isEmpty() || !$$1.isEmpty();
      if (!$$2) {
         return ItemStack.EMPTY;
      } else if ($$0.getCount() <= 1 && $$1.getCount() <= 1) {
         boolean $$3 = !$$0.isEmpty() && !$$1.isEmpty();
         if (!$$3) {
            ItemStack $$4 = !$$0.isEmpty() ? $$0 : $$1;
            return !EnchantmentHelper.hasAnyEnchantments($$4) ? ItemStack.EMPTY : this.removeNonCursesFrom($$4.copy());
         } else {
            return this.mergeItems($$0, $$1);
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   private ItemStack mergeItems(ItemStack $$0, ItemStack $$1) {
      if (!$$0.is($$1.getItem())) {
         return ItemStack.EMPTY;
      } else {
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

         ItemStack $$7 = $$0.copyWithCount($$6);
         if ($$7.isDamageableItem()) {
            $$7.set(DataComponents.MAX_DAMAGE, $$2);
            $$7.setDamageValue(Math.max($$2 - $$5, 0));
         }

         this.mergeEnchantsFrom($$7, $$1);
         return this.removeNonCursesFrom($$7);
      }
   }

   private void mergeEnchantsFrom(ItemStack $$0, ItemStack $$1) {
      EnchantmentHelper.updateEnchantments($$0, $$1x -> {
         ItemEnchantments $$2 = EnchantmentHelper.getEnchantmentsForCrafting($$1);

         for (Entry<Holder<Enchantment>> $$3 : $$2.entrySet()) {
            Holder<Enchantment> $$4 = (Holder<Enchantment>)$$3.getKey();
            if (!$$4.is(EnchantmentTags.CURSE) || $$1x.getLevel($$4) == 0) {
               $$1x.upgrade($$4, $$3.getIntValue());
            }
         }
      });
   }

   private ItemStack removeNonCursesFrom(ItemStack $$0) {
      ItemEnchantments $$1 = EnchantmentHelper.updateEnchantments($$0, $$0x -> $$0x.removeIf($$0xx -> !$$0xx.is(EnchantmentTags.CURSE)));
      if ($$0.is(Items.ENCHANTED_BOOK) && $$1.isEmpty()) {
         $$0 = $$0.transmuteCopy(Items.BOOK);
      }

      int $$2 = 0;

      for (int $$3 = 0; $$3 < $$1.size(); $$3++) {
         $$2 = net.minecraft.world.inventory.AnvilMenu.calculateIncreasedRepairCost($$2);
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
      return stillValid(this.access, $$0, Blocks.GRINDSTONE);
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
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
         } else if ($$1 != 0 && $$1 != 1) {
            if (!$$5.isEmpty() && !$$6.isEmpty()) {
               if ($$1 >= 3 && $$1 < 30) {
                  if (!this.moveItemStackTo($$4, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if ($$1 >= 30 && $$1 < 39 && !this.moveItemStackTo($$4, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo($$4, 0, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo($$4, 3, 39, false)) {
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
