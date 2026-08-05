/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.fixes.References;

public class OptionsKeyTranslationFix
extends DataFix {
    public OptionsKeyTranslationFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsKeyTranslationFix", this.getInputSchema().getType(References.OPTIONS), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.getMapValues().map($$12 -> $$0.createMap($$12.entrySet().stream().map($$1 -> {
            String $$2;
            if (((Dynamic)$$1.getKey()).asString("").startsWith("key_") && !($$2 = ((Dynamic)$$1.getValue()).asString("")).startsWith("key.mouse") && !$$2.startsWith("scancode.")) {
                return Pair.of((Object)((Dynamic)$$1.getKey()), (Object)$$0.createString("key.keyboard." + $$2.substring("key.".length())));
            }
            return Pair.of((Object)((Dynamic)$$1.getKey()), (Object)((Dynamic)$$1.getValue()));
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))).result().orElse($$0)));
    }
}

