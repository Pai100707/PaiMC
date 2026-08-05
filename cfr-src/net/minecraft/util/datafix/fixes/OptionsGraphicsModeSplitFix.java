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
import net.minecraft.util.datafix.fixes.References;

public class OptionsGraphicsModeSplitFix
extends DataFix {
    private final String newFieldName;
    private final String valueIfFast;
    private final String valueIfFancy;
    private final String valueIfFabulous;

    public OptionsGraphicsModeSplitFix(Schema $$0, String $$1, String $$2, String $$3, String $$4) {
        super($$0, true);
        this.newFieldName = $$1;
        this.valueIfFast = $$2;
        this.valueIfFancy = $$3;
        this.valueIfFabulous = $$4;
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("graphicsMode split to " + this.newFieldName, this.getInputSchema().getType(References.OPTIONS), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> (Dynamic)DataFixUtils.orElseGet((Optional)$$0.get("graphicsMode").asString().map($$1 -> $$0.set(this.newFieldName, $$0.createString(this.getValue((String)$$1)))).result(), () -> $$0.set(this.newFieldName, $$0.createString(this.valueIfFancy)))));
    }

    private String getValue(String $$0) {
        return switch ($$0) {
            case "2" -> this.valueIfFabulous;
            case "0" -> this.valueIfFast;
            default -> this.valueIfFancy;
        };
    }
}

