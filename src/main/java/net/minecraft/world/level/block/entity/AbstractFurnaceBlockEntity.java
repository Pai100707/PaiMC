package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap.Entry;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeManager.CachedCheck;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible {
   protected static final int SLOT_INPUT = 0;
   protected static final int SLOT_FUEL = 1;
   protected static final int SLOT_RESULT = 2;
   public static final int DATA_LIT_TIME = 0;
   private static final int[] SLOTS_FOR_UP = new int[]{0};
   private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
   private static final int[] SLOTS_FOR_SIDES = new int[]{1};
   public static final int DATA_LIT_DURATION = 1;
   public static final int DATA_COOKING_PROGRESS = 2;
   public static final int DATA_COOKING_TOTAL_TIME = 3;
   public static final int NUM_DATA_VALUES = 4;
   public static final int BURN_TIME_STANDARD = 200;
   public static final int BURN_COOL_SPEED = 2;
   private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);
   private static final short DEFAULT_COOKING_TIMER = 0;
   private static final short DEFAULT_COOKING_TOTAL_TIME = 0;
   private static final short DEFAULT_LIT_TIME_REMAINING = 0;
   private static final short DEFAULT_LIT_TOTAL_TIME = 0;
   protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
   int litTimeRemaining;
   int litTotalTime;
   int cookingTimer;
   int cookingTotalTime;
   protected final ContainerData dataAccess = new ContainerData() {
      public int get(int $$0) {
         switch ($$0) {
            case 0:
               return AbstractFurnaceBlockEntity.this.litTimeRemaining;
            case 1:
               return AbstractFurnaceBlockEntity.this.litTotalTime;
            case 2:
               return AbstractFurnaceBlockEntity.this.cookingTimer;
            case 3:
               return AbstractFurnaceBlockEntity.this.cookingTotalTime;
            default:
               return 0;
         }
      }

      public void set(int $$0, int $$1) {
         switch ($$0) {
            case 0:
               AbstractFurnaceBlockEntity.this.litTimeRemaining = $$1;
               break;
            case 1:
               AbstractFurnaceBlockEntity.this.litTotalTime = $$1;
               break;
            case 2:
               AbstractFurnaceBlockEntity.this.cookingTimer = $$1;
               break;
            case 3:
               AbstractFurnaceBlockEntity.this.cookingTotalTime = $$1;
         }
      }

      public int getCount() {
         return 4;
      }
   };
   private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap();
   private final CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;

   protected AbstractFurnaceBlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2, RecipeType<? extends AbstractCookingRecipe> $$3) {
      super($$0, $$1, $$2);
      this.quickCheck = RecipeManager.createCheck($$3);
   }

   private boolean isLit() {
      return this.litTimeRemaining > 0;
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      ContainerHelper.loadAllItems($$0, this.items);
      this.cookingTimer = $$0.getShortOr("cooking_time_spent", (short)0);
      this.cookingTotalTime = $$0.getShortOr("cooking_total_time", (short)0);
      this.litTimeRemaining = $$0.getShortOr("lit_time_remaining", (short)0);
      this.litTotalTime = $$0.getShortOr("lit_total_time", (short)0);
      this.recipesUsed.clear();
      this.recipesUsed.putAll($$0.<Map>read("RecipesUsed", RECIPES_USED_CODEC).orElse(Map.of()));
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.putShort("cooking_time_spent", (short)this.cookingTimer);
      $$0.putShort("cooking_total_time", (short)this.cookingTotalTime);
      $$0.putShort("lit_time_remaining", (short)this.litTimeRemaining);
      $$0.putShort("lit_total_time", (short)this.litTotalTime);
      ContainerHelper.saveAllItems($$0, this.items);
      $$0.store("RecipesUsed", RECIPES_USED_CODEC, this.recipesUsed);
   }

   public static void serverTick(ServerLevel $$0, BlockPos $$1, BlockState $$2, AbstractFurnaceBlockEntity $$3) {
      boolean $$4 = $$3.isLit();
      boolean $$5 = false;
      if ($$3.isLit()) {
         $$3.litTimeRemaining--;
      }

      ItemStack $$6 = (ItemStack)$$3.items.get(1);
      ItemStack $$7 = (ItemStack)$$3.items.get(0);
      boolean $$8 = !$$7.isEmpty();
      boolean $$9 = !$$6.isEmpty();
      if ($$3.isLit() || $$9 && $$8) {
         SingleRecipeInput $$10 = new SingleRecipeInput($$7);
         RecipeHolder<? extends AbstractCookingRecipe> $$11;
         if ($$8) {
            $$11 = (RecipeHolder<? extends AbstractCookingRecipe>)$$3.quickCheck.getRecipeFor($$10, $$0).orElse(null);
         } else {
            $$11 = null;
         }

         int $$13 = $$3.getMaxStackSize();
         if (!$$3.isLit() && canBurn($$0.registryAccess(), $$11, $$10, $$3.items, $$13)) {
            $$3.litTimeRemaining = $$3.getBurnDuration($$0.fuelValues(), $$6);
            $$3.litTotalTime = $$3.litTimeRemaining;
            if ($$3.isLit()) {
               $$5 = true;
               if ($$9) {
                  Item $$14 = $$6.getItem();
                  $$6.shrink(1);
                  if ($$6.isEmpty()) {
                     $$3.items.set(1, $$14.getCraftingRemainder());
                  }
               }
            }
         }

         if ($$3.isLit() && canBurn($$0.registryAccess(), $$11, $$10, $$3.items, $$13)) {
            $$3.cookingTimer++;
            if ($$3.cookingTimer == $$3.cookingTotalTime) {
               $$3.cookingTimer = 0;
               $$3.cookingTotalTime = getTotalCookTime($$0, $$3);
               if (burn($$0.registryAccess(), $$11, $$10, $$3.items, $$13)) {
                  $$3.setRecipeUsed($$11);
               }

               $$5 = true;
            }
         } else {
            $$3.cookingTimer = 0;
         }
      } else if (!$$3.isLit() && $$3.cookingTimer > 0) {
         $$3.cookingTimer = Mth.clamp($$3.cookingTimer - 2, 0, $$3.cookingTotalTime);
      }

      if ($$4 != $$3.isLit()) {
         $$5 = true;
         $$2 = $$2.setValue(AbstractFurnaceBlock.LIT, $$3.isLit());
         $$0.setBlock($$1, $$2, 3);
      }

      if ($$5) {
         setChanged($$0, $$1, $$2);
      }
   }

   private static boolean canBurn(
      RegistryAccess $$0, RecipeHolder<? extends AbstractCookingRecipe> $$1, SingleRecipeInput $$2, NonNullList<ItemStack> $$3, int $$4
   ) {
      if (!((ItemStack)$$3.get(0)).isEmpty() && $$1 != null) {
         ItemStack $$5 = ((AbstractCookingRecipe)$$1.value()).assemble($$2, $$0);
         if ($$5.isEmpty()) {
            return false;
         } else {
            ItemStack $$6 = (ItemStack)$$3.get(2);
            if ($$6.isEmpty()) {
               return true;
            } else if (!ItemStack.isSameItemSameComponents($$6, $$5)) {
               return false;
            } else {
               return $$6.getCount() < $$4 && $$6.getCount() < $$6.getMaxStackSize() ? true : $$6.getCount() < $$5.getMaxStackSize();
            }
         }
      } else {
         return false;
      }
   }

   private static boolean burn(
      RegistryAccess $$0, RecipeHolder<? extends AbstractCookingRecipe> $$1, SingleRecipeInput $$2, NonNullList<ItemStack> $$3, int $$4
   ) {
      if ($$1 != null && canBurn($$0, $$1, $$2, $$3, $$4)) {
         ItemStack $$5 = (ItemStack)$$3.get(0);
         ItemStack $$6 = ((AbstractCookingRecipe)$$1.value()).assemble($$2, $$0);
         ItemStack $$7 = (ItemStack)$$3.get(2);
         if ($$7.isEmpty()) {
            $$3.set(2, $$6.copy());
         } else if (ItemStack.isSameItemSameComponents($$7, $$6)) {
            $$7.grow(1);
         }

         if ($$5.is(Blocks.WET_SPONGE.asItem()) && !((ItemStack)$$3.get(1)).isEmpty() && ((ItemStack)$$3.get(1)).is(Items.BUCKET)) {
            $$3.set(1, new ItemStack(Items.WATER_BUCKET));
         }

         $$5.shrink(1);
         return true;
      } else {
         return false;
      }
   }

   protected int getBurnDuration(FuelValues $$0, ItemStack $$1) {
      return $$0.burnDuration($$1);
   }

   private static int getTotalCookTime(ServerLevel $$0, AbstractFurnaceBlockEntity $$1) {
      SingleRecipeInput $$2 = new SingleRecipeInput($$1.getItem(0));
      return $$1.quickCheck.getRecipeFor($$2, $$0).map($$0x -> ((AbstractCookingRecipe)$$0x.value()).cookingTime()).orElse(200);
   }

   public int[] getSlotsForFace(Direction $$0) {
      if ($$0 == Direction.DOWN) {
         return SLOTS_FOR_DOWN;
      } else {
         return $$0 == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
      }
   }

   public boolean canPlaceItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
      return this.canPlaceItem($$0, $$1);
   }

   public boolean canTakeItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
      return $$2 == Direction.DOWN && $$0 == 1 ? $$1.is(Items.WATER_BUCKET) || $$1.is(Items.BUCKET) : true;
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

   @Override
   public void setItem(int $$0, ItemStack $$1) {
      ItemStack $$2 = (ItemStack)this.items.get($$0);
      boolean $$3 = !$$1.isEmpty() && ItemStack.isSameItemSameComponents($$2, $$1);
      this.items.set($$0, $$1);
      $$1.limitSize(this.getMaxStackSize($$1));
      if ($$0 == 0 && !$$3 && this.level instanceof ServerLevel $$4) {
         this.cookingTotalTime = getTotalCookTime($$4, this);
         this.cookingTimer = 0;
         this.setChanged();
      }
   }

   public boolean canPlaceItem(int $$0, ItemStack $$1) {
      if ($$0 == 2) {
         return false;
      } else if ($$0 != 1) {
         return true;
      } else {
         ItemStack $$2 = (ItemStack)this.items.get(1);
         return this.level.fuelValues().isFuel($$1) || $$1.is(Items.BUCKET) && !$$2.is(Items.BUCKET);
      }
   }

   public void setRecipeUsed(RecipeHolder<?> $$0) {
      if ($$0 != null) {
         ResourceKey<Recipe<?>> $$1 = $$0.id();
         this.recipesUsed.addTo($$1, 1);
      }
   }

   
   public RecipeHolder<?> getRecipeUsed() {
      return null;
   }

   public void awardUsedRecipes(Player $$0, List<ItemStack> $$1) {
   }

   public void awardUsedRecipesAndPopExperience(ServerPlayer $$0) {
      List<RecipeHolder<?>> $$1 = this.getRecipesToAwardAndPopExperience($$0.level(), $$0.position());
      $$0.awardRecipes($$1);

      for (RecipeHolder<?> $$2 : $$1) {
         $$0.triggerRecipeCrafted($$2, this.items);
      }

      this.recipesUsed.clear();
   }

   public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel $$0, Vec3 $$1) {
      List<RecipeHolder<?>> $$2 = Lists.newArrayList();
      ObjectIterator var4 = this.recipesUsed.reference2IntEntrySet().iterator();

      while (var4.hasNext()) {
         Entry<ResourceKey<Recipe<?>>> $$3 = (Entry<ResourceKey<Recipe<?>>>)var4.next();
         $$0.recipeAccess().byKey((ResourceKey)$$3.getKey()).ifPresent($$4 -> {
            $$2.add((RecipeHolder<?>)$$4);
            createExperience($$0, $$1, $$3.getIntValue(), ((AbstractCookingRecipe)$$4.value()).experience());
         });
      }

      return $$2;
   }

   private static void createExperience(ServerLevel $$0, Vec3 $$1, int $$2, float $$3) {
      int $$4 = Mth.floor($$2 * $$3);
      float $$5 = Mth.frac($$2 * $$3);
      if ($$5 != 0.0F && $$0.random.nextFloat() < $$5) {
         $$4++;
      }

      ExperienceOrb.award($$0, $$1, $$4);
   }

   public void fillStackedContents(StackedItemContents $$0) {
      for (ItemStack $$1 : this.items) {
         $$0.accountStack($$1);
      }
   }

   @Override
   public void preRemoveSideEffects(BlockPos $$0, BlockState $$1) {
      super.preRemoveSideEffects($$0, $$1);
      if (this.level instanceof ServerLevel $$2) {
         this.getRecipesToAwardAndPopExperience($$2, Vec3.atCenterOf($$0));
      }
   }
}
