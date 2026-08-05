/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.util.datafix.fixes.References;

public class BlendingDataRemoveFromNetherEndFix
extends DataFix {
    public BlendingDataRemoveFromNetherEndFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getOutputSchema().getType(References.CHUNK);
        return this.fixTypeEverywhereTyped("BlendingDataRemoveFromNetherEndFix", $$0, $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> BlendingDataRemoveFromNetherEndFix.updateChunkTag($$0, $$0.get("__context"))));
    }

    private static Dynamic<?> updateChunkTag(Dynamic<?> $$0, OptionalDynamic<?> $$1) {
        boolean $$2 = "minecraft:overworld".equals($$1.get("dimension").asString().result().orElse(""));
        return $$2 ? $$0 : $$0.remove("blending_data");
    }
}

