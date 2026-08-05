/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public abstract class NamedEntityFix
extends DataFix {
    private final String name;
    protected final String entityName;
    protected final DSL.TypeReference type;

    public NamedEntityFix(Schema $$0, boolean $$1, String $$2, DSL.TypeReference $$3, String $$4) {
        super($$0, $$1);
        this.name = $$2;
        this.type = $$3;
        this.entityName = $$4;
    }

    public TypeRewriteRule makeRule() {
        OpticFinder $$0 = DSL.namedChoice((String)this.entityName, (Type)this.getInputSchema().getChoiceType(this.type, this.entityName));
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(this.type), this.getOutputSchema().getType(this.type), $$1 -> $$1.updateTyped($$0, this.getOutputSchema().getChoiceType(this.type, this.entityName), this::fix));
    }

    protected abstract Typed<?> fix(Typed<?> var1);
}

