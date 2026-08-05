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
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class ChunkDeleteIgnoredLightDataFix
extends DataFix {
    public ChunkDeleteIgnoredLightDataFix(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder $$12 = $$0.findField("sections");
        return this.fixTypeEverywhereTyped("ChunkDeleteIgnoredLightDataFix", $$0, $$1 -> {
            boolean $$2 = ((Dynamic)$$1.get(DSL.remainderFinder())).get("isLightOn").asBoolean(false);
            if (!$$2) {
                return $$1.updateTyped($$12, $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.remove("BlockLight").remove("SkyLight")));
            }
            return $$1;
        });
    }
}

