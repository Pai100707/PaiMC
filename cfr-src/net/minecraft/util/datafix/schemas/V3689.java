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

public class V3689
extends NamespacedSchema {
    public V3689(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
        Map $$1 = super.registerEntities($$0);
        $$0.registerSimple($$1, "minecraft:breeze");
        $$0.registerSimple($$1, "minecraft:wind_charge");
        $$0.registerSimple($$1, "minecraft:breeze_wind_charge");
        return $$1;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
        Map $$1 = super.registerBlockEntities($$0);
        $$0.register($$1, "minecraft:trial_spawner", () -> DSL.optionalFields((String)"spawn_potentials", (TypeTemplate)DSL.list((TypeTemplate)DSL.fields((String)"data", (TypeTemplate)DSL.fields((String)"entity", (TypeTemplate)References.ENTITY_TREE.in($$0)))), (String)"spawn_data", (TypeTemplate)DSL.fields((String)"entity", (TypeTemplate)References.ENTITY_TREE.in($$0))));
        return $$1;
    }
}

