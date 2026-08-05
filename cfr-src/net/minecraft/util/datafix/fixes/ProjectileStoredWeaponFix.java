/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
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
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class ProjectileStoredWeaponFix
extends DataFix {
    public ProjectileStoredWeaponFix(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ENTITY);
        Type $$1 = this.getOutputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped("Fix Arrow stored weapon", $$0, $$1, ExtraDataFixUtils.chainAllFilters(this.fixChoice("minecraft:arrow"), this.fixChoice("minecraft:spectral_arrow")));
    }

    private Function<Typed<?>, Typed<?>> fixChoice(String $$0) {
        Type $$1 = this.getInputSchema().getChoiceType(References.ENTITY, $$0);
        Type $$2 = this.getOutputSchema().getChoiceType(References.ENTITY, $$0);
        return ProjectileStoredWeaponFix.fixChoiceCap($$0, $$1, $$2);
    }

    private static <T> Function<Typed<?>, Typed<?>> fixChoiceCap(String $$0, Type<?> $$1, Type<T> $$22) {
        OpticFinder $$3 = DSL.namedChoice((String)$$0, $$1);
        return $$2 -> $$2.updateTyped($$3, $$22, $$1 -> Util.writeAndReadTypedOrThrow($$1, $$22, UnaryOperator.identity()));
    }
}

