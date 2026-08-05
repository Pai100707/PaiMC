/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class VillagerTradeFix
extends DataFix {
    public VillagerTradeFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.VILLAGER_TRADE);
        OpticFinder $$12 = $$0.findField("buy");
        OpticFinder $$2 = $$0.findField("buyB");
        OpticFinder $$3 = $$0.findField("sell");
        OpticFinder $$42 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        Function<Typed, Typed> $$5 = $$1 -> this.updateItemStack((OpticFinder<Pair<String, String>>)$$42, (Typed<?>)$$1);
        return this.fixTypeEverywhereTyped("Villager trade fix", $$0, $$4 -> $$4.updateTyped($$12, $$5).updateTyped($$2, $$5).updateTyped($$3, $$5));
    }

    private Typed<?> updateItemStack(OpticFinder<Pair<String, String>> $$0, Typed<?> $$1) {
        return $$1.update($$0, $$02 -> $$02.mapSecond($$0 -> Objects.equals($$0, "minecraft:carved_pumpkin") ? "minecraft:pumpkin" : $$0));
    }
}

