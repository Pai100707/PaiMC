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
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;

public class AdvancementsRenameFix
extends DataFix {
    private final String name;
    private final Function<String, String> renamer;

    public AdvancementsRenameFix(Schema $$0, boolean $$1, String $$2, Function<String, String> $$3) {
        super($$0, $$1);
        this.name = $$2;
        this.renamer = $$3;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.ADVANCEMENTS), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.updateMapValues($$1 -> {
            String $$22 = ((Dynamic)$$1.getFirst()).asString("");
            return $$1.mapFirst($$2 -> $$0.createString(this.renamer.apply($$22)));
        })));
    }
}

