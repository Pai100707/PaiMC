package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.SelectableRecipe.SingleInputEntry;
import net.minecraft.world.item.crafting.SelectableRecipe.SingleInputSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StonecutterMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
   public static final int INPUT_SLOT = 0;
   public static final int RESULT_SLOT = 1;
   private static final int INV_SLOT_START = 2;
   private static final int INV_SLOT_END = 29;
   private static final int USE_ROW_SLOT_START = 29;
   private static final int USE_ROW_SLOT_END = 38;
   private final net.minecraft.world.inventory.ContainerLevelAccess access;
   final net.minecraft.world.inventory.DataSlot selectedRecipeIndex = net.minecraft.world.inventory.DataSlot.standalone();
   private final Level level;
   private SingleInputSet<StonecutterRecipe> recipesForInput = SingleInputSet.empty();
   private ItemStack input = ItemStack.EMPTY;
   long lastSoundTime;
   final net.minecraft.world.inventory.Slot inputSlot;
   final net.minecraft.world.inventory.Slot resultSlot;
   Runnable slotUpdateListener = () -> {};
   public final Container container = new SimpleContainer(1) {
      public void setChanged() {
         super.setChanged();
         StonecutterMenu.this.slotsChanged(this);
         StonecutterMenu.this.slotUpdateListener.run();
      }
   };
   final net.minecraft.world.inventory.ResultContainer resultContainer = new net.minecraft.world.inventory.ResultContainer();

   public StonecutterMenu(int $$0, Inventory $$1) {
      this($$0, $$1, net.minecraft.world.inventory.ContainerLevelAccess.NULL);
   }

   public StonecutterMenu(int $$0, Inventory $$1, final net.minecraft.world.inventory.ContainerLevelAccess $$2) {
      super(net.minecraft.world.inventory.MenuType.STONECUTTER, $$0);
      this.access = $$2;
      this.level = $$1.player.level();
      this.inputSlot = this.addSlot(new net.minecraft.world.inventory.Slot(this.container, 0, 20, 33));
      this.resultSlot = this.addSlot(new net.minecraft.world.inventory.Slot(this.resultContainer, 1, 143, 33) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return false;
         }

         @Override
         public void onTake(Player $$0, ItemStack $$1x) {
            $$1x.onCraftedBy($$0, $$1x.getCount());
            StonecutterMenu.this.resultContainer.awardUsedRecipes($$0, this.getRelevantItems());
            ItemStack $$2x = StonecutterMenu.this.inputSlot.remove(1);
            if (!$$2x.isEmpty()) {
               StonecutterMenu.this.setupResultSlot(StonecutterMenu.this.selectedRecipeIndex.get());
            }

            $$2.execute(($$0x, $$1xx) -> {
               long $$2xxx = $$0x.getGameTime();
               if (StonecutterMenu.this.lastSoundTime != $$2xxx) {
                  $$0x.playSound(null, $$1xx, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  StonecutterMenu.this.lastSoundTime = $$2xxx;
               }
            });
            super.onTake($$0, $$1x);
         }

         private List<ItemStack> getRelevantItems() {
            return List.of(StonecutterMenu.this.inputSlot.getItem());
         }
      });
      this.addStandardInventorySlots($$1, 8, 84);
      this.addDataSlot(this.selectedRecipeIndex);
   }

   public int getSelectedRecipeIndex() {
      return this.selectedRecipeIndex.get();
   }

   public SingleInputSet<StonecutterRecipe> getVisibleRecipes() {
      return this.recipesForInput;
   }

   public int getNumberOfVisibleRecipes() {
      return this.recipesForInput.size();
   }

   public boolean hasInputItem() {
      return this.inputSlot.hasItem() && !this.recipesForInput.isEmpty();
   }

   @Override
   public boolean stillValid(Player $$0) {
      return stillValid(this.access, $$0, Blocks.STONECUTTER);
   }

   @Override
   public boolean clickMenuButton(Player $$0, int $$1) {
      if (this.selectedRecipeIndex.get() == $$1) {
         return false;
      } else {
         if (this.isValidRecipeIndex($$1)) {
            this.selectedRecipeIndex.set($$1);
            this.setupResultSlot($$1);
         }

         return true;
      }
   }

   private boolean isValidRecipeIndex(int $$0) {
      return $$0 >= 0 && $$0 < this.recipesForInput.size();
   }

   @Override
   public void slotsChanged(Container $$0) {
      ItemStack $$1 = this.inputSlot.getItem();
      if (!$$1.is(this.input.getItem())) {
         this.input = $$1.copy();
         this.setupRecipeList($$1);
      }
   }

   private void setupRecipeList(ItemStack $$0) {
      this.selectedRecipeIndex.set(-1);
      this.resultSlot.set(ItemStack.EMPTY);
      if (!$$0.isEmpty()) {
         this.recipesForInput = this.level.recipeAccess().stonecutterRecipes().selectByInput($$0);
      } else {
         this.recipesForInput = SingleInputSet.empty();
      }
   }

   void setupResultSlot(int $$0) {
      Optional<RecipeHolder<StonecutterRecipe>> $$2;
      if (!this.recipesForInput.isEmpty() && this.isValidRecipeIndex($$0)) {
         SingleInputEntry<StonecutterRecipe> $$1 = (SingleInputEntry<StonecutterRecipe>)this.recipesForInput.entries().get($$0);
         $$2 = $$1.recipe().recipe();
      } else {
         $$2 = Optional.empty();
      }

      $$2.ifPresentOrElse($$0x -> {
         this.resultContainer.setRecipeUsed($$0x);
         this.resultSlot.set(((StonecutterRecipe)$$0x.value()).assemble(new SingleRecipeInput(this.container.getItem(0)), this.level.registryAccess()));
      }, () -> {
         this.resultSlot.set(ItemStack.EMPTY);
         this.resultContainer.setRecipeUsed(null);
      });
      this.broadcastChanges();
   }

   @Override
   public net.minecraft.world.inventory.MenuType<?> getType() {
      return net.minecraft.world.inventory.MenuType.STONECUTTER;
   }

   public void registerUpdateListener(Runnable $$0) {
      this.slotUpdateListener = $$0;
   }

   @Override
   public boolean canTakeItemForPickAll(ItemStack $$0, net.minecraft.world.inventory.Slot $$1) {
      return $$1.container != this.resultContainer && super.canTakeItemForPickAll($$0, $$1);
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
      if ($$3 != null && $$3.hasItem()) {
         ItemStack $$4 = $$3.getItem();
         Item $$5 = $$4.getItem();
         $$2 = $$4.copy();
         if ($$1 == 1) {
            $$5.onCraftedBy($$4, $$0);
            if (!this.moveItemStackTo($$4, 2, 38, true)) {
               return ItemStack.EMPTY;
            }

            $$3.onQuickCraft($$4, $$2);
         } else if ($$1 == 0) {
            if (!this.moveItemStackTo($$4, 2, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.level.recipeAccess().stonecutterRecipes().acceptsInput($$4)) {
            if (!this.moveItemStackTo($$4, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if ($$1 >= 2 && $$1 < 29) {
            if (!this.moveItemStackTo($$4, 29, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if ($$1 >= 29 && $$1 < 38 && !this.moveItemStackTo($$4, 2, 29, false)) {
            return ItemStack.EMPTY;
         }

         if ($$4.isEmpty()) {
            $$3.setByPlayer(ItemStack.EMPTY);
         }

         $$3.setChanged();
         if ($$4.getCount() == $$2.getCount()) {
            return ItemStack.EMPTY;
         }

         $$3.onTake($$0, $$4);
         if ($$1 == 1) {
            $$0.drop($$4, false);
         }

         this.broadcastChanges();
      }

      return $$2;
   }

   @Override
   public void removed(Player $$0) {
      super.removed($$0);
      this.resultContainer.removeItemNoUpdate(1);
      this.access.execute(($$1, $$2) -> this.clearContainer($$0, this.container));
   }
}
