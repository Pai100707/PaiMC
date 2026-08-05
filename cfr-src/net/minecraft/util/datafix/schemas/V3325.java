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

public class V3325
extends NamespacedSchema {
    public V3325(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
        Map $$12 = super.registerEntities($$0);
        $$0.register($$12, "minecraft:item_display", $$1 -> DSL.optionalFields((String)"item", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.register($$12, "minecraft:block_display", $$1 -> DSL.optionalFields((String)"block_state", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        $$0.register($$12, "minecraft:text_display", () -> DSL.optionalFields((String)"text", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        return $$12;
    }
}

