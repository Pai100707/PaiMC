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
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.fixes.References;

public class CriteriaRenameFix
extends DataFix {
    private final String name;
    private final String advancementId;
    private final UnaryOperator<String> conversions;

    public CriteriaRenameFix(Schema $$0, String $$1, String $$2, UnaryOperator<String> $$3) {
        super($$0, false);
        this.name = $$1;
        this.advancementId = $$2;
        this.conversions = $$3;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.ADVANCEMENTS), $$0 -> $$0.update(DSL.remainderFinder(), this::fixAdvancements));
    }

    private Dynamic<?> fixAdvancements(Dynamic<?> $$0) {
        return $$0.update(this.advancementId, $$02 -> $$02.update("criteria", $$0 -> $$0.updateMapValues($$02 -> $$02.mapFirst($$0 -> (Dynamic)DataFixUtils.orElse((Optional)$$0.asString().map($$1 -> $$0.createString((String)this.conversions.apply((String)$$1))).result(), (Object)$$0)))));
    }
}

