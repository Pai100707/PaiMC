package net.minecraft.world.entity.player;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap.Entry;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class StackedContents<T> {
   public final Reference2IntOpenHashMap<T> amounts = new Reference2IntOpenHashMap();

   boolean hasAtLeast(T $$0, int $$1) {
      return this.amounts.getInt($$0) >= $$1;
   }

   void take(T $$0, int $$1) {
      int $$2 = this.amounts.addTo($$0, -$$1);
      if ($$2 < $$1) {
         throw new IllegalStateException("Took " + $$1 + " items, but only had " + $$2);
      }
   }

   void put(T $$0, int $$1) {
      this.amounts.addTo($$0, $$1);
   }

   public boolean tryPick(List<? extends StackedContents.IngredientInfo<T>> $$0, int $$1, @Nullable StackedContents.Output<T> $$2) {
      return new StackedContents.RecipePicker($$0).tryPick($$1, $$2);
   }

   public int tryPickAll(List<? extends StackedContents.IngredientInfo<T>> $$0, int $$1, @Nullable StackedContents.Output<T> $$2) {
      return new StackedContents.RecipePicker($$0).tryPickAll($$1, $$2);
   }

   public void clear() {
      this.amounts.clear();
   }

   public void account(T $$0, int $$1) {
      this.put($$0, $$1);
   }

   List<T> getUniqueAvailableIngredientItems(Iterable<? extends StackedContents.IngredientInfo<T>> $$0) {
      List<T> $$1 = new ArrayList<>();
      ObjectIterator var3 = Reference2IntMaps.fastIterable(this.amounts).iterator();

      while (var3.hasNext()) {
         Entry<T> $$2 = (Entry<T>)var3.next();
         if ($$2.getIntValue() > 0 && anyIngredientMatches($$0, (T)$$2.getKey())) {
            $$1.add((T)$$2.getKey());
         }
      }

      return $$1;
   }

   private static <T> boolean anyIngredientMatches(Iterable<? extends StackedContents.IngredientInfo<T>> $$0, T $$1) {
      for (StackedContents.IngredientInfo<T> $$2 : $$0) {
         if ($$2.acceptsItem($$1)) {
            return true;
         }
      }

      return false;
   }

   @VisibleForTesting
   public int getResultUpperBound(List<? extends StackedContents.IngredientInfo<T>> $$0) {
      int $$1 = Integer.MAX_VALUE;
      ObjectIterable<Entry<T>> $$2 = Reference2IntMaps.fastIterable(this.amounts);

      label31:
      for (StackedContents.IngredientInfo<T> $$3 : $$0) {
         int $$4 = 0;
         ObjectIterator var7 = $$2.iterator();

         while (var7.hasNext()) {
            Entry<T> $$5 = (Entry<T>)var7.next();
            int $$6 = $$5.getIntValue();
            if ($$6 > $$4) {
               if ($$3.acceptsItem((T)$$5.getKey())) {
                  $$4 = $$6;
               }

               if ($$4 >= $$1) {
                  continue label31;
               }
            }
         }

         $$1 = $$4;
         if ($$4 == 0) {
            break;
         }
      }

      return $$1;
   }

   @FunctionalInterface
   public interface IngredientInfo<T> {
      boolean acceptsItem(T var1);
   }

   @FunctionalInterface
   public interface Output<T> {
      void accept(T var1);
   }

   class RecipePicker {
      private final List<? extends StackedContents.IngredientInfo<T>> ingredients;
      private final int ingredientCount;
      private final List<T> items;
      private final int itemCount;
      private final BitSet data;
      private final IntList path = new IntArrayList();

      public RecipePicker(final List<? extends StackedContents.IngredientInfo<T>> $$0) {
         this.ingredients = $$0;
         this.ingredientCount = $$0.size();
         this.items = StackedContents.this.getUniqueAvailableIngredientItems($$0);
         this.itemCount = this.items.size();
         this.data = new BitSet(this.visitedIngredientCount() + this.visitedItemCount() + this.satisfiedCount() + this.connectionCount() + this.residualCount());
         this.setInitialConnections();
      }

      private void setInitialConnections() {
         for (int $$0 = 0; $$0 < this.ingredientCount; $$0++) {
            StackedContents.IngredientInfo<T> $$1 = (StackedContents.IngredientInfo<T>)this.ingredients.get($$0);

            for (int $$2 = 0; $$2 < this.itemCount; $$2++) {
               if ($$1.acceptsItem(this.items.get($$2))) {
                  this.setConnection($$2, $$0);
               }
            }
         }
      }

      public boolean tryPick(int $$0, @Nullable StackedContents.Output<T> $$1) {
         if ($$0 <= 0) {
            return true;
         } else {
            int $$2 = 0;

            while (true) {
               IntList $$3 = this.tryAssigningNewItem($$0);
               if ($$3 == null) {
                  boolean $$11 = $$2 == this.ingredientCount;
                  boolean $$12 = $$11 && $$1 != null;
                  this.clearAllVisited();
                  this.clearSatisfied();

                  for (int $$13 = 0; $$13 < this.ingredientCount; $$13++) {
                     for (int $$14 = 0; $$14 < this.itemCount; $$14++) {
                        if (this.isAssigned($$14, $$13)) {
                           this.unassign($$14, $$13);
                           StackedContents.this.put(this.items.get($$14), $$0);
                           if ($$12) {
                              $$1.accept(this.items.get($$14));
                           }
                           break;
                        }
                     }
                  }

                  assert this.data.get(this.residualOffset(), this.residualOffset() + this.residualCount()).isEmpty();

                  return $$11;
               }

               int $$4 = $$3.getInt(0);
               StackedContents.this.take(this.items.get($$4), $$0);
               int $$5 = $$3.size() - 1;
               this.setSatisfied($$3.getInt($$5));
               $$2++;

               for (int $$6 = 0; $$6 < $$3.size() - 1; $$6++) {
                  if (isPathIndexItem($$6)) {
                     int $$7 = $$3.getInt($$6);
                     int $$8 = $$3.getInt($$6 + 1);
                     this.assign($$7, $$8);
                  } else {
                     int $$9 = $$3.getInt($$6 + 1);
                     int $$10 = $$3.getInt($$6);
                     this.unassign($$9, $$10);
                  }
               }
            }
         }
      }

      private static boolean isPathIndexItem(int $$0) {
         return ($$0 & 1) == 0;
      }

      @Nullable
      private IntList tryAssigningNewItem(int $$0) {
         this.clearAllVisited();

         for (int $$1 = 0; $$1 < this.itemCount; $$1++) {
            if (StackedContents.this.hasAtLeast(this.items.get($$1), $$0)) {
               IntList $$2 = this.findNewItemAssignmentPath($$1);
               if ($$2 != null) {
                  return $$2;
               }
            }
         }

         return null;
      }

      @Nullable
      private IntList findNewItemAssignmentPath(int $$0) {
         this.path.clear();
         this.visitItem($$0);
         this.path.add($$0);

         while (!this.path.isEmpty()) {
            int $$1 = this.path.size();
            if (isPathIndexItem($$1 - 1)) {
               int $$2 = this.path.getInt($$1 - 1);

               for (int $$3 = 0; $$3 < this.ingredientCount; $$3++) {
                  if (!this.hasVisitedIngredient($$3) && this.hasConnection($$2, $$3) && !this.isAssigned($$2, $$3)) {
                     this.visitIngredient($$3);
                     this.path.add($$3);
                     break;
                  }
               }
            } else {
               int $$4 = this.path.getInt($$1 - 1);
               if (!this.isSatisfied($$4)) {
                  return this.path;
               }

               for (int $$5 = 0; $$5 < this.itemCount; $$5++) {
                  if (!this.hasVisitedItem($$5) && this.isAssigned($$5, $$4)) {
                     assert this.hasConnection($$5, $$4);

                     this.visitItem($$5);
                     this.path.add($$5);
                     break;
                  }
               }
            }

            int $$6 = this.path.size();
            if ($$6 == $$1) {
               this.path.removeInt($$6 - 1);
            }
         }

         return null;
      }

      private int visitedIngredientOffset() {
         return 0;
      }

      private int visitedIngredientCount() {
         return this.ingredientCount;
      }

      private int visitedItemOffset() {
         return this.visitedIngredientOffset() + this.visitedIngredientCount();
      }

      private int visitedItemCount() {
         return this.itemCount;
      }

      private int satisfiedOffset() {
         return this.visitedItemOffset() + this.visitedItemCount();
      }

      private int satisfiedCount() {
         return this.ingredientCount;
      }

      private int connectionOffset() {
         return this.satisfiedOffset() + this.satisfiedCount();
      }

      private int connectionCount() {
         return this.ingredientCount * this.itemCount;
      }

      private int residualOffset() {
         return this.connectionOffset() + this.connectionCount();
      }

      private int residualCount() {
         return this.ingredientCount * this.itemCount;
      }

      private boolean isSatisfied(int $$0) {
         return this.data.get(this.getSatisfiedIndex($$0));
      }

      private void setSatisfied(int $$0) {
         this.data.set(this.getSatisfiedIndex($$0));
      }

      private int getSatisfiedIndex(int $$0) {
         assert $$0 >= 0 && $$0 < this.ingredientCount;

         return this.satisfiedOffset() + $$0;
      }

      private void clearSatisfied() {
         this.clearRange(this.satisfiedOffset(), this.satisfiedCount());
      }

      private void setConnection(int $$0, int $$1) {
         this.data.set(this.getConnectionIndex($$0, $$1));
      }

      private boolean hasConnection(int $$0, int $$1) {
         return this.data.get(this.getConnectionIndex($$0, $$1));
      }

      private int getConnectionIndex(int $$0, int $$1) {
         assert $$0 >= 0 && $$0 < this.itemCount;

         assert $$1 >= 0 && $$1 < this.ingredientCount;

         return this.connectionOffset() + $$0 * this.ingredientCount + $$1;
      }

      private boolean isAssigned(int $$0, int $$1) {
         return this.data.get(this.getResidualIndex($$0, $$1));
      }

      private void assign(int $$0, int $$1) {
         int $$2 = this.getResidualIndex($$0, $$1);

         assert !this.data.get($$2);

         this.data.set($$2);
      }

      private void unassign(int $$0, int $$1) {
         int $$2 = this.getResidualIndex($$0, $$1);

         assert this.data.get($$2);

         this.data.clear($$2);
      }

      private int getResidualIndex(int $$0, int $$1) {
         assert $$0 >= 0 && $$0 < this.itemCount;

         assert $$1 >= 0 && $$1 < this.ingredientCount;

         return this.residualOffset() + $$0 * this.ingredientCount + $$1;
      }

      private void visitIngredient(int $$0) {
         this.data.set(this.getVisitedIngredientIndex($$0));
      }

      private boolean hasVisitedIngredient(int $$0) {
         return this.data.get(this.getVisitedIngredientIndex($$0));
      }

      private int getVisitedIngredientIndex(int $$0) {
         assert $$0 >= 0 && $$0 < this.ingredientCount;

         return this.visitedIngredientOffset() + $$0;
      }

      private void visitItem(int $$0) {
         this.data.set(this.getVisitiedItemIndex($$0));
      }

      private boolean hasVisitedItem(int $$0) {
         return this.data.get(this.getVisitiedItemIndex($$0));
      }

      private int getVisitiedItemIndex(int $$0) {
         assert $$0 >= 0 && $$0 < this.itemCount;

         return this.visitedItemOffset() + $$0;
      }

      private void clearAllVisited() {
         this.clearRange(this.visitedIngredientOffset(), this.visitedIngredientCount());
         this.clearRange(this.visitedItemOffset(), this.visitedItemCount());
      }

      private void clearRange(int $$0, int $$1) {
         this.data.clear($$0, $$0 + $$1);
      }

      public int tryPickAll(int $$0, @Nullable StackedContents.Output<T> $$1) {
         int $$2 = 0;
         int $$3 = Math.min($$0, StackedContents.this.getResultUpperBound(this.ingredients)) + 1;

         while (true) {
            int $$4 = ($$2 + $$3) / 2;
            if (this.tryPick($$4, null)) {
               if ($$3 - $$2 <= 1) {
                  if ($$4 > 0) {
                     this.tryPick($$4, $$1);
                  }

                  return $$4;
               }

               $$2 = $$4;
            } else {
               $$3 = $$4;
            }
         }
      }
   }
}
