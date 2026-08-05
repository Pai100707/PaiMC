package net.minecraft.world.inventory;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.HashedStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

public abstract class AbstractContainerMenu {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int SLOT_CLICKED_OUTSIDE = -999;
   public static final int QUICKCRAFT_TYPE_CHARITABLE = 0;
   public static final int QUICKCRAFT_TYPE_GREEDY = 1;
   public static final int QUICKCRAFT_TYPE_CLONE = 2;
   public static final int QUICKCRAFT_HEADER_START = 0;
   public static final int QUICKCRAFT_HEADER_CONTINUE = 1;
   public static final int QUICKCRAFT_HEADER_END = 2;
   public static final int CARRIED_SLOT_SIZE = Integer.MAX_VALUE;
   public static final int SLOTS_PER_ROW = 9;
   public static final int SLOT_SIZE = 18;
   private final NonNullList<ItemStack> lastSlots = NonNullList.create();
   public final NonNullList<net.minecraft.world.inventory.Slot> slots = NonNullList.create();
   private final List<net.minecraft.world.inventory.DataSlot> dataSlots = Lists.newArrayList();
   private ItemStack carried = ItemStack.EMPTY;
   private final NonNullList<net.minecraft.world.inventory.RemoteSlot> remoteSlots = NonNullList.create();
   private final IntList remoteDataSlots = new IntArrayList();
   private net.minecraft.world.inventory.RemoteSlot remoteCarried = net.minecraft.world.inventory.RemoteSlot.PLACEHOLDER;
   private int stateId;
   
   private final net.minecraft.world.inventory.MenuType<?> menuType;
   public final int containerId;
   private int quickcraftType = -1;
   private int quickcraftStatus;
   private final Set<net.minecraft.world.inventory.Slot> quickcraftSlots = Sets.newHashSet();
   private final List<net.minecraft.world.inventory.ContainerListener> containerListeners = Lists.newArrayList();
   
   private net.minecraft.world.inventory.ContainerSynchronizer synchronizer;
   private boolean suppressRemoteUpdates;

   protected AbstractContainerMenu(net.minecraft.world.inventory.MenuType<?> $$0, int $$1) {
      this.menuType = $$0;
      this.containerId = $$1;
   }

   protected void addInventoryHotbarSlots(Container $$0, int $$1, int $$2) {
      for (int $$3 = 0; $$3 < 9; $$3++) {
         this.addSlot(new net.minecraft.world.inventory.Slot($$0, $$3, $$1 + $$3 * 18, $$2));
      }
   }

   protected void addInventoryExtendedSlots(Container $$0, int $$1, int $$2) {
      for (int $$3 = 0; $$3 < 3; $$3++) {
         for (int $$4 = 0; $$4 < 9; $$4++) {
            this.addSlot(new net.minecraft.world.inventory.Slot($$0, $$4 + ($$3 + 1) * 9, $$1 + $$4 * 18, $$2 + $$3 * 18));
         }
      }
   }

   protected void addStandardInventorySlots(Container $$0, int $$1, int $$2) {
      this.addInventoryExtendedSlots($$0, $$1, $$2);
      int $$3 = 4;
      int $$4 = 58;
      this.addInventoryHotbarSlots($$0, $$1, $$2 + 58);
   }

   protected static boolean stillValid(net.minecraft.world.inventory.ContainerLevelAccess $$0, Player $$1, Block $$2) {
      return $$0.evaluate(($$2x, $$3) -> !$$2x.getBlockState($$3).is($$2) ? false : $$1.isWithinBlockInteractionRange($$3, 4.0), true);
   }

   public net.minecraft.world.inventory.MenuType<?> getType() {
      if (this.menuType == null) {
         throw new UnsupportedOperationException("Unable to construct this menu by type");
      } else {
         return this.menuType;
      }
   }

   protected static void checkContainerSize(Container $$0, int $$1) {
      int $$2 = $$0.getContainerSize();
      if ($$2 < $$1) {
         throw new IllegalArgumentException("Container size " + $$2 + " is smaller than expected " + $$1);
      }
   }

   protected static void checkContainerDataCount(net.minecraft.world.inventory.ContainerData $$0, int $$1) {
      int $$2 = $$0.getCount();
      if ($$2 < $$1) {
         throw new IllegalArgumentException("Container data count " + $$2 + " is smaller than expected " + $$1);
      }
   }

