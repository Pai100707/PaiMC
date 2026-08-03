package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SmithingMenu extends net.minecraft.world.inventory.ItemCombinerMenu {
   public static final int TEMPLATE_SLOT = 0;
   public static final int BASE_SLOT = 1;
   public static final int ADDITIONAL_SLOT = 2;
   public static final int RESULT_SLOT = 3;
   public static final int TEMPLATE_SLOT_X_PLACEMENT = 8;
   public static final int BASE_SLOT_X_PLACEMENT = 26;
   public static final int ADDITIONAL_SLOT_X_PLACEMENT = 44;
   private static final int RESULT_SLOT_X_PLACEMENT = 98;
   public static final int SLOT_Y_PLACEMENT = 48;
   private final Level level;
   private final RecipePropertySet baseItemTest;
   private final RecipePropertySet templateItemTest;
   private final RecipePropertySet additionItemTest;
   private final net.minecraft.world.inventory.DataSlot hasRecipeError = net.minecraft.world.inventory.DataSlot.standalone();

   public SmithingMenu(int $$0, Inventory $$1) {
      this($$0, $$1, net.minecraft.world.inventory.ContainerLevelAccess.NULL);
   }

   public SmithingMenu(int $$0, Inventory $$1, net.minecraft.world.inventory.ContainerLevelAccess $$2) {
      this($$0, $$1, $$2, $$1.player.level());
   }

   private SmithingMenu(int $$0, Inventory $$1, net.minecraft.world.inventory.ContainerLevelAccess $$2, Level $$3) {
      super(net.minecraft.world.inventory.MenuType.SMITHING, $$0, $$1, $$2, createInputSlotDefinitions($$3.recipeAccess()));
      this.level = $$3;
      this.baseItemTest = $$3.recipeAccess().propertySet(RecipePropertySet.SMITHING_BASE);
      this.templateItemTest = $$3.recipeAccess().propertySet(RecipePropertySet.SMITHING_TEMPLATE);
      this.additionItemTest = $$3.recipeAccess().propertySet(RecipePropertySet.SMITHING_ADDITION);
      this.addDataSlot(this.hasRecipeError).set(0);
   }

   private static net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition createInputSlotDefinitions(RecipeAccess $$0) {
      RecipePropertySet $$1 = $$0.propertySet(RecipePropertySet.SMITHING_BASE);
      RecipePropertySet $$2 = $$0.propertySet(RecipePropertySet.SMITHING_TEMPLATE);
      RecipePropertySet $$3 = $$0.propertySet(RecipePropertySet.SMITHING_ADDITION);
      return net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.create()
         .withSlot(0, 8, 48, $$2::test)
         .withSlot(1, 26, 48, $$1::test)
         .withSlot(2, 44, 48, $$3::test)
         .withResultSlot(3, 98, 48)
         .build();
   }

   @Override
   protected boolean isValidBlock(BlockState $$0) {
      return $$0.is(Blocks.SMITHING_TABLE);
   }

   @Override
   protected void onTake(Player $$0, ItemStack $$1) {
      $$1.onCraftedBy($$0, $$1.getCount());
      this.resultSlots.awardUsedRecipes($$0, this.getRelevantItems());
      this.shrinkStackInSlot(0);
      this.shrinkStackInSlot(1);
      this.shrinkStackInSlot(2);
      this.access.execute(($$0x, $$1x) -> $$0x.levelEvent(1044, $$1x, 0));
   }

   private List<ItemStack> getRelevantItems() {
      return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
   }

   private SmithingRecipeInput createRecipeInput() {
      return new SmithingRecipeInput(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
   }

   private void shrinkStackInSlot(int $$0) {
      ItemStack $$1 = this.inputSlots.getItem($$0);
      if (!$$1.isEmpty()) {
         $$1.shrink(1);
         this.inputSlots.setItem($$0, $$1);
      }
   }

   @Override
   public void slotsChanged(Container $$0) {
      super.slotsChanged($$0);
      if (this.level instanceof ServerLevel) {
         boolean $$1 = this.getSlot(0).hasItem() && this.getSlot(1).hasItem() && this.getSlot(2).hasItem() && !this.getSlot(this.getResultSlot()).hasItem();
         this.hasRecipeError.set($$1 ? 1 : 0);
      }
   }

   @Override
   public void createResult() {
      SmithingRecipeInput $$0 = this.createRecipeInput();
      Optional<RecipeHolder<SmithingRecipe>> $$2;
      if (this.level instanceof ServerLevel $$1) {
         $$2 = $$1.recipeAccess().getRecipeFor(RecipeType.SMITHING, $$0, $$1);
      } else {
         $$2 = Optional.empty();
      }

      $$2.ifPresentOrElse($$1x -> {
         ItemStack $$2x = ((SmithingRecipe)$$1x.value()).assemble($$0, this.level.registryAccess());
         this.resultSlots.setRecipeUsed($$1x);
         this.resultSlots.setItem(0, $$2x);
      }, () -> {
         this.resultSlots.setRecipeUsed(null);
         this.resultSlots.setItem(0, ItemStack.EMPTY);
      });
   }

   @Override
   public boolean canTakeItemForPickAll(ItemStack $$0, net.minecraft.world.inventory.Slot $$1) {
      return $$1.container != this.resultSlots && super.canTakeItemForPickAll($$0, $$1);
   }

   @Override
   public boolean canMoveIntoInputSlots(ItemStack $$0) {
      if (this.templateItemTest.test($$0) && !this.getSlot(0).hasItem()) {
         return true;
      } else {
         return this.baseItemTest.test($$0) && !this.getSlot(1).hasItem() ? true : this.additionItemTest.test($$0) && !this.getSlot(2).hasItem();
      }
   }

   public boolean hasRecipeError() {
      return this.hasRecipeError.get() > 0;
   }
}
