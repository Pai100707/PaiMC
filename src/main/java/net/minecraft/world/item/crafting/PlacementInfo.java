package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlacementInfo {
   public static final int EMPTY_SLOT = -1;
   public static final PlacementInfo NOT_PLACEABLE = new PlacementInfo(List.of(), IntList.of());
   private final List<Ingredient> ingredients;
   private final IntList slotsToIngredientIndex;

   private PlacementInfo(List<Ingredient> $$0, IntList $$1) {
      this.ingredients = $$0;
      this.slotsToIngredientIndex = $$1;
   }

   public static PlacementInfo create(Ingredient $$0) {
      return $$0.isEmpty() ? NOT_PLACEABLE : new PlacementInfo(List.of($$0), IntList.of(0));
   }

   public static PlacementInfo createFromOptionals(List<Optional<Ingredient>> $$0) {
      int $$1 = $$0.size();
      List<Ingredient> $$2 = new ArrayList<>($$1);
      IntList $$3 = new IntArrayList($$1);
      int $$4 = 0;

      for (Optional<Ingredient> $$5 : $$0) {
         if ($$5.isPresent()) {
            Ingredient $$6 = $$5.get();
            if ($$6.isEmpty()) {
               return NOT_PLACEABLE;
            }

            $$2.add($$6);
            $$3.add($$4++);
         } else {
            $$3.add(-1);
         }
      }

      return new PlacementInfo($$2, $$3);
   }

   public static PlacementInfo create(List<Ingredient> $$0) {
      int $$1 = $$0.size();
      IntList $$2 = new IntArrayList($$1);

      for (int $$3 = 0; $$3 < $$1; $$3++) {
         Ingredient $$4 = $$0.get($$3);
         if ($$4.isEmpty()) {
            return NOT_PLACEABLE;
         }

         $$2.add($$3);
      }

      return new PlacementInfo($$0, $$2);
   }

   public IntList slotsToIngredientIndex() {
      return this.slotsToIngredientIndex;
   }

   public List<Ingredient> ingredients() {
      return this.ingredients;
   }

   public boolean isImpossibleToPlace() {
      return this.slotsToIngredientIndex.isEmpty();
   }
}
