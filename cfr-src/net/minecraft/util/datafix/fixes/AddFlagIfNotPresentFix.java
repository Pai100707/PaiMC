/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class AddFlagIfNotPresentFix
extends DataFix {
    private final String name;
    private final boolean flagValue;
    private final String flagKey;
    private final DSL.TypeReference typeReference;

    public AddFlagIfNotPresentFix(Schema $$0, DSL.TypeReference $$1, String $$2, boolean $$3) {
        super($$0, true);
        this.flagValue = $$3;
        this.flagKey = $$2;
        this.name = "AddFlagIfNotPresentFix_" + this.flagKey + "=" + this.flagValue + " for " + $$0.getVersionKey();
        this.typeReference = $$1;
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(this.typeReference);
        return this.fixTypeEverywhereTyped(this.name, $$0, $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.set(this.flagKey, (Dynamic)DataFixUtils.orElseGet((Optional)$$0.get(this.flagKey).result(), () -> $$0.createBoolean(this.flagValue)))));
    }
}

