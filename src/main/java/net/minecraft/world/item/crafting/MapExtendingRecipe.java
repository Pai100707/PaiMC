package net.minecraft.world.item.crafting;

import java.util.Map;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapExtendingRecipe extends ShapedRecipe {
   public MapExtendingRecipe(CraftingBookCategory $$0) {
      super(
         "",
         $$0,
         ShapedRecipePattern.of(
            Map.of('#', Ingredient.of(net.minecraft.world.item.Items.PAPER), 'x', Ingredient.of(net.minecraft.world.item.Items.FILLED_MAP)),
            "###",
            "#x#",
            "###"
         ),
         new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.MAP)
      );
   }

   @Override
   public boolean matches(CraftingInput $$0, Level $$1) {
      if (!super.matches($$0, $$1)) {
         return false;
      } else {
         net.minecraft.world.item.ItemStack $$2 = findFilledMap($$0);
         if ($$2.isEmpty()) {
            return false;
         } else {
            MapItemSavedData $$3 = net.minecraft.world.item.MapItem.getSavedData($$2, $$1);
            if ($$3 == null) {
               return false;
            } else {
               return $$3.isExplorationMap() ? false : $$3.scale < 4;
            }
         }
      }
   }

   @Override
   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      net.minecraft.world.item.ItemStack $$2 = findFilledMap($$0).copyWithCount(1);
      $$2.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.SCALE);
      return $$2;
   }

   private static net.minecraft.world.item.ItemStack findFilledMap(CraftingInput $$0) {
      for (int $$1 = 0; $$1 < $$0.size(); $$1++) {
         net.minecraft.world.item.ItemStack $$2 = $$0.getItem($$1);
         if ($$2.has(DataComponents.MAP_ID)) {
            return $$2;
         }
      }

      return net.minecraft.world.item.ItemStack.EMPTY;
   }

   @Override
   public boolean isSpecial() {
      return true;
   }

   @Override
   public RecipeSerializer<MapExtendingRecipe> getSerializer() {
      return RecipeSerializer.MAP_EXTENDING;
   }
}
