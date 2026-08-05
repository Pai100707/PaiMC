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

public class VillagerFollowRangeFix
extends NamedEntityFix {
    private static final double ORIGINAL_VALUE = 16.0;
    private static final double NEW_BASE_VALUE = 48.0;

    public VillagerFollowRangeFix(Schema $$0) {
        super($$0, false, "Villager Follow Range Fix", References.ENTITY, "minecraft:villager");
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), VillagerFollowRangeFix::fixValue);
    }

    private static Dynamic<?> fixValue(Dynamic<?> $$0) {
        return $$0.update("Attributes", $$1 -> $$0.createList($$1.asStream().map($$0 -> {
            if (!$$0.get("Name").asString("").equals("generic.follow_range") || $$0.get("Base").asDouble(0.0) != 16.0) {
                return $$0;
            }
            return $$0.set("Base", $$0.createDouble(48.0));
        })));
    }
}

