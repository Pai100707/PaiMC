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
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V3818_3;

public class V4059
extends NamespacedSchema {
    public V4059(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema $$0) {
        SequencedMap<String, Supplier<TypeTemplate>> $$1 = V3818_3.components($$0);
        $$1.remove("minecraft:food");
        $$1.put("minecraft:use_remainder", () -> References.ITEM_STACK.in($$0));
        $$1.put("minecraft:equippable", () -> DSL.optionalFields((String)"allowed_entities", (TypeTemplate)DSL.or((TypeTemplate)References.ENTITY_NAME.in($$0), (TypeTemplate)DSL.list((TypeTemplate)References.ENTITY_NAME.in($$0)))));
        return $$1;
    }

    public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
        super.registerTypes($$0, $$1, $$2);
        $$0.registerType(true, References.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(V4059.components($$0)));
    }
}

