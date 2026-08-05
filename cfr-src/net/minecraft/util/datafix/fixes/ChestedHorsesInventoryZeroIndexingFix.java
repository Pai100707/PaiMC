/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class ChestedHorsesInventoryZeroIndexingFix
extends DataFix {
    public ChestedHorsesInventoryZeroIndexingFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        OpticFinder $$0 = DSL.typeFinder((Type)this.getInputSchema().getType(References.ITEM_STACK));
        Type $$1 = this.getInputSchema().getType(References.ENTITY);
        return TypeRewriteRule.seq((TypeRewriteRule)this.horseLikeInventoryIndexingFixer($$0, $$1, "minecraft:llama"), (TypeRewriteRule[])new TypeRewriteRule[]{this.horseLikeInventoryIndexingFixer($$0, $$1, "minecraft:trader_llama"), this.horseLikeInventoryIndexingFixer($$0, $$1, "minecraft:mule"), this.horseLikeInventoryIndexingFixer($$0, $$1, "minecraft:donkey")});
    }

    private TypeRewriteRule horseLikeInventoryIndexingFixer(OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> $$0, Type<?> $$1, String $$2) {
        Type $$32 = this.getInputSchema().getChoiceType(References.ENTITY, $$2);
        OpticFinder $$4 = DSL.namedChoice((String)$$2, (Type)$$32);
        OpticFinder $$5 = $$32.findField("Items");
        return this.fixTypeEverywhereTyped("Fix non-zero indexing in chest horse type " + $$2, $$1, $$3 -> $$3.updateTyped($$4, $$2 -> $$2.updateTyped($$5, $$1 -> $$1.update($$0, $$0 -> $$0.mapSecond($$02 -> $$02.mapSecond($$0 -> $$0.mapSecond($$02 -> $$02.update("Slot", $$0 -> $$0.createByte((byte)($$0.asInt(2) - 2))))))))));
    }
}

