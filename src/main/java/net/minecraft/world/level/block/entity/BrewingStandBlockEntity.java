package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BrewingStandBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
   private static final int INGREDIENT_SLOT = 3;
   private static final int FUEL_SLOT = 4;
   private static final int[] SLOTS_FOR_UP = new int[]{3};
   private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
   private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};
   public static final int FUEL_USES = 20;
   public static final int DATA_BREW_TIME = 0;
   public static final int DATA_FUEL_USES = 1;
   public static final int NUM_DATA_VALUES = 2;
   private static final short DEFAULT_BREW_TIME = 0;
   private static final byte DEFAULT_FUEL = 0;
   private static final Component DEFAULT_NAME = Component.translatable("container.brewing");
   private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
   int brewTime;
   private boolean[] lastPotionCount;
   private Item ingredient;
   int fuel;
   protected final ContainerData dataAccess = new ContainerData() {
      public int get(int $$0) {
         return switch ($$0) {
            case 0 -> BrewingStandBlockEntity.this.brewTime;
            case 1 -> BrewingStandBlockEntity.this.fuel;
            default -> 0;
         };
      }

      public void set(int $$0, int $$1) {
         switch ($$0) {
            case 0:
               BrewingStandBlockEntity.this.brewTime = $$1;
               break;
            case 1:
               BrewingStandBlockEntity.this.fuel = $$1;
         }
      }

      public int getCount() {
         return 2;
      }
   };

   public BrewingStandBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.BREWING_STAND, $$0, $$1);
   }

   @Override
   protected Component getDefaultName() {
      return DEFAULT_NAME;
   }

   public int getContainerSize() {
      return this.items.size();
   }

   @Override
   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   @Override
   protected void setItems(NonNullList<ItemStack> $$0) {
      this.items = $$0;
   }

   public static void serverTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, BrewingStandBlockEntity $$3) {
      ItemStack $$4 = (ItemStack)$$3.items.get(4);
      if ($$3.fuel <= 0 && $$4.is(ItemTags.BREWING_FUEL)) {
         $$3.fuel = 20;
         $$4.shrink(1);
         setChanged($$0, $$1, $$2);
      }

      boolean $$5 = isBrewable($$0.potionBrewing(), $$3.items);
      boolean $$6 = $$3.brewTime > 0;
      ItemStack $$7 = (ItemStack)$$3.items.get(3);
      if ($$6) {
         $$3.brewTime--;
         boolean $$8 = $$3.brewTime == 0;
         if ($$8 && $$5) {
            doBrew($$0, $$1, $$3.items);
         } else if (!$$5 || !$$7.is($$3.ingredient)) {
            $$3.brewTime = 0;
         }

         setChanged($$0, $$1, $$2);
      } else if ($$5 && $$3.fuel > 0) {
         $$3.fuel--;
         $$3.brewTime = 400;
         $$3.ingredient = $$7.getItem();
         setChanged($$0, $$1, $$2);
      }

      boolean[] $$9 = $$3.getPotionBits();
      if (!Arrays.equals($$9, $$3.lastPotionCount)) {
         $$3.lastPotionCount = $$9;
         BlockState $$10 = $$2;
         if (!($$2.getBlock() instanceof BrewingStandBlock)) {
            return;
         }

         for (int $$11 = 0; $$11 < BrewingStandBlock.HAS_BOTTLE.length; $$11++) {
            $$10 = $$10.setValue(BrewingStandBlock.HAS_BOTTLE[$$11], $$9[$$11]);
         }

         $$0.setBlock($$1, $$10, 2);
      }
   }

   private boolean[] getPotionBits() {
      boolean[] $$0 = new boolean[3];

      for (int $$1 = 0; $$1 < 3; $$1++) {
         if (!((ItemStack)this.items.get($$1)).isEmpty()) {
            $$0[$$1] = true;
         }
      }

      return $$0;
   }

   private static boolean isBrewable(PotionBrewing $$0, NonNullList<ItemStack> $$1) {
      ItemStack $$2 = (ItemStack)$$1.get(3);
      if ($$2.isEmpty()) {
         return false;
      } else if (!$$0.isIngredient($$2)) {
         return false;
      } else {
         for (int $$3 = 0; $$3 < 3; $$3++) {
            ItemStack $$4 = (ItemStack)$$1.get($$3);
            if (!$$4.isEmpty() && $$0.hasMix($$4, $$2)) {
               return true;
            }
         }

         return false;
      }
   }

   private static void doBrew(net.minecraft.world.level.Level $$0, BlockPos $$1, NonNullList<ItemStack> $$2) {
      ItemStack $$3 = (ItemStack)$$2.get(3);
      PotionBrewing $$4 = $$0.potionBrewing();

      for (int $$5 = 0; $$5 < 3; $$5++) {
         $$2.set($$5, $$4.mix($$3, (ItemStack)$$2.get($$5)));
      }

      $$3.shrink(1);
      ItemStack $$6 = $$3.getItem().getCraftingRemainder();
      if (!$$6.isEmpty()) {
         if ($$3.isEmpty()) {
            $$3 = $$6;
         } else {
            Containers.dropItemStack($$0, $$1.getX(), $$1.getY(), $$1.getZ(), $$6);
         }
      }

      $$2.set(3, $$3);
      $$0.levelEvent(1035, $$1, 0);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      ContainerHelper.loadAllItems($$0, this.items);
      this.brewTime = $$0.getShortOr("BrewTime", (short)0);
      if (this.brewTime > 0) {
         this.ingredient = ((ItemStack)this.items.get(3)).getItem();
      }

      this.fuel = $$0.getByteOr("Fuel", (byte)0);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.putShort("BrewTime", (short)this.brewTime);
      ContainerHelper.saveAllItems($$0, this.items);
      $$0.putByte("Fuel", (byte)this.fuel);
   }

   public boolean canPlaceItem(int $$0, ItemStack $$1) {
      if ($$0 == 3) {
         PotionBrewing $$2 = this.level != null ? this.level.potionBrewing() : PotionBrewing.EMPTY;
         return $$2.isIngredient($$1);
      } else {
         return $$0 == 4
            ? $$1.is(ItemTags.BREWING_FUEL)
            : ($$1.is(Items.POTION) || $$1.is(Items.SPLASH_POTION) || $$1.is(Items.LINGERING_POTION) || $$1.is(Items.GLASS_BOTTLE))
               && this.getItem($$0).isEmpty();
      }
   }

   public int[] getSlotsForFace(Direction $$0) {
      if ($$0 == Direction.UP) {
         return SLOTS_FOR_UP;
      } else {
         return $$0 == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES;
      }
   }

   public boolean canPlaceItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
      return this.canPlaceItem($$0, $$1);
   }

   public boolean canTakeItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
      return $$0 == 3 ? $$1.is(Items.GLASS_BOTTLE) : true;
   }

   @Override
   protected AbstractContainerMenu createMenu(int $$0, Inventory $$1) {
      return new BrewingStandMenu($$0, $$1, this, this.dataAccess);
   }
}
