package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.recipebook.ServerPlaceRecipe.CraftingMenuAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public abstract class AbstractFurnaceMenu extends net.minecraft.world.inventory.RecipeBookMenu {
   public static final int INGREDIENT_SLOT = 0;
   public static final int FUEL_SLOT = 1;
   public static final int RESULT_SLOT = 2;
   public static final int SLOT_COUNT = 3;
   public static final int DATA_COUNT = 4;
   private static final int INV_SLOT_START = 3;
   private static final int INV_SLOT_END = 30;
   private static final int USE_ROW_SLOT_START = 30;
   private static final int USE_ROW_SLOT_END = 39;
   final Container container;
   private final net.minecraft.world.inventory.ContainerData data;
   protected final Level level;
   private final RecipeType<? extends AbstractCookingRecipe> recipeType;
   private final RecipePropertySet acceptedInputs;
   private final net.minecraft.world.inventory.RecipeBookType recipeBookType;

   protected AbstractFurnaceMenu(
      net.minecraft.world.inventory.MenuType<?> $$0,
      RecipeType<? extends AbstractCookingRecipe> $$1,
      ResourceKey<RecipePropertySet> $$2,
      net.minecraft.world.inventory.RecipeBookType $$3,
      int $$4,
      Inventory $$5
   ) {
      this($$0, $$1, $$2, $$3, $$4, $$5, new SimpleContainer(3), new net.minecraft.world.inventory.SimpleContainerData(4));
   }

   protected AbstractFurnaceMenu(
      net.minecraft.world.inventory.MenuType<?> $$0,
      RecipeType<? extends AbstractCookingRecipe> $$1,
      ResourceKey<RecipePropertySet> $$2,
      net.minecraft.world.inventory.RecipeBookType $$3,
      int $$4,
      Inventory $$5,
      Container $$6,
      net.minecraft.world.inventory.ContainerData $$7
   ) {
      super($$0, $$4);
      this.recipeType = $$1;
      this.recipeBookType = $$3;
      checkContainerSize($$6, 3);
      checkContainerDataCount($$7, 4);
      this.container = $$6;
      this.data = $$7;
      this.level = $$5.player.level();
      this.acceptedInputs = this.level.recipeAccess().propertySet($$2);
      this.addSlot(new net.minecraft.world.inventory.Slot($$6, 0, 56, 17));
      this.addSlot(new net.minecraft.world.inventory.FurnaceFuelSlot(this, $$6, 1, 56, 53));
      this.addSlot(new net.minecraft.world.inventory.FurnaceResultSlot($$5.player, $$6, 2, 116, 35));
      this.addStandardInventorySlots($$5, 8, 84);
      this.addDataSlots($$7);
   }

   @Override
   public void fillCraftSlotsStackedContents(StackedItemContents $$0) {
      if (this.container instanceof net.minecraft.world.inventory.StackedContentsCompatible) {
         ((net.minecraft.world.inventory.StackedContentsCompatible)this.container).fillStackedContents($$0);
      }
   }

   public net.minecraft.world.inventory.Slot getResultSlot() {
      return (net.minecraft.world.inventory.Slot)this.slots.get(2);
   }

   @Override
   public boolean stillValid(Player $$0) {
      return this.container.stillValid($$0);
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
      if ($$3 != null && $$3.hasItem()) {
         ItemStack $$4 = $$3.getItem();
         $$2 = $$4.copy();
         if ($$1 == 2) {
            if (!this.moveItemStackTo($$4, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            $$3.onQuickCraft($$4, $$2);
         } else if ($$1 != 1 && $$1 != 0) {
            if (this.canSmelt($$4)) {
               if (!this.moveItemStackTo($$4, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (this.isFuel($$4)) {
               if (!this.moveItemStackTo($$4, 1, 2, false)) {
                  return ItemStack.EMPTY;
               }
            } else if ($$1 >= 3 && $$1 < 30) {
               if (!this.moveItemStackTo($$4, 30, 39, false)) {
                  return ItemStack.EMPTY;
               }
            } else if ($$1 >= 30 && $$1 < 39 && !this.moveItemStackTo($$4, 3, 30, false)) {
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

   protected boolean canSmelt(ItemStack $$0) {
      return this.acceptedInputs.test($$0);
   }

   protected boolean isFuel(ItemStack $$0) {
      return this.level.fuelValues().isFuel($$0);
   }

   public float getBurnProgress() {
      int $$0 = this.data.get(2);
      int $$1 = this.data.get(3);
      return $$1 != 0 && $$0 != 0 ? Mth.clamp((float)$$0 / $$1, 0.0F, 1.0F) : 0.0F;
   }

   public float getLitProgress() {
      int $$0 = this.data.get(1);
      if ($$0 == 0) {
         $$0 = 200;
      }

      return Mth.clamp((float)this.data.get(0) / $$0, 0.0F, 1.0F);
   }

   public boolean isLit() {
      return this.data.get(0) > 0;
   }

   @Override
   public net.minecraft.world.inventory.RecipeBookType getRecipeBookType() {
      return this.recipeBookType;
   }

   @Override
   public net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction handlePlacement(
      boolean $$0, boolean $$1, RecipeHolder<?> $$2, final ServerLevel $$3, Inventory $$4
   ) {
      final List<net.minecraft.world.inventory.Slot> $$5 = List.of(this.getSlot(0), this.getSlot(2));
      return ServerPlaceRecipe.placeRecipe(new CraftingMenuAccess<AbstractCookingRecipe>() {
         public void fillCraftSlotsStackedContents(StackedItemContents $$0) {
            AbstractFurnaceMenu.this.fillCraftSlotsStackedContents($$0);
         }

         public void clearCraftingContent() {
            $$5.forEach($$0x -> $$0x.set(ItemStack.EMPTY));
         }

         public boolean recipeMatches(RecipeHolder<AbstractCookingRecipe> $$0) {
            return ((AbstractCookingRecipe)$$0.value()).matches(new SingleRecipeInput(AbstractFurnaceMenu.this.container.getItem(0)), $$3);
         }
      }, 1, 1, List.of(this.getSlot(0)), $$5, $$4, $$2, $$0, $$1);
   }
}
