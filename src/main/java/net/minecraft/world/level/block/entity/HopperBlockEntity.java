package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class HopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper {
   public static final int MOVE_ITEM_SPEED = 8;
   public static final int HOPPER_CONTAINER_SIZE = 5;
   private static final int[][] CACHED_SLOTS = new int[54][];
   private static final int NO_COOLDOWN_TIME = -1;
   private static final Component DEFAULT_NAME = Component.translatable("container.hopper");
   private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
   private int cooldownTime = -1;
   private long tickedGameTime;
   private Direction facing;

   public HopperBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.HOPPER, $$0, $$1);
      this.facing = $$1.getValue(HopperBlock.FACING);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable($$0)) {
         ContainerHelper.loadAllItems($$0, this.items);
      }

      this.cooldownTime = $$0.getIntOr("TransferCooldown", -1);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (!this.trySaveLootTable($$0)) {
         ContainerHelper.saveAllItems($$0, this.items);
      }

      $$0.putInt("TransferCooldown", this.cooldownTime);
   }

   public int getContainerSize() {
      return this.items.size();
   }

   @Override
   public ItemStack removeItem(int $$0, int $$1) {
      this.unpackLootTable(null);
      return ContainerHelper.removeItem(this.getItems(), $$0, $$1);
   }

   @Override
   public void setItem(int $$0, ItemStack $$1) {
      this.unpackLootTable(null);
      this.getItems().set($$0, $$1);
      $$1.limitSize(this.getMaxStackSize($$1));
   }

   @Override
   public void setBlockState(BlockState $$0) {
      super.setBlockState($$0);
      this.facing = $$0.getValue(HopperBlock.FACING);
   }

   @Override
   protected Component getDefaultName() {
      return DEFAULT_NAME;
   }

   public static void pushItemsTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, HopperBlockEntity $$3) {
      $$3.cooldownTime--;
      $$3.tickedGameTime = $$0.getGameTime();
      if (!$$3.isOnCooldown()) {
         $$3.setCooldown(0);
         tryMoveItems($$0, $$1, $$2, $$3, () -> suckInItems($$0, $$3));
      }
   }

   private static boolean tryMoveItems(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, HopperBlockEntity $$3, BooleanSupplier $$4) {
      if ($$0.isClientSide()) {
         return false;
      } else {
         if (!$$3.isOnCooldown() && $$2.getValue(HopperBlock.ENABLED)) {
            boolean $$5 = false;
            if (!$$3.isEmpty()) {
               $$5 = ejectItems($$0, $$1, $$3);
            }

            if (!$$3.inventoryFull()) {
               $$5 |= $$4.getAsBoolean();
            }

            if ($$5) {
               $$3.setCooldown(8);
               setChanged($$0, $$1, $$2);
               return true;
            }
         }

         return false;
      }
   }

   private boolean inventoryFull() {
      for (ItemStack $$0 : this.items) {
         if ($$0.isEmpty() || $$0.getCount() != $$0.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   private static boolean ejectItems(net.minecraft.world.level.Level $$0, BlockPos $$1, HopperBlockEntity $$2) {
      Container $$3 = getAttachedContainer($$0, $$1, $$2);
      if ($$3 == null) {
         return false;
      } else {
         Direction $$4 = $$2.facing.getOpposite();
         if (isFullContainer($$3, $$4)) {
            return false;
         } else {
            for (int $$5 = 0; $$5 < $$2.getContainerSize(); $$5++) {
               ItemStack $$6 = $$2.getItem($$5);
               if (!$$6.isEmpty()) {
                  int $$7 = $$6.getCount();
                  ItemStack $$8 = addItem($$2, $$3, $$2.removeItem($$5, 1), $$4);
                  if ($$8.isEmpty()) {
                     $$3.setChanged();
                     return true;
                  }

                  $$6.setCount($$7);
                  if ($$7 == 1) {
                     $$2.setItem($$5, $$6);
                  }
               }
            }

            return false;
         }
      }
   }

   private static int[] getSlots(Container $$0, Direction $$1) {
      if ($$0 instanceof WorldlyContainer $$2) {
         return $$2.getSlotsForFace($$1);
      } else {
         int $$3 = $$0.getContainerSize();
         if ($$3 < CACHED_SLOTS.length) {
            int[] $$4 = CACHED_SLOTS[$$3];
            if ($$4 != null) {
               return $$4;
            } else {
               int[] $$5 = createFlatSlots($$3);
               CACHED_SLOTS[$$3] = $$5;
               return $$5;
            }
         } else {
            return createFlatSlots($$3);
         }
      }
   }

   private static int[] createFlatSlots(int $$0) {
      int[] $$1 = new int[$$0];
      int $$2 = 0;

      while ($$2 < $$1.length) {
         $$1[$$2] = $$2++;
      }

      return $$1;
   }

   private static boolean isFullContainer(Container $$0, Direction $$1) {
      int[] $$2 = getSlots($$0, $$1);

      for (int $$3 : $$2) {
         ItemStack $$4 = $$0.getItem($$3);
         if ($$4.getCount() < $$4.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   public static boolean suckInItems(net.minecraft.world.level.Level $$0, Hopper $$1) {
      BlockPos $$2 = BlockPos.containing($$1.getLevelX(), $$1.getLevelY() + 1.0, $$1.getLevelZ());
      BlockState $$3 = $$0.getBlockState($$2);
      Container $$4 = getSourceContainer($$0, $$1, $$2, $$3);
      if ($$4 != null) {
         Direction $$5 = Direction.DOWN;

         for (int $$6 : getSlots($$4, $$5)) {
            if (tryTakeInItemFromSlot($$1, $$4, $$6, $$5)) {
               return true;
            }
         }

         return false;
      } else {
         boolean $$7 = $$1.isGridAligned() && $$3.isCollisionShapeFullBlock($$0, $$2) && !$$3.is(BlockTags.DOES_NOT_BLOCK_HOPPERS);
         if (!$$7) {
            for (ItemEntity $$8 : getItemsAtAndAbove($$0, $$1)) {
               if (addItem($$1, $$8)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static boolean tryTakeInItemFromSlot(Hopper $$0, Container $$1, int $$2, Direction $$3) {
      ItemStack $$4 = $$1.getItem($$2);
      if (!$$4.isEmpty() && canTakeItemFromContainer($$0, $$1, $$4, $$2, $$3)) {
         int $$5 = $$4.getCount();
         ItemStack $$6 = addItem($$1, $$0, $$1.removeItem($$2, 1), null);
         if ($$6.isEmpty()) {
            $$1.setChanged();
            return true;
         }

         $$4.setCount($$5);
         if ($$5 == 1) {
            $$1.setItem($$2, $$4);
         }
      }

      return false;
   }

   public static boolean addItem(Container $$0, ItemEntity $$1) {
      boolean $$2 = false;
      ItemStack $$3 = $$1.getItem().copy();
      ItemStack $$4 = addItem(null, $$0, $$3, null);
      if ($$4.isEmpty()) {
         $$2 = true;
         $$1.setItem(ItemStack.EMPTY);
         $$1.discard();
      } else {
         $$1.setItem($$4);
      }

      return $$2;
   }

   public static ItemStack addItem(Container $$0, Container $$1, ItemStack $$2, Direction $$3) {
      if ($$1 instanceof WorldlyContainer $$4 && $$3 != null) {
         int[] $$5 = $$4.getSlotsForFace($$3);

         for (int $$6 = 0; $$6 < $$5.length && !$$2.isEmpty(); $$6++) {
            $$2 = tryMoveInItem($$0, $$1, $$2, $$5[$$6], $$3);
         }
      } else {
         int $$7 = $$1.getContainerSize();

         for (int $$8 = 0; $$8 < $$7 && !$$2.isEmpty(); $$8++) {
            $$2 = tryMoveInItem($$0, $$1, $$2, $$8, $$3);
         }
      }

      return $$2;
   }

   private static boolean canPlaceItemInContainer(Container $$0, ItemStack $$1, int $$2, Direction $$3) {
      return !$$0.canPlaceItem($$2, $$1) ? false : !($$0 instanceof WorldlyContainer $$4 && !$$4.canPlaceItemThroughFace($$2, $$1, $$3));
   }

   private static boolean canTakeItemFromContainer(Container $$0, Container $$1, ItemStack $$2, int $$3, Direction $$4) {
      return !$$1.canTakeItem($$0, $$3, $$2) ? false : !($$1 instanceof WorldlyContainer $$5 && !$$5.canTakeItemThroughFace($$3, $$2, $$4));
   }

   private static ItemStack tryMoveInItem(Container $$0, Container $$1, ItemStack $$2, int $$3, Direction $$4) {
      ItemStack $$5 = $$1.getItem($$3);
      if (canPlaceItemInContainer($$1, $$2, $$3, $$4)) {
         boolean $$6 = false;
         boolean $$7 = $$1.isEmpty();
         if ($$5.isEmpty()) {
            $$1.setItem($$3, $$2);
            $$2 = ItemStack.EMPTY;
            $$6 = true;
         } else if (canMergeItems($$5, $$2)) {
            int $$8 = $$2.getMaxStackSize() - $$5.getCount();
            int $$9 = Math.min($$2.getCount(), $$8);
            $$2.shrink($$9);
            $$5.grow($$9);
            $$6 = $$9 > 0;
         }

         if ($$6) {
            if ($$7 && $$1 instanceof HopperBlockEntity $$10 && !$$10.isOnCustomCooldown()) {
               int $$11 = 0;
               if ($$0 instanceof HopperBlockEntity $$12 && $$10.tickedGameTime >= $$12.tickedGameTime) {
                  $$11 = 1;
               }

               $$10.setCooldown(8 - $$11);
            }

            $$1.setChanged();
         }
      }

      return $$2;
   }

   
   private static Container getAttachedContainer(net.minecraft.world.level.Level $$0, BlockPos $$1, HopperBlockEntity $$2) {
      return getContainerAt($$0, $$1.relative($$2.facing));
   }

   
   private static Container getSourceContainer(net.minecraft.world.level.Level $$0, Hopper $$1, BlockPos $$2, BlockState $$3) {
      return getContainerAt($$0, $$2, $$3, $$1.getLevelX(), $$1.getLevelY() + 1.0, $$1.getLevelZ());
   }

   public static List<ItemEntity> getItemsAtAndAbove(net.minecraft.world.level.Level $$0, Hopper $$1) {
      AABB $$2 = $$1.getSuckAabb().move($$1.getLevelX() - 0.5, $$1.getLevelY() - 0.5, $$1.getLevelZ() - 0.5);
      return $$0.getEntitiesOfClass(ItemEntity.class, $$2, EntitySelector.ENTITY_STILL_ALIVE);
   }

   
   public static Container getContainerAt(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      return getContainerAt($$0, $$1, $$0.getBlockState($$1), $$1.getX() + 0.5, $$1.getY() + 0.5, $$1.getZ() + 0.5);
   }

   
   private static Container getContainerAt(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, double $$3, double $$4, double $$5) {
      Container $$6 = getBlockContainer($$0, $$1, $$2);
      if ($$6 == null) {
         $$6 = getEntityContainer($$0, $$3, $$4, $$5);
      }

      return $$6;
   }

   
   private static Container getBlockContainer(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      Block $$3 = $$2.getBlock();
      if ($$3 instanceof WorldlyContainerHolder) {
         return ((WorldlyContainerHolder)$$3).getContainer($$2, $$0, $$1);
      } else if ($$2.hasBlockEntity() && $$0.getBlockEntity($$1) instanceof Container $$5) {
         if ($$5 instanceof ChestBlockEntity && $$3 instanceof ChestBlock) {
            $$5 = ChestBlock.getContainer((ChestBlock)$$3, $$2, $$0, $$1, true);
         }

         return $$5;
      } else {
         return null;
      }
   }

   
   private static Container getEntityContainer(net.minecraft.world.level.Level $$0, double $$1, double $$2, double $$3) {
      List<Entity> $$4 = $$0.getEntities(
         (Entity)null, new AABB($$1 - 0.5, $$2 - 0.5, $$3 - 0.5, $$1 + 0.5, $$2 + 0.5, $$3 + 0.5), EntitySelector.CONTAINER_ENTITY_SELECTOR
      );
      return !$$4.isEmpty() ? (Container)$$4.get($$0.random.nextInt($$4.size())) : null;
   }

   private static boolean canMergeItems(ItemStack $$0, ItemStack $$1) {
      return $$0.getCount() <= $$0.getMaxStackSize() && ItemStack.isSameItemSameComponents($$0, $$1);
   }

   @Override
   public double getLevelX() {
      return this.worldPosition.getX() + 0.5;
   }

   @Override
   public double getLevelY() {
      return this.worldPosition.getY() + 0.5;
   }

   @Override
   public double getLevelZ() {
      return this.worldPosition.getZ() + 0.5;
   }

   @Override
   public boolean isGridAligned() {
      return true;
   }

   private void setCooldown(int $$0) {
      this.cooldownTime = $$0;
   }

   private boolean isOnCooldown() {
      return this.cooldownTime > 0;
   }

   private boolean isOnCustomCooldown() {
      return this.cooldownTime > 8;
   }

   @Override
   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   @Override
   protected void setItems(NonNullList<ItemStack> $$0) {
      this.items = $$0;
   }

   public static void entityInside(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Entity $$3, HopperBlockEntity $$4) {
      if ($$3 instanceof ItemEntity $$5
         && !$$5.getItem().isEmpty()
         && $$3.getBoundingBox().move(-$$1.getX(), -$$1.getY(), -$$1.getZ()).intersects($$4.getSuckAabb())) {
         tryMoveItems($$0, $$1, $$2, $$4, () -> addItem($$4, $$5));
      }
   }

   @Override
   protected AbstractContainerMenu createMenu(int $$0, Inventory $$1) {
      return new HopperMenu($$0, $$1, this);
   }
}
