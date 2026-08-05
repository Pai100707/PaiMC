/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;
import org.jspecify.annotations.Nullable;

public abstract class DataComponentRemainderFix
extends DataFix {
    private final String name;
    private final String componentId;
    private final String newComponentId;

    public DataComponentRemainderFix(Schema $$0, String $$1, String $$2) {
        this($$0, $$1, $$2, $$2);
    }

    public DataComponentRemainderFix(Schema $$0, String $$1, String $$2, String $$3) {
        super($$0, false);
        this.name = $$1;
        this.componentId = $$2;
        this.newComponentId = $$3;
    }

    public final TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.DATA_COMPONENTS);
        return this.fixTypeEverywhereTyped(this.name, $$0, $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> {
            Optional $$1 = $$0.get(this.componentId).result();
            if ($$1.isEmpty()) {
                return $$0;
            }
            Dynamic $$2 = this.fixComponent((Dynamic)$$1.get());
            return $$0.remove(this.componentId).setFieldIfPresent(this.newComponentId, Optional.ofNullable($$2));
        }));
    }

    protected abstract <T> @Nullable Dynamic<T> fixComponent(Dynamic<T> var1);
}

