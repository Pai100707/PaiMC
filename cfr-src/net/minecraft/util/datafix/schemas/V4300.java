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

public class V4300
extends NamespacedSchema {
    public V4300(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
        Map $$12 = super.registerEntities($$0);
        $$0.register($$12, "minecraft:llama", $$1 -> V4300.entityWithInventory($$0));
        $$0.register($$12, "minecraft:trader_llama", $$1 -> V4300.entityWithInventory($$0));
        $$0.register($$12, "minecraft:donkey", $$1 -> V4300.entityWithInventory($$0));
        $$0.register($$12, "minecraft:mule", $$1 -> V4300.entityWithInventory($$0));
        $$0.registerSimple($$12, "minecraft:horse");
        $$0.registerSimple($$12, "minecraft:skeleton_horse");
        $$0.registerSimple($$12, "minecraft:zombie_horse");
        return $$12;
    }

    private static TypeTemplate entityWithInventory(Schema $$0) {
        return DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)));
    }
}

