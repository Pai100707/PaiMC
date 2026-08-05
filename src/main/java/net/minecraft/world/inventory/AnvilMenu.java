package net.minecraft.world.inventory;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class AnvilMenu extends net.minecraft.world.inventory.ItemCombinerMenu {
   public static final int INPUT_SLOT = 0;
   public static final int ADDITIONAL_SLOT = 1;
   public static final int RESULT_SLOT = 2;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean DEBUG_COST = false;
   public static final int MAX_NAME_LENGTH = 50;
   private int repairItemCountCost;
   
   private String itemName;
   private final net.minecraft.world.inventory.DataSlot cost = net.minecraft.world.inventory.DataSlot.standalone();
   private boolean onlyRenaming = false;
   private static final int COST_FAIL = 0;
   private static final int COST_BASE = 1;
   private static final int COST_ADDED_BASE = 1;
   private static final int COST_REPAIR_MATERIAL = 1;
   private static final int COST_REPAIR_SACRIFICE = 2;
   private static final int COST_INCOMPATIBLE_PENALTY = 1;
   private static final int COST_RENAME = 1;
   private static final int INPUT_SLOT_X_PLACEMENT = 27;
   private static final int ADDITIONAL_SLOT_X_PLACEMENT = 76;
   private static final int RESULT_SLOT_X_PLACEMENT = 134;
   private static final int SLOT_Y_PLACEMENT = 47;

   public AnvilMenu(int $$0, Inventory $$1) {
      this($$0, $$1, net.minecraft.world.inventory.ContainerLevelAccess.NULL);
   }

   public AnvilMenu(int $$0, Inventory $$1, net.minecraft.world.inventory.ContainerLevelAccess $$2) {
      super(net.minecraft.world.inventory.MenuType.ANVIL, $$0, $$1, $$2, createInputSlotDefinitions());
      this.addDataSlot(this.cost);
   }

   private static net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
      return net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.create()
         .withSlot(0, 27, 47, $$0 -> true)
         .withSlot(1, 76, 47, $$0 -> true)
         .withResultSlot(2, 134, 47)
         .build();
   }

   @Override
   protected boolean isValidBlock(BlockState $$0) {
      return $$0.is(BlockTags.ANVIL);
   }

   @Override
   protected boolean mayPickup(Player $$0, boolean $$1) {
      return ($$0.hasInfiniteMaterials() || $$0.experienceLevel >= this.cost.get()) && this.cost.get() > 0;
   }

   @Override
   protected void onTake(Player $$0, ItemStack $$1) {
      if (!$$0.hasInfiniteMaterials()) {
         $$0.giveExperienceLevels(-this.cost.get());
      }

      if (this.repairItemCountCost > 0) {
         ItemStack $$2 = this.inputSlots.getItem(1);
         if (!$$2.isEmpty() && $$2.getCount() > this.repairItemCountCost) {
            $$2.shrink(this.repairItemCountCost);
            this.inputSlots.setItem(1, $$2);
         } else {
            this.inputSlots.setItem(1, ItemStack.EMPTY);
         }
      } else if (!this.onlyRenaming) {
         this.inputSlots.setItem(1, ItemStack.EMPTY);
      }

      this.cost.set(0);
      if ($$0 instanceof ServerPlayer $$3 && !StringUtil.isBlank(this.itemName) && !this.inputSlots.getItem(0).getHoverName().getString().equals(this.itemName)
         )
       {
         $$3.getTextFilter().processStreamMessage(this.itemName);
      }

      this.inputSlots.setItem(0, ItemStack.EMPTY);
      this.access.execute(($$1x, $$2) -> {
         BlockState $$3x = $$1x.getBlockState($$2);
         if (!$$0.hasInfiniteMaterials() && $$3x.is(BlockTags.ANVIL) && $$0.getRandom().nextFloat() < 0.12F) {
            BlockState $$4 = AnvilBlock.damage($$3x);
            if ($$4 == null) {
               $$1x.removeBlock($$2, false);
               $$1x.levelEvent(1029, $$2, 0);
            } else {
               $$1x.setBlock($$2, $$4, 2);
               $$1x.levelEvent(1030, $$2, 0);
            }
         } else {
            $$1x.levelEvent(1030, $$2, 0);
         }
      });
   }

   @Override
   public void createResult() {
      ItemStack $$0 = this.inputSlots.getItem(0);
      this.onlyRenaming = false;
      this.cost.set(1);
      int $$1 = 0;
      long $$2 = 0L;
      int $$3 = 0;
      if (!$$0.isEmpty() && EnchantmentHelper.canStoreEnchantments($$0)) {
         ItemStack $$4 = $$0.copy();
         ItemStack $$5 = this.inputSlots.getItem(1);
         Mutable $$6 = new Mutable(EnchantmentHelper.getEnchantmentsForCrafting($$4));
         $$2 += (long)((Integer)$$0.getOrDefault(DataComponents.REPAIR_COST, 0)).intValue()
            + ((Integer)$$5.getOrDefault(DataComponents.REPAIR_COST, 0)).intValue();
         this.repairItemCountCost = 0;
         if (!$$5.isEmpty()) {
            boolean $$7 = $$5.has(DataComponents.STORED_ENCHANTMENTS);
            if ($$4.isDamageableItem() && $$0.isValidRepairItem($$5)) {
               int $$8 = Math.min($$4.getDamageValue(), $$4.getMaxDamage() / 4);
               if ($$8 <= 0) {
                  this.resultSlots.setItem(0, ItemStack.EMPTY);
                  this.cost.set(0);
                  return;
               }

               int $$9;
               for ($$9 = 0; $$8 > 0 && $$9 < $$5.getCount(); $$9++) {
                  int $$10 = $$4.getDamageValue() - $$8;
                  $$4.setDamageValue($$10);
                  $$1++;
                  $$8 = Math.min($$4.getDamageValue(), $$4.getMaxDamage() / 4);
               }

               this.repairItemCountCost = $$9;
            } else {
               if (!$$7 && (!$$4.is($$5.getItem()) || !$$4.isDamageableItem())) {
                  this.resultSlots.setItem(0, ItemStack.EMPTY);
                  this.cost.set(0);
                  return;
               }

               if ($$4.isDamageableItem() && !$$7) {
                  int $$11 = $$0.getMaxDamage() - $$0.getDamageValue();
                  int $$12 = $$5.getMaxDamage() - $$5.getDamageValue();
                  int $$13 = $$12 + $$4.getMaxDamage() * 12 / 100;
                  int $$14 = $$11 + $$13;
                  int $$15 = $$4.getMaxDamage() - $$14;
                  if ($$15 < 0) {
                     $$15 = 0;
                  }

                  if ($$15 < $$4.getDamageValue()) {
                     $$4.setDamageValue($$15);
                     $$1 += 2;
                  }
               }

               ItemEnchantments $$16 = EnchantmentHelper.getEnchantmentsForCrafting($$5);
               boolean $$17 = false;
               boolean $$18 = false;

               for (Entry<Holder<Enchantment>> $$19 : $$16.entrySet()) {
                  Holder<Enchantment> $$20 = (Holder<Enchantment>)$$19.getKey();
                  int $$21 = $$6.getLevel($$20);
                  int $$22 = $$19.getIntValue();
                  $$22 = $$21 == $$22 ? $$22 + 1 : Math.max($$22, $$21);
                  Enchantment $$23 = (Enchantment)$$20.value();
                  boolean $$24 = $$23.canEnchant($$0);
                  if (this.player.hasInfiniteMaterials() || $$0.is(Items.ENCHANTED_BOOK)) {
                     $$24 = true;
                  }

                  for (Holder<Enchantment> $$25 : $$6.keySet()) {
                     if (!$$25.equals($$20) && !Enchantment.areCompatible($$20, $$25)) {
                        $$24 = false;
                        $$1++;
                     }
                  }

                  if (!$$24) {
                     $$18 = true;
                  } else {
                     $$17 = true;
                     if ($$22 > $$23.getMaxLevel()) {
                        $$22 = $$23.getMaxLevel();
                     }

                     $$6.set($$20, $$22);
                     int $$26 = $$23.getAnvilCost();
                     if ($$7) {
                        $$26 = Math.max(1, $$26 / 2);
                     }

                     $$1 += $$26 * $$22;
                     if ($$0.getCount() > 1) {
                        $$1 = 40;
                     }
                  }
               }

               if ($$18 && !$$17) {
                  this.resultSlots.setItem(0, ItemStack.EMPTY);
                  this.cost.set(0);
                  return;
               }
            }
         }

         if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
            if (!this.itemName.equals($$0.getHoverName().getString())) {
               $$3 = 1;
               $$1 += $$3;
               $$4.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
            }
         } else if ($$0.has(DataComponents.CUSTOM_NAME)) {
            $$3 = 1;
            $$1 += $$3;
            $$4.remove(DataComponents.CUSTOM_NAME);
         }

         int $$27 = $$1 <= 0 ? 0 : (int)Mth.clamp($$2 + $$1, 0L, 2147483647L);
         this.cost.set($$27);
         if ($$1 <= 0) {
            $$4 = ItemStack.EMPTY;
         }

         if ($$3 == $$1 && $$3 > 0) {
            if (this.cost.get() >= 40) {
               this.cost.set(39);
            }

            this.onlyRenaming = true;
         }

         if (this.cost.get() >= 40 && !this.player.hasInfiniteMaterials()) {
            $$4 = ItemStack.EMPTY;
         }

         if (!$$4.isEmpty()) {
            int $$28 = (Integer)$$4.getOrDefault(DataComponents.REPAIR_COST, 0);
            if ($$28 < (Integer)$$5.getOrDefault(DataComponents.REPAIR_COST, 0)) {
               $$28 = (Integer)$$5.getOrDefault(DataComponents.REPAIR_COST, 0);
            }

            if ($$3 != $$1 || $$3 == 0) {
               $$28 = calculateIncreasedRepairCost($$28);
            }

            $$4.set(DataComponents.REPAIR_COST, $$28);
            EnchantmentHelper.setEnchantments($$4, $$6.toImmutable());
         }

         this.resultSlots.setItem(0, $$4);
         this.broadcastChanges();
      } else {
         this.resultSlots.setItem(0, ItemStack.EMPTY);
         this.cost.set(0);
      }
   }

   public static int calculateIncreasedRepairCost(int $$0) {
      return (int)Math.min($$0 * 2L + 1L, 2147483647L);
   }

   public boolean setItemName(String $$0) {
      String $$1 = validateName($$0);
      if ($$1 != null && !$$1.equals(this.itemName)) {
         this.itemName = $$1;
         if (this.getSlot(2).hasItem()) {
            ItemStack $$2 = this.getSlot(2).getItem();
            if (StringUtil.isBlank($$1)) {
               $$2.remove(DataComponents.CUSTOM_NAME);
            } else {
               $$2.set(DataComponents.CUSTOM_NAME, Component.literal($$1));
            }
         }

         this.createResult();
         return true;
      } else {
         return false;
      }
   }

   
   private static String validateName(String $$0) {
      String $$1 = StringUtil.filterText($$0);
      return $$1.length() <= 50 ? $$1 : null;
   }

   public int getCost() {
      return this.cost.get();
   }
}
