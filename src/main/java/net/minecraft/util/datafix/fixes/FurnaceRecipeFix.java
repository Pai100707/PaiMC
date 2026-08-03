package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;

public class FurnaceRecipeFix extends DataFix {
   public FurnaceRecipeFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   protected TypeRewriteRule makeRule() {
      return this.cap(this.getOutputSchema().getTypeRaw(References.RECIPE));
   }

   private <R> TypeRewriteRule cap(Type<R> $$0) {
      Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, com.mojang.datafixers.util.Unit>, Dynamic<?>>> $$1 = DSL.and(
         DSL.optional(DSL.field("RecipesUsed", DSL.and(DSL.compoundList($$0, DSL.intType()), DSL.remainderType()))), DSL.remainderType()
      );
      OpticFinder<?> $$2 = DSL.namedChoice("minecraft:furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace"));
      OpticFinder<?> $$3 = DSL.namedChoice("minecraft:blast_furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace"));
      OpticFinder<?> $$4 = DSL.namedChoice("minecraft:smoker", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker"));
      Type<?> $$5 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace");
      Type<?> $$6 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace");
      Type<?> $$7 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker");
      Type<?> $$8 = this.getInputSchema().getType(References.BLOCK_ENTITY);
      Type<?> $$9 = this.getOutputSchema().getType(References.BLOCK_ENTITY);
      return this.fixTypeEverywhereTyped(
         "FurnaceRecipesFix",
         $$8,
         $$9,
         $$8x -> $$8x.updateTyped($$2, $$5, $$2xx -> this.updateFurnaceContents($$0, $$1, $$2xx))
            .updateTyped($$3, $$6, $$2xx -> this.updateFurnaceContents($$0, $$1, $$2xx))
            .updateTyped($$4, $$7, $$2xx -> this.updateFurnaceContents($$0, $$1, $$2xx))
      );
   }

   private <R> Typed<?> updateFurnaceContents(
      Type<R> $$0, Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, com.mojang.datafixers.util.Unit>, Dynamic<?>>> $$1, Typed<?> $$2
   ) {
      Dynamic<?> $$3 = (Dynamic<?>)$$2.getOrCreate(DSL.remainderFinder());
      int $$4 = $$3.get("RecipesUsedSize").asInt(0);
      $$3 = $$3.remove("RecipesUsedSize");
      List<Pair<R, Integer>> $$5 = Lists.newArrayList();

      for (int $$6 = 0; $$6 < $$4; $$6++) {
         String $$7 = "RecipeLocation" + $$6;
         String $$8 = "RecipeAmount" + $$6;
         Optional<? extends Dynamic<?>> $$9 = $$3.get($$7).result();
         int $$10 = $$3.get($$8).asInt(0);
         if ($$10 > 0) {
            $$9.ifPresent($$3x -> {
               Optional<? extends Pair<R, ? extends Dynamic<?>>> $$4x = $$0.read($$3x).result();
               $$4x.ifPresent($$2xx -> $$5.add(Pair.of($$2xx.getFirst(), $$10)));
            });
         }

         $$3 = $$3.remove($$7).remove($$8);
      }

      return $$2.set(DSL.remainderFinder(), $$1, Pair.of(Either.left(Pair.of($$5, $$3.emptyMap())), $$3));
   }
}
