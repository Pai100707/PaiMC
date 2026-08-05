/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V3685
extends NamespacedSchema {
    public V3685(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    protected static TypeTemplate abstractArrow(Schema $$0) {
        return DSL.optionalFields((String)"inBlockState", (TypeTemplate)References.BLOCK_STATE.in($$0), (String)"item", (TypeTemplate)References.ITEM_STACK.in($$0));
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
        Map $$1 = super.registerEntities($$0);
        $$0.register($$1, "minecraft:trident", () -> V3685.abstractArrow($$0));
        $$0.register($$1, "minecraft:spectral_arrow", () -> V3685.abstractArrow($$0));
        $$0.register($$1, "minecraft:arrow", () -> V3685.abstractArrow($$0));
        return $$1;
    }
}

