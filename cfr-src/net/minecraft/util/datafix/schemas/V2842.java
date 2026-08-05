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

public class V2842
extends NamespacedSchema {
    public V2842(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
        super.registerTypes($$0, $$1, $$2);
        $$0.registerType(false, References.CHUNK, () -> DSL.optionalFields((String)"entities", (TypeTemplate)DSL.list((TypeTemplate)References.ENTITY_TREE.in($$0)), (String)"block_entities", (TypeTemplate)DSL.list((TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_ENTITY.in($$0), (TypeTemplate)DSL.remainder())), (String)"block_ticks", (TypeTemplate)DSL.list((TypeTemplate)DSL.fields((String)"i", (TypeTemplate)References.BLOCK_NAME.in($$0))), (String)"sections", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"biomes", (TypeTemplate)DSL.optionalFields((String)"palette", (TypeTemplate)DSL.list((TypeTemplate)References.BIOME.in($$0))), (String)"block_states", (TypeTemplate)DSL.optionalFields((String)"palette", (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_STATE.in($$0))))), (String)"structures", (TypeTemplate)DSL.optionalFields((String)"starts", (TypeTemplate)DSL.compoundList((TypeTemplate)References.STRUCTURE_FEATURE.in($$0)))));
    }
}

