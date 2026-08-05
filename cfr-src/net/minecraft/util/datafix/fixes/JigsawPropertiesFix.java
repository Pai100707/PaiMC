/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class JigsawPropertiesFix
extends NamedEntityFix {
    public JigsawPropertiesFix(Schema $$0, boolean $$1) {
        super($$0, $$1, "JigsawPropertiesFix", References.BLOCK_ENTITY, "minecraft:jigsaw");
    }

    private static Dynamic<?> fixTag(Dynamic<?> $$0) {
        String $$1 = $$0.get("attachement_type").asString("minecraft:empty");
        String $$2 = $$0.get("target_pool").asString("minecraft:empty");
        return $$0.set("name", $$0.createString($$1)).set("target", $$0.createString($$1)).remove("attachement_type").set("pool", $$0.createString($$2)).remove("target_pool");
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), JigsawPropertiesFix::fixTag);
    }
}

