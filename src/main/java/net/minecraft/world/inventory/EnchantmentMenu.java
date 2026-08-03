package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;

public class EnchantmentMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
   static final Identifier EMPTY_SLOT_LAPIS_LAZULI = Identifier.withDefaultNamespace("container/slot/lapis_lazuli");
   private final Container enchantSlots = new SimpleContainer(2) {
      public void setChanged() {
         super.setChanged();
         EnchantmentMenu.this.slotsChanged(this);
      }
   };
   private final net.minecraft.world.inventory.ContainerLevelAccess access;
   private final RandomSource random = RandomSource.create();
   private final net.minecraft.world.inventory.DataSlot enchantmentSeed = net.minecraft.world.inventory.DataSlot.standalone();
   public final int[] costs = new int[3];
   public final int[] enchantClue = new int[]{-1, -1, -1};
   public final int[] levelClue = new int[]{-1, -1, -1};

   public EnchantmentMenu(int $$0, Inventory $$1) {
      this($$0, $$1, net.minecraft.world.inventory.ContainerLevelAccess.NULL);
   }

   public EnchantmentMenu(int $$0, Inventory $$1, net.minecraft.world.inventory.ContainerLevelAccess $$2) {
      super(net.minecraft.world.inventory.MenuType.ENCHANTMENT, $$0);
      this.access = $$2;
      this.addSlot(new net.minecraft.world.inventory.Slot(this.enchantSlots, 0, 15, 47) {
         @Override
         public int getMaxStackSize() {
            return 1;
         }
      });
      this.addSlot(new net.minecraft.world.inventory.Slot(this.enchantSlots, 1, 35, 47) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return $$0.is(Items.LAPIS_LAZULI);
         }

         @Override
         public Identifier getNoItemIcon() {
            return net.minecraft.world.inventory.EnchantmentMenu.EMPTY_SLOT_LAPIS_LAZULI;
         }
      });
      this.addStandardInventorySlots($$1, 8, 84);
      this.addDataSlot(net.minecraft.world.inventory.DataSlot.shared(this.costs, 0));
      this.addDataSlot(net.minecraft.world.inventory.DataSlot.shared(this.costs, 1));
      this.addDataSlot(net.minecraft.world.inventory.DataSlot.shared(this.costs, 2));
      this.addDataSlot(this.enchantmentSeed).set($$1.player.getEnchantmentSeed());
      this.addDataSlot(net.minecraft.world.inventory.DataSlot.shared(this.enchantClue, 0));
      this.addDataSlot(net.minecraft.world.inventory.DataSlot.shared(this.enchantClue, 1));
      this.addDataSlot(net.minecraft.world.inventory.DataSlot.shared(this.enchantClue, 2));
      this.addDataSlot(net.minecraft.world.inventory.DataSlot.shared(this.levelClue, 0));
      this.addDataSlot(net.minecraft.world.inventory.DataSlot.shared(this.levelClue, 1));
      this.addDataSlot(net.minecraft.world.inventory.DataSlot.shared(this.levelClue, 2));
   }

   @Override
   public void slotsChanged(Container $$0) {
      if ($$0 == this.enchantSlots) {
         ItemStack $$1 = $$0.getItem(0);
         if (!$$1.isEmpty() && $$1.isEnchantable()) {
            this.access.execute(($$1x, $$2x) -> {
               IdMap<Holder<Enchantment>> $$3 = $$1x.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
               int $$4 = 0;

               for (BlockPos $$5 : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
                  if (EnchantingTableBlock.isValidBookShelf($$1x, $$2x, $$5)) {
                     $$4++;
                  }
               }

               this.random.setSeed(this.enchantmentSeed.get());

               for (int $$6 = 0; $$6 < 3; $$6++) {
                  this.costs[$$6] = EnchantmentHelper.getEnchantmentCost(this.random, $$6, $$4, $$1);
                  this.enchantClue[$$6] = -1;
                  this.levelClue[$$6] = -1;
                  if (this.costs[$$6] < $$6 + 1) {
                     this.costs[$$6] = 0;
                  }
               }

               for (int $$7 = 0; $$7 < 3; $$7++) {
                  if (this.costs[$$7] > 0) {
                     List<EnchantmentInstance> $$8 = this.getEnchantmentList($$1x.registryAccess(), $$1, $$7, this.costs[$$7]);
                     if (!$$8.isEmpty()) {
                        EnchantmentInstance $$9 = $$8.get(this.random.nextInt($$8.size()));
                        this.enchantClue[$$7] = $$3.getId($$9.enchantment());
                        this.levelClue[$$7] = $$9.level();
                     }
                  }
               }

               this.broadcastChanges();
            });
         } else {
            for (int $$2 = 0; $$2 < 3; $$2++) {
               this.costs[$$2] = 0;
               this.enchantClue[$$2] = -1;
               this.levelClue[$$2] = -1;
            }
         }
      }
   }

   @Override
   public boolean clickMenuButton(Player $$0, int $$1) {
      if ($$1 >= 0 && $$1 < this.costs.length) {
         ItemStack $$2 = this.enchantSlots.getItem(0);
         ItemStack $$3 = this.enchantSlots.getItem(1);
         int $$4 = $$1 + 1;
         if (($$3.isEmpty() || $$3.getCount() < $$4) && !$$0.hasInfiniteMaterials()) {
            return false;
         } else if (this.costs[$$1] <= 0
            || $$2.isEmpty()
            || ($$0.experienceLevel < $$4 || $$0.experienceLevel < this.costs[$$1]) && !$$0.hasInfiniteMaterials()) {
            return false;
         } else {
            this.access.execute(($$5, $$6) -> {
               ItemStack $$7 = $$2;
               List<EnchantmentInstance> $$8 = this.getEnchantmentList($$5.registryAccess(), $$2, $$1, this.costs[$$1]);
               if (!$$8.isEmpty()) {
                  $$0.onEnchantmentPerformed($$2, $$4);
                  if ($$2.is(Items.BOOK)) {
                     $$7 = $$2.transmuteCopy(Items.ENCHANTED_BOOK);
                     this.enchantSlots.setItem(0, $$7);
                  }

                  for (EnchantmentInstance $$9 : $$8) {
                     $$7.enchant($$9.enchantment(), $$9.level());
                  }

                  $$3.consume($$4, $$0);
                  if ($$3.isEmpty()) {
                     this.enchantSlots.setItem(1, ItemStack.EMPTY);
                  }

                  $$0.awardStat(Stats.ENCHANT_ITEM);
                  if ($$0 instanceof ServerPlayer) {
                     CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)$$0, $$7, $$4);
                  }

                  this.enchantSlots.setChanged();
                  this.enchantmentSeed.set($$0.getEnchantmentSeed());
                  this.slotsChanged(this.enchantSlots);
                  $$5.playSound(null, $$6, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, $$5.random.nextFloat() * 0.1F + 0.9F);
               }
            });
            return true;
         }
      } else {
         Util.logAndPauseIfInIde($$0.getPlainTextName() + " pressed invalid button id: " + $$1);
         return false;
      }
   }

   private List<EnchantmentInstance> getEnchantmentList(RegistryAccess $$0, ItemStack $$1, int $$2, int $$3) {
      this.random.setSeed(this.enchantmentSeed.get() + $$2);
      Optional<Named<Enchantment>> $$4 = $$0.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);
      if ($$4.isEmpty()) {
         return List.of();
      } else {
         List<EnchantmentInstance> $$5 = EnchantmentHelper.selectEnchantment(this.random, $$1, $$3, $$4.get().stream());
         if ($$1.is(Items.BOOK) && $$5.size() > 1) {
            $$5.remove(this.random.nextInt($$5.size()));
         }

         return $$5;
      }
   }

   public int getGoldCount() {
      ItemStack $$0 = this.enchantSlots.getItem(1);
      return $$0.isEmpty() ? 0 : $$0.getCount();
   }

   public int getEnchantmentSeed() {
      return this.enchantmentSeed.get();
   }

   @Override
   public void removed(Player $$0) {
      super.removed($$0);
      this.access.execute(($$1, $$2) -> this.clearContainer($$0, this.enchantSlots));
   }

   @Override
   public boolean stillValid(Player $$0) {
      return stillValid(this.access, $$0, Blocks.ENCHANTING_TABLE);
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
      if ($$3 != null && $$3.hasItem()) {
         ItemStack $$4 = $$3.getItem();
         $$2 = $$4.copy();
         if ($$1 == 0) {
            if (!this.moveItemStackTo($$4, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if ($$1 == 1) {
            if (!this.moveItemStackTo($$4, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if ($$4.is(Items.LAPIS_LAZULI)) {
            if (!this.moveItemStackTo($$4, 1, 2, true)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (((net.minecraft.world.inventory.Slot)this.slots.get(0)).hasItem() || !((net.minecraft.world.inventory.Slot)this.slots.get(0)).mayPlace($$4)) {
               return ItemStack.EMPTY;
            }

            ItemStack $$5 = $$4.copyWithCount(1);
            $$4.shrink(1);
            ((net.minecraft.world.inventory.Slot)this.slots.get(0)).setByPlayer($$5);
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
