/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.fixes.References;

public class FeatureFlagRemoveFix
extends DataFix {
    private final String name;
    private final Set<String> flagsToRemove;

    public FeatureFlagRemoveFix(Schema $$0, String $$1, Set<String> $$2) {
        super($$0, false);
        this.name = $$1;
        this.flagsToRemove = $$2;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.LIGHTWEIGHT_LEVEL), $$0 -> $$0.update(DSL.remainderFinder(), this::fixTag));
    }

    private <T> Dynamic<T> fixTag(Dynamic<T> $$0) {
        List $$1 = $$0.get("removed_features").asStream().collect(Collectors.toCollection(ArrayList::new));
        Dynamic $$22 = $$0.update("enabled_features", $$2 -> (Dynamic)DataFixUtils.orElse($$2.asStreamOpt().result().map($$22 -> $$22.filter($$2 -> {
            Optional $$3 = $$2.asString().result();
            if ($$3.isEmpty()) {
                return true;
            }
            boolean $$4 = this.flagsToRemove.contains($$3.get());
            if ($$4) {
                $$1.add($$0.createString((String)$$3.get()));
            }
            return !$$4;
        })).map(arg_0 -> ((Dynamic)$$0).createList(arg_0)), (Object)$$2));
        if (!$$1.isEmpty()) {
            $$22 = $$22.set("removed_features", $$0.createList($$1.stream()));
        }
        return $$22;
    }
}