   public boolean isValidSlotIndex(int $$0) {
      return $$0 == -1 || $$0 == -999 || $$0 < this.slots.size();
   }

   protected net.minecraft.world.inventory.Slot addSlot(net.minecraft.world.inventory.Slot $$0) {
      $$0.index = this.slots.size();
      this.slots.add($$0);
      this.lastSlots.add(ItemStack.EMPTY);
      this.remoteSlots.add(this.synchronizer != null ? this.synchronizer.createSlot() : net.minecraft.world.inventory.RemoteSlot.PLACEHOLDER);
      return $$0;
   }

   protected net.minecraft.world.inventory.DataSlot addDataSlot(net.minecraft.world.inventory.DataSlot $$0) {
      this.dataSlots.add($$0);
      this.remoteDataSlots.add(0);
      return $$0;
   }

   protected void addDataSlots(net.minecraft.world.inventory.ContainerData $$0) {
      for (int $$1 = 0; $$1 < $$0.getCount(); $$1++) {
         this.addDataSlot(net.minecraft.world.inventory.DataSlot.forContainer($$0, $$1));
      }
   }

   public void addSlotListener(net.minecraft.world.inventory.ContainerListener $$0) {
      if (!this.containerListeners.contains($$0)) {
         this.containerListeners.add($$0);
         this.broadcastChanges();
      }
   }

   public void setSynchronizer(net.minecraft.world.inventory.ContainerSynchronizer $$0) {
      this.synchronizer = $$0;
      this.remoteCarried = $$0.createSlot();
      this.remoteSlots.replaceAll($$1 -> $$0.createSlot());
      this.sendAllDataToRemote();
   }

   public void sendAllDataToRemote() {
      List<ItemStack> $$0 = new ArrayList<>(this.slots.size());
      int $$1 = 0;

      for (int $$2 = this.slots.size(); $$1 < $$2; $$1++) {
         ItemStack $$3 = ((net.minecraft.world.inventory.Slot)this.slots.get($$1)).getItem();
         $$0.add($$3.copy());
         ((net.minecraft.world.inventory.RemoteSlot)this.remoteSlots.get($$1)).force($$3);
      }

      ItemStack $$4 = this.getCarried();
      this.remoteCarried.force($$4);
      int $$5 = 0;

      for (int $$6 = this.dataSlots.size(); $$5 < $$6; $$5++) {
         this.remoteDataSlots.set($$5, this.dataSlots.get($$5).get());
      }

      if (this.synchronizer != null) {
         this.synchronizer.sendInitialData(this, $$0, $$4.copy(), this.remoteDataSlots.toIntArray());
      }
   }

   public void removeSlotListener(net.minecraft.world.inventory.ContainerListener $$0) {
      this.containerListeners.remove($$0);
   }

   public NonNullList<ItemStack> getItems() {
      NonNullList<ItemStack> $$0 = NonNullList.create();

      for (net.minecraft.world.inventory.Slot $$1 : this.slots) {
         $$0.add($$1.getItem());
      }

      return $$0;
   }

   public void broadcastChanges() {
      for (int $$0 = 0; $$0 < this.slots.size(); $$0++) {
         ItemStack $$1 = ((net.minecraft.world.inventory.Slot)this.slots.get($$0)).getItem();
         Supplier<ItemStack> $$2 = Suppliers.memoize($$1::copy);
         this.triggerSlotListeners($$0, $$1, $$2);
         this.synchronizeSlotToRemote($$0, $$1, $$2);
      }

      this.synchronizeCarriedToRemote();

      for (int $$3 = 0; $$3 < this.dataSlots.size(); $$3++) {
         net.minecraft.world.inventory.DataSlot $$4 = this.dataSlots.get($$3);
         int $$5 = $$4.get();
         if ($$4.checkAndClearUpdateFlag()) {
            this.updateDataSlotListeners($$3, $$5);
         }

         this.synchronizeDataSlotToRemote($$3, $$5);
      }
   }

