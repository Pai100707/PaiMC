/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TypeTemplate
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V4290
extends NamespacedSchema {
    public V4290(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
        super.registerTypes($$0, $$1, $$2);
        $$0.registerType(true, References.TEXT_COMPONENT, () -> DSL.or((TypeTemplate)DSL.or((TypeTemplate)DSL.constType((Type)DSL.string()), (TypeTemplate)DSL.list((TypeTemplate)References.TEXT_COMPONENT.in($$0))), (TypeTemplate)DSL.optionalFields((String)"extra", (TypeTemplate)DSL.list((TypeTemplate)References.TEXT_COMPONENT.in($$0)), (String)"separator", (TypeTemplate)References.TEXT_COMPONENT.in($$0), (String)"hoverEvent", (TypeTemplate)DSL.taggedChoice((String)"action", (Type)DSL.string(), Map.of("show_text", DSL.optionalFields((String)"contents", (TypeTemplate)References.TEXT_COMPONENT.in($$0)), "show_item", DSL.optionalFields((String)"contents", (TypeTemplate)DSL.or((TypeTemplate)References.ITEM_STACK.in($$0), (TypeTemplate)References.ITEM_NAME.in($$0))), "show_entity", DSL.optionalFields((String)"type", (TypeTemplate)References.ENTITY_NAME.in($$0), (String)"name", (TypeTemplate)References.TEXT_COMPONENT.in($$0)))))));
    }
}

