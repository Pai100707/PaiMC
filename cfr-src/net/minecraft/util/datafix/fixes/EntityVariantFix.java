/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.util.datafix.fixes.NamedEntityFix;

public class EntityVariantFix
extends NamedEntityFix {
    private final String fieldName;
    private final IntFunction<String> idConversions;

    public EntityVariantFix(Schema $$0, String $$1, DSL.TypeReference $$2, String $$3, String $$4, IntFunction<String> $$5) {
        super($$0, false, $$1, $$2, $$3);
        this.fieldName = $$4;
        this.idConversions = $$5;
    }

    private static <T> Dynamic<T> updateAndRename(Dynamic<T> $$0, String $$1, String $$2, Function<Dynamic<T>, Dynamic<T>> $$3) {
        return $$0.map($$42 -> {
            DynamicOps $$5 = $$0.getOps();
            Function<Object, Object> $$6 = $$2 -> ((Dynamic)$$3.apply(new Dynamic($$5, $$2))).getValue();
            return $$5.get($$42, $$1).map($$4 -> $$5.set($$42, $$2, $$6.apply($$4))).result().orElse($$42);
        });
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), $$02 -> EntityVariantFix.updateAndRename($$02, this.fieldName, "variant", $$0 -> (Dynamic)DataFixUtils.orElse((Optional)$$0.asNumber().map($$1 -> $$0.createString(this.idConversions.apply($$1.intValue()))).result(), (Object)$$0)));
    }
}