   public void broadcastFullState() {
      for (int $$0 = 0; $$0 < this.slots.size(); $$0++) {
         ItemStack $$1 = ((net.minecraft.world.inventory.Slot)this.slots.get($$0)).getItem();
         this.triggerSlotListeners($$0, $$1, $$1::copy);
      }

      for (int $$2 = 0; $$2 < this.dataSlots.size(); $$2++) {
         net.minecraft.world.inventory.DataSlot $$3 = this.dataSlots.get($$2);
         if ($$3.checkAndClearUpdateFlag()) {
            this.updateDataSlotListeners($$2, $$3.get());
         }
      }

      this.sendAllDataToRemote();
   }

   private void updateDataSlotListeners(int $$0, int $$1) {
      for (net.minecraft.world.inventory.ContainerListener $$2 : this.containerListeners) {
         $$2.dataChanged(this, $$0, $$1);
      }
   }

   private void triggerSlotListeners(int $$0, ItemStack $$1, Supplier<ItemStack> $$2) {
      ItemStack $$3 = (ItemStack)this.lastSlots.get($$0);
      if (!ItemStack.matches($$3, $$1)) {
         ItemStack $$4 = $$2.get();
         this.lastSlots.set($$0, $$4);

         for (net.minecraft.world.inventory.ContainerListener $$5 : this.containerListeners) {
            $$5.slotChanged(this, $$0, $$4);
         }
      }
   }

   private void synchronizeSlotToRemote(int $$0, ItemStack $$1, Supplier<ItemStack> $$2) {
      if (!this.suppressRemoteUpdates) {
         net.minecraft.world.inventory.RemoteSlot $$3 = (net.minecraft.world.inventory.RemoteSlot)this.remoteSlots.get($$0);
         if (!$$3.matches($$1)) {
            $$3.force($$1);
            if (this.synchronizer != null) {
               this.synchronizer.sendSlotChange(this, $$0, $$2.get());
            }
         }
      }
   }

   private void synchronizeDataSlotToRemote(int $$0, int $$1) {
      if (!this.suppressRemoteUpdates) {
         int $$2 = this.remoteDataSlots.getInt($$0);
         if ($$2 != $$1) {
            this.remoteDataSlots.set($$0, $$1);
            if (this.synchronizer != null) {
               this.synchronizer.sendDataChange(this, $$0, $$1);
            }
         }
      }
   }

   private void synchronizeCarriedToRemote() {
      if (!this.suppressRemoteUpdates) {
         ItemStack $$0 = this.getCarried();
         if (!this.remoteCarried.matches($$0)) {
            this.remoteCarried.force($$0);
            if (this.synchronizer != null) {
               this.synchronizer.sendCarriedChange(this, $$0.copy());
            }
         }
      }
   }

   public void setRemoteSlot(int $$0, ItemStack $$1) {
      ((net.minecraft.world.inventory.RemoteSlot)this.remoteSlots.get($$0)).force($$1);
   }

   public void setRemoteSlotUnsafe(int $$0, HashedStack $$1) {
      if ($$0 >= 0 && $$0 < this.remoteSlots.size()) {
         ((net.minecraft.world.inventory.RemoteSlot)this.remoteSlots.get($$0)).receive($$1);
      } else {
         LOGGER.debug("Incorrect slot index: {} available slots: {}", $$0, this.remoteSlots.size());
      }
   }

   public void setRemoteCarried(HashedStack $$0) {
      this.remoteCarried.receive($$0);
   }

   public boolean clickMenuButton(Player $$0, int $$1) {
      return false;
   }

   public net.minecraft.world.inventory.Slot getSlot(int $$0) {
      return (net.minecraft.world.inventory.Slot)this.slots.get($$0);
   }

   public abstract ItemStack quickMoveStack(Player var1, int var2);

   public void setSelectedBundleItemIndex(int $$0, int $$1) {
      if ($$0 >= 0 && $$0 < this.slots.size()) {
         ItemStack $$2 = ((net.minecraft.world.inventory.Slot)this.slots.get($$0)).getItem();
         BundleItem.toggleSelectedItem($$2, $$1);
      }
   }

   public void clicked(int $$0, int $$1, net.minecraft.world.inventory.ClickType $$2, Player $$3) {
      try {
         this.doClick($$0, $$1, $$2, $$3);
      } catch (Exception var8) {
         CrashReport $$5 = CrashReport.forThrowable(var8, "Container click");
         CrashReportCategory $$6 = $$5.addCategory("Click info");
         $$6.setDetail("Menu Type", () -> this.menuType != null ? BuiltInRegistries.MENU.getKey(this.menuType).toString() : "<no type>");
         $$6.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
         $$6.setDetail("Slot Count", this.slots.size());
         $$6.setDetail("Slot", $$0);
         $$6.setDetail("Button", $$1);
         $$6.setDetail("Type", $$2);
         throw new ReportedException($$5);
      }
   }

