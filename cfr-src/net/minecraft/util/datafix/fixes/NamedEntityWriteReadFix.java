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
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public abstract class NamedEntityWriteReadFix
extends DataFix {
    private final String name;
    private final String entityName;
    private final DSL.TypeReference type;

    public NamedEntityWriteReadFix(Schema $$0, boolean $$1, String $$2, DSL.TypeReference $$3, String $$4) {
        super($$0, $$1);
        this.name = $$2;
        this.type = $$3;
        this.entityName = $$4;
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(this.type);
        Type $$1 = this.getInputSchema().getChoiceType(this.type, this.entityName);
        Type $$2 = this.getOutputSchema().getType(this.type);
        OpticFinder $$3 = DSL.namedChoice((String)this.entityName, (Type)$$1);
        Type<?> $$4 = ExtraDataFixUtils.patchSubType($$0, $$0, $$2);
        return this.fix($$0, $$2, $$4, $$3);
    }

    private <S, T, A> TypeRewriteRule fix(Type<S> $$0, Type<T> $$1, Type<?> $$2, OpticFinder<A> $$32) {
        return this.fixTypeEverywhereTyped(this.name, $$0, $$1, $$3 -> {
            if ($$3.getOptional($$32).isEmpty()) {
                return ExtraDataFixUtils.cast($$1, $$3);
            }
            Typed $$4 = ExtraDataFixUtils.cast($$2, $$3);
            return Util.writeAndReadTypedOrThrow($$4, $$1, this::fix);
        });
    }

    protected abstract <T> Dynamic<T> fix(Dynamic<T> var1);
}

