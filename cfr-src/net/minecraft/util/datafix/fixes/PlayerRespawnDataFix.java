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

public class PlayerRespawnDataFix
extends DataFix {
    public PlayerRespawnDataFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("PlayerRespawnDataFix", this.getInputSchema().getType(References.PLAYER), $$0 -> $$0.update(DSL.remainderFinder(), $$02 -> $$02.update("respawn", $$0 -> $$0.set("dimension", $$0.createString($$0.get("dimension").asString("minecraft:overworld"))).set("yaw", $$0.createFloat($$0.get("angle").asFloat(0.0f))).set("pitch", $$0.createFloat(0.0f)).remove("angle"))));
    }
}

