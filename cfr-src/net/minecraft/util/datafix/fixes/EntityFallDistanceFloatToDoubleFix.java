/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class EntityFallDistanceFloatToDoubleFix
extends DataFix {
    private final DSL.TypeReference type;

    public EntityFallDistanceFloatToDoubleFix(Schema $$0, DSL.TypeReference $$1) {
        super($$0, false);
        this.type = $$1;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityFallDistanceFloatToDoubleFixFor" + this.type.typeName(), this.getOutputSchema().getType(this.type), EntityFallDistanceFloatToDoubleFix::fixEntity);
    }

    private static Typed<?> fixEntity(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), $$02 -> $$02.renameAndFixField("FallDistance", "fall_distance", $$0 -> $$0.createDouble((double)$$0.asFloat(0.0f))));
    }
}

