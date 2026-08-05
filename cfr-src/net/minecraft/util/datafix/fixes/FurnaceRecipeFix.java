/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Dynamic
 */
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
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class FurnaceRecipeFix
extends DataFix {
    public FurnaceRecipeFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    protected TypeRewriteRule makeRule() {
        return this.cap(this.getOutputSchema().getTypeRaw(References.RECIPE));
    }

    private <R> TypeRewriteRule cap(Type<R> $$0) {
        Type $$1 = DSL.and((Type)DSL.optional((Type)DSL.field((String)"RecipesUsed", (Type)DSL.and((Type)DSL.compoundList($$0, (Type)DSL.intType()), (Type)DSL.remainderType()))), (Type)DSL.remainderType());
        OpticFinder $$2 = DSL.namedChoice((String)"minecraft:furnace", (Type)this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace"));
        OpticFinder $$3 = DSL.namedChoice((String)"minecraft:blast_furnace", (Type)this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace"));
        OpticFinder $$4 = DSL.namedChoice((String)"minecraft:smoker", (Type)this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker"));
        Type $$5 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace");
        Type $$6 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace");
        Type $$7 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker");
        Type $$82 = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type $$9 = this.getOutputSchema().getType(References.BLOCK_ENTITY);
        return this.fixTypeEverywhereTyped("FurnaceRecipesFix", $$82, $$9, $$8 -> $$8.updateTyped($$2, $$5, $$2 -> this.updateFurnaceContents($$0, (Type)$$1, (Typed<?>)$$2)).updateTyped($$3, $$6, $$2 -> this.updateFurnaceContents($$0, (Type)$$1, (Typed<?>)$$2)).updateTyped($$4, $$7, $$2 -> this.updateFurnaceContents($$0, (Type)$$1, (Typed<?>)$$2)));
    }

    private <R> Typed<?> updateFurnaceContents(Type<R> $$0, Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> $$1, Typed<?> $$2) {
        Dynamic $$32 = (Dynamic)$$2.getOrCreate(DSL.remainderFinder());
        int $$4 = $$32.get("RecipesUsedSize").asInt(0);
        $$32 = $$32.remove("RecipesUsedSize");
        ArrayList $$5 = Lists.newArrayList();
        for (int $$6 = 0; $$6 < $$4; ++$$6) {
            String $$7 = "RecipeLocation" + $$6;
            String $$8 = "RecipeAmount" + $$6;
            Optional $$9 = $$32.get($$7).result();
            int $$10 = $$32.get($$8).asInt(0);
            if ($$10 > 0) {
                $$9.ifPresent($$3 -> {
                    Optional $$4 = $$0.read($$3).result();
                    $$4.ifPresent($$2 -> $$5.add(Pair.of((Object)$$2.getFirst(), (Object)$$10)));
                });
            }
            $$32 = $$32.remove($$7).remove($$8);
        }
        return $$2.set(DSL.remainderFinder(), $$1, (Object)Pair.of((Object)Either.left((Object)Pair.of((Object)$$5, (Object)$$32.emptyMap())), (Object)$$32));
    }
}

