/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityCustomNameToComponentFix
extends DataFix {
    public EntityCustomNameToComponentFix(Schema $$0) {
        super($$0, true);
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ENTITY);
        Type $$1 = this.getOutputSchema().getType(References.ENTITY);
        OpticFinder $$2 = DSL.fieldFinder((String)"id", NamespacedSchema.namespacedString());
        OpticFinder $$3 = $$0.findField("CustomName");
        Type $$42 = $$1.findFieldType("CustomName");
        return this.fixTypeEverywhereTyped("EntityCustomNameToComponentFix", $$0, $$1, $$4 -> EntityCustomNameToComponentFix.fixEntity($$4, $$1, (OpticFinder<String>)$$2, (OpticFinder<String>)$$3, $$42));
    }

    private static <T> Typed<?> fixEntity(Typed<?> $$02, Type<?> $$1, OpticFinder<String> $$2, OpticFinder<String> $$3, Type<T> $$4) {
        Optional $$5 = $$02.getOptional($$3);
        if ($$5.isEmpty()) {
            return ExtraDataFixUtils.cast($$1, $$02);
        }
        if (((String)$$5.get()).isEmpty()) {
            return Util.writeAndReadTypedOrThrow($$02, $$1, $$0 -> $$0.remove("CustomName"));
        }
        String $$6 = $$02.getOptional($$2).orElse("");
        Dynamic<T> $$7 = EntityCustomNameToComponentFix.fixCustomName($$02.getOps(), (String)$$5.get(), $$6);
        return $$02.set($$3, Util.readTypedOrThrow($$4, $$7));
    }

    private static <T> Dynamic<T> fixCustomName(DynamicOps<T> $$0, String $$1, String $$2) {
        if ("minecraft:commandblock_minecart".equals($$2)) {
            return new Dynamic($$0, $$0.createString($$1));
        }
        return LegacyComponentDataFixUtils.createPlainTextComponent($$0, $$1);
    }
}

