/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.DataComponentRemainderFix;

public class FireResistantToDamageResistantComponentFix
extends DataComponentRemainderFix {
    public FireResistantToDamageResistantComponentFix(Schema $$0) {
        super($$0, "FireResistantToDamageResistantComponentFix", "minecraft:fire_resistant", "minecraft:damage_resistant");
    }

    @Override
    protected <T> Dynamic<T> fixComponent(Dynamic<T> $$0) {
        return $$0.emptyMap().set("types", $$0.createString("#minecraft:is_fire"));
    }
}

