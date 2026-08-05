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
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.fixes.References;

public class ChunkLightRemoveFix
extends DataFix {
    public ChunkLightRemoveFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.CHUNK);
        Type $$12 = $$0.findFieldType("Level");
        OpticFinder $$2 = DSL.fieldFinder((String)"Level", (Type)$$12);
        return this.fixTypeEverywhereTyped("ChunkLightRemoveFix", $$0, this.getOutputSchema().getType(References.CHUNK), $$1 -> $$1.updateTyped($$2, $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.remove("isLightOn"))));
    }
}

