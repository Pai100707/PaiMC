/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.DoubleUnaryOperator;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityAttributeBaseValueFix
extends NamedEntityFix {
    private final String attributeId;
    private final DoubleUnaryOperator valueFixer;

    public EntityAttributeBaseValueFix(Schema $$0, String $$1, String $$2, String $$3, DoubleUnaryOperator $$4) {
        super($$0, false, $$1, References.ENTITY, $$2);
        this.attributeId = $$3;
        this.valueFixer = $$4;
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), this::fixValue);
    }

    private Dynamic<?> fixValue(Dynamic<?> $$0) {
        return $$0.update("attributes", $$1 -> $$0.createList($$1.asStream().map($$0 -> {
            Object $$1 = NamespacedSchema.ensureNamespaced($$0.get("id").asString(""));
            if (!$$1.equals(this.attributeId)) {
                return $$0;
            }
            double $$2 = $$0.get("base").asDouble(0.0);
            return $$0.set("base", $$0.createDouble(this.valueFixer.applyAsDouble($$2)));
        })));
    }
}

