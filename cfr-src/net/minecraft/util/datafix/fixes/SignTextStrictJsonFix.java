/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.List;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class SignTextStrictJsonFix
extends NamedEntityFix {
    private static final List<String> LINE_FIELDS = List.of("Text1", "Text2", "Text3", "Text4");

    public SignTextStrictJsonFix(Schema $$0) {
        super($$0, false, "SignTextStrictJsonFix", References.BLOCK_ENTITY, "Sign");
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        for (String $$12 : LINE_FIELDS) {
            OpticFinder $$2 = $$0.getType().findField($$12);
            OpticFinder $$3 = DSL.typeFinder((Type)this.getInputSchema().getType(References.TEXT_COMPONENT));
            $$0 = $$0.updateTyped($$2, $$1 -> $$1.update($$3, $$0 -> $$0.mapSecond(LegacyComponentDataFixUtils::rewriteFromLenient)));
        }
        return $$0;
    }
}

