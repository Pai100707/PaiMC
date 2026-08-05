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

public class V2100
extends NamespacedSchema {
    public V2100(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    protected static void registerMob(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
        $$0.registerSimple($$1, $$2);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
        Map $$1 = super.registerEntities($$0);
        V2100.registerMob($$0, $$1, "minecraft:bee");
        V2100.registerMob($$0, $$1, "minecraft:bee_stinger");
        return $$1;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
        Map $$1 = super.registerBlockEntities($$0);
        $$0.register($$1, "minecraft:beehive", () -> DSL.optionalFields((String)"Bees", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"EntityData", (TypeTemplate)References.ENTITY_TREE.in($$0)))));
        return $$1;
    }
}

