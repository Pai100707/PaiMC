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

public class DebugProfileOverlayReferenceFix
extends DataFix {
    public DebugProfileOverlayReferenceFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("DebugProfileOverlayReferenceFix", this.getInputSchema().getType(References.DEBUG_PROFILE), $$0 -> $$0.update(DSL.remainderFinder(), $$02 -> $$02.update("custom", $$0 -> $$0.updateMapValues($$02 -> $$02.mapSecond($$0 -> {
            if ($$0.asString("").equals("inF3")) {
                return $$0.createString("inOverlay");
            }
            return $$0;
        })))));
    }
}

