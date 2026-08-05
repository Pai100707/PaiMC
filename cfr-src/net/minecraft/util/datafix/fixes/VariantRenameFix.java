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
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityFix;

public class VariantRenameFix
extends NamedEntityFix {
    private final Map<String, String> renames;

    public VariantRenameFix(Schema $$0, String $$1, DSL.TypeReference $$2, String $$3, Map<String, String> $$4) {
        super($$0, false, $$1, $$2, $$3);
        this.renames = $$4;
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), $$02 -> $$02.update("variant", $$0 -> (Dynamic)DataFixUtils.orElse((Optional)$$0.asString().map($$1 -> $$0.createString(this.renames.getOrDefault($$1, (String)$$1))).result(), (Object)$$0)));
    }
}

