/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.NamedEntityFix;

public class NamedEntityConvertUncheckedFix
extends NamedEntityFix {
    public NamedEntityConvertUncheckedFix(Schema $$0, String $$1, DSL.TypeReference $$2, String $$3) {
        super($$0, true, $$1, $$2, $$3);
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        Type $$1 = this.getOutputSchema().getChoiceType(this.type, this.entityName);
        return ExtraDataFixUtils.cast($$1, $$0);
    }
}

