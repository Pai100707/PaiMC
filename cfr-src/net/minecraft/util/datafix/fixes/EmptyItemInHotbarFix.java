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

public class EmptyItemInHotbarFix
extends DataFix {
    public EmptyItemInHotbarFix(Schema $$0) {
        super($$0, false);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder $$0 = DSL.typeFinder((Type)this.getInputSchema().getType(References.ITEM_STACK));
        return this.fixTypeEverywhereTyped("EmptyItemInHotbarFix", this.getInputSchema().getType(References.HOTBAR), $$1 -> $$1.update($$0, $$02 -> $$02.mapSecond($$0 -> {
            boolean $$4;
            Object $$1 = ((Either)$$0.getFirst()).left().map(Pair::getSecond);
            Dynamic $$2 = (Dynamic)((Pair)$$0.getSecond()).getSecond();
            boolean $$3 = $$1.isEmpty() || $$1.get().equals("minecraft:air");
            boolean bl = $$4 = $$2.get("Count").asInt(0) <= 0;
            if ($$3 || $$4) {
                return Pair.of((Object)Either.right((Object)Unit.INSTANCE), (Object)Pair.of((Object)Either.right((Object)Unit.INSTANCE), (Object)$$2.emptyMap()));
            }
            return $$0;
        })));
    }
}

