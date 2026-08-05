/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.References;

public class OptionsSetGraphicsPresetToCustomFix
extends DataFix {
    public OptionsSetGraphicsPresetToCustomFix(Schema $$0) {
        super($$0, true);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("graphicsPreset set to \"custom\"", this.getInputSchema().getType(References.OPTIONS), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.set("graphicsPreset", $$0.createString("custom"))));
    }
}

