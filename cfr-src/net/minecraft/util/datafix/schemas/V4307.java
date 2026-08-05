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
import net.minecraft.util.datafix.schemas.V4059;

public class V4307
extends NamespacedSchema {
    public V4307(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema $$0) {
        SequencedMap<String, Supplier<TypeTemplate>> $$1 = V4059.components($$0);
        $$1.put("minecraft:can_place_on", () -> V4307.adventureModePredicate($$0));
        $$1.put("minecraft:can_break", () -> V4307.adventureModePredicate($$0));
        return $$1;
    }

    private static TypeTemplate adventureModePredicate(Schema $$0) {
        TypeTemplate $$1 = DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_NAME.in($$0), (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_NAME.in($$0))));
        return DSL.or((TypeTemplate)$$1, (TypeTemplate)DSL.list((TypeTemplate)$$1));
    }

    public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
        super.registerTypes($$0, $$1, $$2);
        $$0.registerType(true, References.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(V4307.components($$0)));
    }
}

