/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class OptionsFancyGraphicsToGraphicsModeFix
extends DataFix {
    public OptionsFancyGraphicsToGraphicsModeFix(Schema $$0) {
        super($$0, true);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("fancyGraphics to graphicsMode", this.getInputSchema().getType(References.OPTIONS), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.renameAndFixField("fancyGraphics", "graphicsMode", OptionsFancyGraphicsToGraphicsModeFix::fixGraphicsMode)));
    }

    private static <T> Dynamic<T> fixGraphicsMode(Dynamic<T> $$0) {
        if ("true".equals($$0.asString("true"))) {
            return $$0.createString("1");
        }
        return $$0.createString("0");
    }
}

