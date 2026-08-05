/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class AbstractBlockPropertyFix
extends DataFix {
    private final String name;

    public AbstractBlockPropertyFix(Schema $$0, String $$1) {
        super($$0, false);
        this.name = $$1;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.BLOCK_STATE), $$0 -> $$0.update(DSL.remainderFinder(), this::fixBlockState));
    }

    private Dynamic<?> fixBlockState(Dynamic<?> $$0) {
        Optional<String> $$12 = $$0.get("Name").asString().result().map(NamespacedSchema::ensureNamespaced);
        if ($$12.isPresent() && this.shouldFix($$12.get())) {
            return $$0.update("Properties", $$1 -> this.fixProperties((String)$$12.get(), (Dynamic)$$1));
        }
        return $$0;
    }

    protected abstract boolean shouldFix(String var1);

    protected abstract <T> Dynamic<T> fixProperties(String var1, Dynamic<T> var2);
}

