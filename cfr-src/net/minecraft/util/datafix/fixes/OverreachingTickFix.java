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
import java.util.List;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class OverreachingTickFix
extends DataFix {
    public OverreachingTickFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder $$1 = $$0.findField("block_ticks");
        return this.fixTypeEverywhereTyped("Handle ticks saved in the wrong chunk", $$0, $$12 -> {
            Optional $$2 = $$12.getOptionalTyped($$1);
            Optional $$3 = $$2.isPresent() ? ((Typed)$$2.get()).write().result() : Optional.empty();
            return $$12.update(DSL.remainderFinder(), $$1 -> {
                int $$2 = $$1.get("xPos").asInt(0);
                int $$3 = $$1.get("zPos").asInt(0);
                Optional $$4 = $$1.get("fluid_ticks").get().result();
                $$1 = OverreachingTickFix.extractOverreachingTicks($$1, $$2, $$3, $$3, "neighbor_block_ticks");
                $$1 = OverreachingTickFix.extractOverreachingTicks($$1, $$2, $$3, $$4, "neighbor_fluid_ticks");
                return $$1;
            });
        });
    }

    private static Dynamic<?> extractOverreachingTicks(Dynamic<?> $$0, int $$1, int $$22, Optional<? extends Dynamic<?>> $$3, String $$4) {
        List<Dynamic> $$5;
        if ($$3.isPresent() && !($$5 = $$3.get().asStream().filter($$2 -> {
            int $$3 = $$2.get("x").asInt(0);
            int $$4 = $$2.get("z").asInt(0);
            int $$5 = Math.abs($$1 - ($$3 >> 4));
            int $$6 = Math.abs($$22 - ($$4 >> 4));
            return ($$5 != 0 || $$6 != 0) && $$5 <= 1 && $$6 <= 1;
        }).toList()).isEmpty()) {
            $$0 = $$0.set("UpgradeData", $$0.get("UpgradeData").orElseEmptyMap().set($$4, $$0.createList($$5.stream())));
        }
        return $$0;
    }
}

