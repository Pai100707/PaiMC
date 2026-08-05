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
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class BannerEntityCustomNameToOverrideComponentFix
extends DataFix {
    public BannerEntityCustomNameToOverrideComponentFix(Schema $$0) {
        super($$0, false);
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.BLOCK_ENTITY);
        TaggedChoice.TaggedChoiceType $$1 = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        OpticFinder $$2 = $$0.findField("CustomName");
        OpticFinder $$32 = DSL.typeFinder((Type)this.getInputSchema().getType(References.TEXT_COMPONENT));
        return this.fixTypeEverywhereTyped("Banner entity custom_name to item_name component fix", $$0, $$3 -> {
            Object $$4 = ((Pair)$$3.get($$1.finder())).getFirst();
            return $$4.equals("minecraft:banner") ? this.fix((Typed<?>)$$3, (OpticFinder<Pair<String, String>>)$$32, (OpticFinder<?>)$$2) : $$3;
        });
    }

    private Typed<?> fix(Typed<?> $$02, OpticFinder<Pair<String, String>> $$12, OpticFinder<?> $$2) {
        Optional $$3 = $$02.getOptionalTyped($$2).flatMap($$1 -> $$1.getOptional($$12).map(Pair::getSecond));
        boolean $$4 = $$3.flatMap(LegacyComponentDataFixUtils::extractTranslationString).filter($$0 -> $$0.equals("block.minecraft.ominous_banner")).isPresent();
        if ($$4) {
            return Util.writeAndReadTypedOrThrow($$02, $$02.getType(), $$1 -> {
                Dynamic $$2 = $$1.createMap(Map.of($$1.createString("minecraft:item_name"), $$1.createString((String)$$3.get()), $$1.createString("minecraft:hide_additional_tooltip"), $$1.emptyMap()));
                return $$1.set("components", $$2).remove("CustomName");
            });
        }
        return $$02;
    }
}

