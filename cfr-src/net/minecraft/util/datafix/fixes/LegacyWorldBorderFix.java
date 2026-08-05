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
import net.minecraft.util.datafix.fixes.References;

public class LegacyWorldBorderFix
extends DataFix {
    public LegacyWorldBorderFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LegacyWorldBorderFix", this.getInputSchema().getType(References.LEVEL), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> {
            Dynamic $$1 = $$0.emptyMap().set("center_x", $$0.createDouble($$0.get("BorderCenterX").asDouble(0.0))).set("center_z", $$0.createDouble($$0.get("BorderCenterZ").asDouble(0.0))).set("size", $$0.createDouble($$0.get("BorderSize").asDouble(5.9999968E7))).set("lerp_time", $$0.createLong($$0.get("BorderSizeLerpTime").asLong(0L))).set("lerp_target", $$0.createDouble($$0.get("BorderSizeLerpTarget").asDouble(0.0))).set("safe_zone", $$0.createDouble($$0.get("BorderSafeZone").asDouble(5.0))).set("damage_per_block", $$0.createDouble($$0.get("BorderDamagePerBlock").asDouble(0.2))).set("warning_blocks", $$0.createInt($$0.get("BorderWarningBlocks").asInt(5))).set("warning_time", $$0.createInt($$0.get("BorderWarningTime").asInt(15)));
            $$0 = $$0.remove("BorderCenterX").remove("BorderCenterZ").remove("BorderSize").remove("BorderSizeLerpTime").remove("BorderSizeLerpTarget").remove("BorderSafeZone").remove("BorderDamagePerBlock").remove("BorderWarningBlocks").remove("BorderWarningTime");
            return $$0.set("world_border", $$1);
        }));
    }
}

