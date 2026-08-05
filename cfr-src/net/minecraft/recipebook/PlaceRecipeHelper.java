/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public interface PlaceRecipeHelper {
    public static <T> void placeRecipe(int $$0, int $$1, Recipe<?> $$2, Iterable<T> $$3, Output<T> $$4) {
        if ($$2 instanceof ShapedRecipe) {
            ShapedRecipe $$5 = (ShapedRecipe)$$2;
            PlaceRecipeHelper.placeRecipe($$0, $$1, $$5.getWidth(), $$5.getHeight(), $$3, $$4);
        } else {
            PlaceRecipeHelper.placeRecipe($$0, $$1, $$0, $$1, $$3, $$4);
        }
    }

    public static <T> void placeRecipe(int $$0, int $$1, int $$2, int $$3, Iterable<T> $$4, Output<T> $$5) {
        Iterator<T> $$6 = $$4.iterator();
        int $$7 = 0;
        block0: for (int $$8 = 0; $$8 < $$1; ++$$8) {
            boolean $$9 = (float)$$3 < (float)$$1 / 2.0f;
            int $$10 = Mth.floor((float)$$1 / 2.0f - (float)$$3 / 2.0f);
            if ($$9 && $$10 > $$8) {
                $$7 += $$0;
                ++$$8;
            }
            for (int $$11 = 0; $$11 < $$0; ++$$11) {
                boolean $$13;
                if (!$$6.hasNext()) {
                    return;
                }
                $$9 = (float)$$2 < (float)$$0 / 2.0f;
                $$10 = Mth.floor((float)$$0 / 2.0f - (float)$$2 / 2.0f);
                int $$12 = $$2;
                boolean bl = $$13 = $$11 < $$2;
                if ($$9) {
                    $$12 = $$10 + $$2;
                    boolean bl2 = $$13 = $$10 <= $$11 && $$11 < $$10 + $$2;
                }
                if ($$13) {
                    $$5.addItemToSlot($$6.next(), $$7, $$11, $$8);
                } else if ($$12 == $$11) {
                    $$7 += $$0 - $$11;
                    continue block0;
                }
                ++$$7;
            }
        }
    }

    @FunctionalInterface
    public static interface Output<T> {
        public void addItemToSlot(T var1, int var2, int var3, int var4);
    }
}

