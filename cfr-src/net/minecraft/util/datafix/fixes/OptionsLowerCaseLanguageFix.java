/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class OptionsLowerCaseLanguageFix
extends DataFix {
    public OptionsLowerCaseLanguageFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsLowerCaseLanguageFix", this.getInputSchema().getType(References.OPTIONS), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> {
            Optional $$1 = $$0.get("lang").asString().result();
            if ($$1.isPresent()) {
                return $$0.set("lang", $$0.createString(((String)$$1.get()).toLowerCase(Locale.ROOT)));
            }
            return $$0;
        }));
    }
}