   private void doClick(int $$0, int $$1, net.minecraft.world.inventory.ClickType $$2, Player $$3) {
      Inventory $$4 = $$3.getInventory();
      if ($$2 == net.minecraft.world.inventory.ClickType.QUICK_CRAFT) {
         int $$5 = this.quickcraftStatus;
         this.quickcraftStatus = getQuickcraftHeader($$1);
         if (($$5 != 1 || this.quickcraftStatus != 2) && $$5 != this.quickcraftStatus) {
            this.resetQuickCraft();
         } else if (this.getCarried().isEmpty()) {
            this.resetQuickCraft();
         } else if (this.quickcraftStatus == 0) {
            this.quickcraftType = getQuickcraftType($$1);
            if (isValidQuickcraftType(this.quickcraftType, $$3)) {
               this.quickcraftStatus = 1;
               this.quickcraftSlots.clear();
            } else {
               this.resetQuickCraft();
            }
         } else if (this.quickcraftStatus == 1) {
            net.minecraft.world.inventory.Slot $$6 = (net.minecraft.world.inventory.Slot)this.slots.get($$0);
            ItemStack $$7 = this.getCarried();
            if (canItemQuickReplace($$6, $$7, true)
               && $$6.mayPlace($$7)
               && (this.quickcraftType == 2 || $$7.getCount() > this.quickcraftSlots.size())
               && this.canDragTo($$6)) {
               this.quickcraftSlots.add($$6);
            }
         } else if (this.quickcraftStatus == 2) {
            if (!this.quickcraftSlots.isEmpty()) {
               if (this.quickcraftSlots.size() == 1) {
                  int $$8 = this.quickcraftSlots.iterator().next().index;
                  this.resetQuickCraft();
                  this.doClick($$8, this.quickcraftType, net.minecraft.world.inventory.ClickType.PICKUP, $$3);
                  return;
               }

               ItemStack $$9 = this.getCarried().copy();
               if ($$9.isEmpty()) {
                  this.resetQuickCraft();
                  return;
               }

               int $$10 = this.getCarried().getCount();

               for (net.minecraft.world.inventory.Slot $$11 : this.quickcraftSlots) {
                  ItemStack $$12 = this.getCarried();
                  if ($$11 != null
                     && canItemQuickReplace($$11, $$12, true)
                     && $$11.mayPlace($$12)
                     && (this.quickcraftType == 2 || $$12.getCount() >= this.quickcraftSlots.size())
                     && this.canDragTo($$11)) {
                     int $$13 = $$11.hasItem() ? $$11.getItem().getCount() : 0;
                     int $$14 = Math.min($$9.getMaxStackSize(), $$11.getMaxStackSize($$9));
                     int $$15 = Math.min(getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, $$9) + $$13, $$14);
                     $$10 -= $$15 - $$13;
                     $$11.setByPlayer($$9.copyWithCount($$15));
                  }
               }

               $$9.setCount($$10);
               this.setCarried($$9);
            }

            this.resetQuickCraft();
         } else {
            this.resetQuickCraft();
         }
      } else if (this.quickcraftStatus != 0) {
         this.resetQuickCraft();
      } else if (($$2 == net.minecraft.world.inventory.ClickType.PICKUP || $$2 == net.minecraft.world.inventory.ClickType.QUICK_MOVE) && ($$1 == 0 || $$1 == 1)
         )
       {
         net.minecraft.world.inventory.ClickAction $$16 = $$1 == 0
            ? net.minecraft.world.inventory.ClickAction.PRIMARY
            : net.minecraft.world.inventory.ClickAction.SECONDARY;
         if ($$0 == -999) {
            if (!this.getCarried().isEmpty()) {
               if ($$16 == net.minecraft.world.inventory.ClickAction.PRIMARY) {
                  $$3.drop(this.getCarried(), true);
                  this.setCarried(ItemStack.EMPTY);
               } else {
                  $$3.drop(this.getCarried().split(1), true);
               }
            }
         } else if ($$2 == net.minecraft.world.inventory.ClickType.QUICK_MOVE) {
            if ($$0 < 0) {
               return;
            }

            net.minecraft.world.inventory.Slot $$17 = (net.minecraft.world.inventory.Slot)this.slots.get($$0);
            if (!$$17.mayPickup($$3)) {
               return;
            }

            ItemStack $$18 = this.quickMoveStack($$3, $$0);

            while (!$$18.isEmpty() && ItemStack.isSameItem($$17.getItem(), $$18)) {
               $$18 = this.quickMoveStack($$3, $$0);
            }
         } else {
            if ($$0 < 0) {
               return;
            }

            net.minecraft.world.inventory.Slot $$19 = (net.minecraft.world.inventory.Slot)this.slots.get($$0);
            ItemStack $$20 = $$19.getItem();
            ItemStack $$21 = this.getCarried();
            $$3.updateTutorialInventoryAction($$21, $$19.getItem(), $$16);
            if (!this.tryItemClickBehaviourOverride($$3, $$16, $$19, $$20, $$21)) {
               if ($$20.isEmpty()) {
                  if (!$$21.isEmpty()) {
                     int $$22 = $$16 == net.minecraft.world.inventory.ClickAction.PRIMARY ? $$21.getCount() : 1;
                     this.setCarried($$19.safeInsert($$21, $$22));
                  }
               } else if ($$19.mayPickup($$3)) {
                  if ($$21.isEmpty()) {
                     int $$23 = $$16 == net.minecraft.world.inventory.ClickAction.PRIMARY ? $$20.getCount() : ($$20.getCount() + 1) / 2;
                     Optional<ItemStack> $$24 = $$19.tryRemove($$23, Integer.MAX_VALUE, $$3);
                     $$24.ifPresent($$2x -> {
                        this.setCarried($$2x);
                        $$19.onTake($$3, $$2x);
                     });
                  } else if ($$19.mayPlace($$21)) {
                     if (ItemStack.isSameItemSameComponents($$20, $$21)) {
                        int $$25 = $$16 == net.minecraft.world.inventory.ClickAction.PRIMARY ? $$21.getCount() : 1;
                        this.setCarried($$19.safeInsert($$21, $$25));
                     } else if ($$21.getCount() <= $$19.getMaxStackSize($$21)) {
                        this.setCarried($$20);
                        $$19.setByPlayer($$21);
                     }
                  } else if (ItemStack.isSameItemSameComponents($$20, $$21)) {
                     Optional<ItemStack> $$26 = $$19.tryRemove($$20.getCount(), $$21.getMaxStackSize() - $$21.getCount(), $$3);
                     $$26.ifPresent($$3x -> {
                        $$21.grow($$3x.getCount());
                        $$19.onTake($$3, $$3x);
                     });
                  }
               }
            }

            $$19.setChanged();
         }
      } else if ($$2 == net.minecraft.world.inventory.ClickType.SWAP && ($$1 >= 0 && $$1 < 9 || $$1 == 40)) {
         ItemStack $$27 = $$4.getItem($$1);
         net.minecraft.world.inventory.Slot $$28 = (net.minecraft.world.inventory.Slot)this.slots.get($$0);
         ItemStack $$29 = $$28.getItem();
         if (!$$27.isEmpty() || !$$29.isEmpty()) {
            if ($$27.isEmpty()) {
               if ($$28.mayPickup($$3)) {
                  $$4.setItem($$1, $$29);
                  $$28.onSwapCraft($$29.getCount());
                  $$28.setByPlayer(ItemStack.EMPTY);
                  $$28.onTake($$3, $$29);
               }
            } else if ($$29.isEmpty()) {
               if ($$28.mayPlace($$27)) {
                  int $$30 = $$28.getMaxStackSize($$27);
                  if ($$27.getCount() > $$30) {
                     $$28.setByPlayer($$27.split($$30));
                  } else {
                     $$4.setItem($$1, ItemStack.EMPTY);
                     $$28.setByPlayer($$27);
                  }
               }
            } else if ($$28.mayPickup($$3) && $$28.mayPlace($$27)) {
               int $$31 = $$28.getMaxStackSize($$27);
               if ($$27.getCount() > $$31) {
                  $$28.setByPlayer($$27.split($$31));
                  $$28.onTake($$3, $$29);
                  if (!$$4.add($$29)) {
                     $$3.drop($$29, true);
                  }
               } else {
                  $$4.setItem($$1, $$29);
                  $$28.setByPlayer($$27);
                  $$28.onTake($$3, $$29);
               }
            }
         }
      } else if ($$2 == net.minecraft.world.inventory.ClickType.CLONE && $$3.hasInfiniteMaterials() && this.getCarried().isEmpty() && $$0 >= 0) {
         net.minecraft.world.inventory.Slot $$32 = (net.minecraft.world.inventory.Slot)this.slots.get($$0);
         if ($$32.hasItem()) {
            ItemStack $$33 = $$32.getItem();
            this.setCarried($$33.copyWithCount($$33.getMaxStackSize()));
         }
      } else if ($$2 == net.minecraft.world.inventory.ClickType.THROW && this.getCarried().isEmpty() && $$0 >= 0) {
         net.minecraft.world.inventory.Slot $$34 = (net.minecraft.world.inventory.Slot)this.slots.get($$0);
         int $$35 = $$1 == 0 ? 1 : $$34.getItem().getCount();
         if (!$$3.canDropItems()) {
            return;
         }

         ItemStack $$36 = $$34.safeTake($$35, Integer.MAX_VALUE, $$3);
         $$3.drop($$36, true);
         $$3.handleCreativeModeItemDrop($$36);
         if ($$1 == 1) {
            while (!$$36.isEmpty() && ItemStack.isSameItem($$34.getItem(), $$36)) {
               if (!$$3.canDropItems()) {
                  return;
               }

               $$36 = $$34.safeTake($$35, Integer.MAX_VALUE, $$3);
               $$3.drop($$36, true);
               $$3.handleCreativeModeItemDrop($$36);
            }
         }
      } else if ($$2 == net.minecraft.world.inventory.ClickType.PICKUP_ALL && $$0 >= 0) {
         net.minecraft.world.inventory.Slot $$37 = (net.minecraft.world.inventory.Slot)this.slots.get($$0);
         ItemStack $$38 = this.getCarried();
         if (!$$38.isEmpty() && (!$$37.hasItem() || !$$37.mayPickup($$3))) {
            int $$39 = $$1 == 0 ? 0 : this.slots.size() - 1;
            int $$40 = $$1 == 0 ? 1 : -1;

            for (int $$41 = 0; $$41 < 2; $$41++) {
               for (int $$42 = $$39; $$42 >= 0 && $$42 < this.slots.size() && $$38.getCount() < $$38.getMaxStackSize(); $$42 += $$40) {
                  net.minecraft.world.inventory.Slot $$43 = (net.minecraft.world.inventory.Slot)this.slots.get($$42);
                  if ($$43.hasItem() && canItemQuickReplace($$43, $$38, true) && $$43.mayPickup($$3) && this.canTakeItemForPickAll($$38, $$43)) {
                     ItemStack $$44 = $$43.getItem();
                     if ($$41 != 0 || $$44.getCount() != $$44.getMaxStackSize()) {
                        ItemStack $$45 = $$43.safeTake($$44.getCount(), $$38.getMaxStackSize() - $$38.getCount(), $$3);
                        $$38.grow($$45.getCount());
                     }
                  }
               }
            }
         }
      }
   }

   private boolean tryItemClickBehaviourOverride(
      Player $$0, net.minecraft.world.inventory.ClickAction $$1, net.minecraft.world.inventory.Slot $$2, ItemStack $$3, ItemStack $$4
   ) {
      FeatureFlagSet $$5 = $$0.level().enabledFeatures();
      return $$4.isItemEnabled($$5) && $$4.overrideStackedOnOther($$2, $$1, $$0)
         ? true
         : $$3.isItemEnabled($$5) && $$3.overrideOtherStackedOnMe($$4, $$2, $$1, $$0, this.createCarriedSlotAccess());
   }

   private SlotAccess createCarriedSlotAccess() {
      return new SlotAccess() {
         public ItemStack get() {
            return AbstractContainerMenu.this.getCarried();
         }

         public boolean set(ItemStack $$0) {
            AbstractContainerMenu.this.setCarried($$0);
            return true;
         }
      };
   }

   public boolean canTakeItemForPickAll(ItemStack $$0, net.minecraft.world.inventory.Slot $$1) {
      return true;
   }

   public void removed(Player $$0) {
      if ($$0 instanceof ServerPlayer) {
         ItemStack $$1 = this.getCarried();
         if (!$$1.isEmpty()) {
            dropOrPlaceInInventory($$0, $$1);
            this.setCarried(ItemStack.EMPTY);
         }
      }
   }

   private static void dropOrPlaceInInventory(Player $$0, ItemStack $$1) {
      boolean $$2 = $$0.isRemoved() && $$0.getRemovalReason() != RemovalReason.CHANGED_DIMENSION;
      boolean $$4 = $$0 instanceof ServerPlayer $$3 && $$3.hasDisconnected();
      if ($$2 || $$4) {
         $$0.drop($$1, false);
      } else if ($$0 instanceof ServerPlayer) {
         $$0.getInventory().placeItemBackInInventory($$1);
      }
   }

   protected void clearContainer(Player $$0, Container $$1) {
      for (int $$2 = 0; $$2 < $$1.getContainerSize(); $$2++) {
         dropOrPlaceInInventory($$0, $$1.removeItemNoUpdate($$2));
      }
   }

   public void slotsChanged(Container $$0) {
      this.broadcastChanges();
   }

   public void setItem(int $$0, int $$1, ItemStack $$2) {
      this.getSlot($$0).set($$2);
      this.stateId = $$1;
   }

   public void initializeContents(int $$0, List<ItemStack> $$1, ItemStack $$2) {
      for (int $$3 = 0; $$3 < $$1.size(); $$3++) {
         this.getSlot($$3).set($$1.get($$3));
      }

      this.carried = $$2;
      this.stateId = $$0;
   }

   public void setData(int $$0, int $$1) {
      this.dataSlots.get($$0).set($$1);
   }

   public abstract boolean stillValid(Player var1);

   protected boolean moveItemStackTo(ItemStack $$0, int $$1, int $$2, boolean $$3) {
      boolean $$4 = false;
      int $$5 = $$1;
      if ($$3) {
         $$5 = $$2 - 1;
      }

      if ($$0.isStackable()) {
         while (!$$0.isEmpty() && ($$3 ? $$5 >= $$1 : $$5 < $$2)) {
            net.minecraft.world.inventory.Slot $$6 = (net.minecraft.world.inventory.Slot)this.slots.get($$5);
            ItemStack $$7 = $$6.getItem();
            if (!$$7.isEmpty() && ItemStack.isSameItemSameComponents($$0, $$7)) {
               int $$8 = $$7.getCount() + $$0.getCount();
               int $$9 = $$6.getMaxStackSize($$7);
               if ($$8 <= $$9) {
                  $$0.setCount(0);
                  $$7.setCount($$8);
                  $$6.setChanged();
                  $$4 = true;
               } else if ($$7.getCount() < $$9) {
                  $$0.shrink($$9 - $$7.getCount());
                  $$7.setCount($$9);
                  $$6.setChanged();
                  $$4 = true;
               }
            }

            if ($$3) {
               $$5--;
            } else {
               $$5++;
            }
         }
      }

      if (!$$0.isEmpty()) {
         if ($$3) {
            $$5 = $$2 - 1;
         } else {
            $$5 = $$1;
         }

         while ($$3 ? $$5 >= $$1 : $$5 < $$2) {
            net.minecraft.world.inventory.Slot $$10 = (net.minecraft.world.inventory.Slot)this.slots.get($$5);
            ItemStack $$11 = $$10.getItem();
            if ($$11.isEmpty() && $$10.mayPlace($$0)) {
               int $$12 = $$10.getMaxStackSize($$0);
               $$10.setByPlayer($$0.split(Math.min($$0.getCount(), $$12)));
               $$10.setChanged();
               $$4 = true;
               break;
            }

            if ($$3) {
               $$5--;
            } else {
               $$5++;
            }
         }
      }

      return $$4;
   }

   public static int getQuickcraftType(int $$0) {
      return $$0 >> 2 & 3;
   }

   public static int getQuickcraftHeader(int $$0) {
      return $$0 & 3;
   }

   public static int getQuickcraftMask(int $$0, int $$1) {
      return $$0 & 3 | ($$1 & 3) << 2;
   }

   public static boolean isValidQuickcraftType(int $$0, Player $$1) {
      if ($$0 == 0) {
         return true;
      } else {
         return $$0 == 1 ? true : $$0 == 2 && $$1.hasInfiniteMaterials();
      }
   }

   protected void resetQuickCraft() {
      this.quickcraftStatus = 0;
      this.quickcraftSlots.clear();
   }

   public static boolean canItemQuickReplace(net.minecraft.world.inventory.Slot $$0, ItemStack $$1, boolean $$2) {
      boolean $$3 = $$0 == null || !$$0.hasItem();
      return !$$3 && ItemStack.isSameItemSameComponents($$1, $$0.getItem())
         ? $$0.getItem().getCount() + ($$2 ? 0 : $$1.getCount()) <= $$1.getMaxStackSize()
         : $$3;
   }

   public static int getQuickCraftPlaceCount(Set<net.minecraft.world.inventory.Slot> $$0, int $$1, ItemStack $$2) {
      return switch ($$1) {
         case 0 -> Mth.floor((float)$$2.getCount() / $$0.size());
         case 1 -> 1;
         case 2 -> $$2.getMaxStackSize();
         default -> $$2.getCount();
      };
   }

   public boolean canDragTo(net.minecraft.world.inventory.Slot $$0) {
      return true;
   }

   public static int getRedstoneSignalFromBlockEntity(BlockEntity $$0) {
      return $$0 instanceof Container ? getRedstoneSignalFromContainer((Container)$$0) : 0;
   }

   public static int getRedstoneSignalFromContainer(Container $$0) {
      if ($$0 == null) {
         return 0;
      } else {
         float $$1 = 0.0F;

         for (int $$2 = 0; $$2 < $$0.getContainerSize(); $$2++) {
            ItemStack $$3 = $$0.getItem($$2);
            if (!$$3.isEmpty()) {
               $$1 += (float)$$3.getCount() / $$0.getMaxStackSize($$3);
            }
         }

         $$1 /= $$0.getContainerSize();
         return Mth.lerpDiscrete($$1, 0, 15);
      }
   }

   public void setCarried(ItemStack $$0) {
      this.carried = $$0;
   }

   public ItemStack getCarried() {
      return this.carried;
   }

   public void suppressRemoteUpdates() {
      this.suppressRemoteUpdates = true;
   }

   public void resumeRemoteUpdates() {
      this.suppressRemoteUpdates = false;
   }

   public void transferState(net.minecraft.world.inventory.AbstractContainerMenu $$0) {
      Table<Container, Integer, Integer> $$1 = HashBasedTable.create();

      for (int $$2 = 0; $$2 < $$0.slots.size(); $$2++) {
         net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)$$0.slots.get($$2);
         $$1.put($$3.container, $$3.getContainerSlot(), $$2);
      }

      for (int $$4 = 0; $$4 < this.slots.size(); $$4++) {
         net.minecraft.world.inventory.Slot $$5 = (net.minecraft.world.inventory.Slot)this.slots.get($$4);
         Integer $$6 = (Integer)$$1.get($$5.container, $$5.getContainerSlot());
         if ($$6 != null) {
            this.lastSlots.set($$4, (ItemStack)$$0.lastSlots.get($$6));
            net.minecraft.world.inventory.RemoteSlot $$7 = (net.minecraft.world.inventory.RemoteSlot)$$0.remoteSlots.get($$6);
            net.minecraft.world.inventory.RemoteSlot $$8 = (net.minecraft.world.inventory.RemoteSlot)this.remoteSlots.get($$4);
            if ($$7 instanceof net.minecraft.world.inventory.RemoteSlot.Synchronized $$9
               && $$8 instanceof net.minecraft.world.inventory.RemoteSlot.Synchronized $$10) {
               $$10.copyFrom($$9);
            }
         }
      }
   }

   public OptionalInt findSlot(Container $$0, int $$1) {
      for (int $$2 = 0; $$2 < this.slots.size(); $$2++) {
         net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$2);
         if ($$3.container == $$0 && $$1 == $$3.getContainerSlot()) {
            return OptionalInt.of($$2);
         }
      }

      return OptionalInt.empty();
   }

   public int getStateId() {
      return this.stateId;
   }

   public int incrementStateId() {
      this.stateId = this.stateId + 1 & 32767;
      return this.stateId;
   }
}
