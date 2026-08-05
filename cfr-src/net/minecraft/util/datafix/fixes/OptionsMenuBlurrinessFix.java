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

public class OptionsMenuBlurrinessFix
extends DataFix {
    public OptionsMenuBlurrinessFix(Schema $$0) {
        super($$0, false);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsMenuBlurrinessFix", this.getInputSchema().getType(References.OPTIONS), $$0 -> $$0.update(DSL.remainderFinder(), $$02 -> $$02.update("menuBackgroundBlurriness", $$0 -> {
            int $$1 = this.convertToIntRange($$0.asString("0.5"));
            return $$0.createString(String.valueOf($$1));
        })));
    }

    private int convertToIntRange(String $$0) {
        try {
            return Math.round(Float.parseFloat($$0) * 10.0f);
        }
        catch (NumberFormatException $$1) {
            return 5;
        }
    }
}

