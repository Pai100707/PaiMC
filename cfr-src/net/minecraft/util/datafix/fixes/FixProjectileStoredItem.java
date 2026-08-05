/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class FixProjectileStoredItem
extends DataFix {
    private static final String EMPTY_POTION = "minecraft:empty";

    public FixProjectileStoredItem(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ENTITY);
        Type $$1 = this.getOutputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped("Fix AbstractArrow item type", $$0, $$1, ExtraDataFixUtils.chainAllFilters(this.fixChoice("minecraft:trident", FixProjectileStoredItem::castUnchecked), this.fixChoice("minecraft:arrow", FixProjectileStoredItem::fixArrow), this.fixChoice("minecraft:spectral_arrow", FixProjectileStoredItem::fixSpectralArrow)));
    }

    private Function<Typed<?>, Typed<?>> fixChoice(String $$0, SubFixer<?> $$1) {
        Type $$2 = this.getInputSchema().getChoiceType(References.ENTITY, $$0);
        Type $$3 = this.getOutputSchema().getChoiceType(References.ENTITY, $$0);
        return FixProjectileStoredItem.fixChoiceCap($$0, $$1, $$2, $$3);
    }

    private static <T> Function<Typed<?>, Typed<?>> fixChoiceCap(String $$0, SubFixer<?> $$1, Type<?> $$2, Type<T> $$32) {
        OpticFinder $$4 = DSL.namedChoice((String)$$0, $$2);
        SubFixer<?> $$5 = $$1;
        return $$3 -> $$3.updateTyped($$4, $$32, $$2 -> $$5.fix((Typed<?>)$$2, $$32));
    }

    private static <T> Typed<T> fixArrow(Typed<?> $$02, Type<T> $$1) {
        return Util.writeAndReadTypedOrThrow($$02, $$1, $$0 -> $$0.set("item", FixProjectileStoredItem.createItemStack($$0, FixProjectileStoredItem.getArrowType($$0))));
    }

    private static String getArrowType(Dynamic<?> $$0) {
        return $$0.get("Potion").asString(EMPTY_POTION).equals(EMPTY_POTION) ? "minecraft:arrow" : "minecraft:tipped_arrow";
    }

    private static <T> Typed<T> fixSpectralArrow(Typed<?> $$02, Type<T> $$1) {
        return Util.writeAndReadTypedOrThrow($$02, $$1, $$0 -> $$0.set("item", FixProjectileStoredItem.createItemStack($$0, "minecraft:spectral_arrow")));
    }

    private static Dynamic<?> createItemStack(Dynamic<?> $$0, String $$1) {
        return $$0.createMap((Map)ImmutableMap.of((Object)$$0.createString("id"), (Object)$$0.createString($$1), (Object)$$0.createString("Count"), (Object)$$0.createInt(1)));
    }

    private static <T> Typed<T> castUnchecked(Typed<?> $$0, Type<T> $$1) {
        return new Typed($$1, $$0.getOps(), $$0.getValue());
    }

    static interface SubFixer<F> {
        public Typed<F> fix(Typed<?> var1, Type<F> var2);
    }
}

