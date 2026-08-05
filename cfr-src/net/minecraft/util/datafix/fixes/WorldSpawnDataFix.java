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
import java.util.stream.IntStream;
import net.minecraft.util.datafix.fixes.References;

public class WorldSpawnDataFix
extends DataFix {
    public WorldSpawnDataFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("WorldSpawnDataFix", this.getInputSchema().getType(References.LEVEL), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> {
            int $$1 = $$0.get("SpawnX").asInt(0);
            int $$2 = $$0.get("SpawnY").asInt(0);
            int $$3 = $$0.get("SpawnZ").asInt(0);
            float $$4 = $$0.get("SpawnAngle").asFloat(0.0f);
            Dynamic $$5 = $$0.emptyMap().set("dimension", $$0.createString("minecraft:overworld")).set("pos", $$0.createIntList(IntStream.of($$1, $$2, $$3))).set("yaw", $$0.createFloat($$4)).set("pitch", $$0.createFloat(0.0f));
            $$0 = $$0.remove("SpawnX").remove("SpawnY").remove("SpawnZ").remove("SpawnAngle");
            return $$0.set("spawn", $$5);
        }));
    }
}

