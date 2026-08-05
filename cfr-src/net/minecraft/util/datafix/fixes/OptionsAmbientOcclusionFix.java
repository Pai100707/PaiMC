/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class OptionsAmbientOcclusionFix
extends DataFix {
    public OptionsAmbientOcclusionFix(Schema $$0) {
        super($$0, false);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsAmbientOcclusionFix", this.getInputSchema().getType(References.OPTIONS), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> (Dynamic)DataFixUtils.orElse((Optional)$$0.get("ao").asString().map($$1 -> $$0.set("ao", $$0.createString(OptionsAmbientOcclusionFix.updateValue($$1)))).result(), (Object)$$0)));
    }

    private static String updateValue(String $$0) {
        return switch ($$0) {
            case "0" -> "false";
            case "1", "2" -> "true";
            default -> $$0;
        };
    }
}

