/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class AttributesRenameFix
extends DataFix {
    private final String name;
    private final UnaryOperator<String> renames;

    public AttributesRenameFix(Schema $$0, String $$1, UnaryOperator<String> $$2) {
        super($$0, false);
        this.name = $$1;
        this.renames = $$2;
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped(this.name + " (Components)", this.getInputSchema().getType(References.DATA_COMPONENTS), this::fixDataComponents), (TypeRewriteRule[])new TypeRewriteRule[]{this.fixTypeEverywhereTyped(this.name + " (Entity)", this.getInputSchema().getType(References.ENTITY), this::fixEntity), this.fixTypeEverywhereTyped(this.name + " (Player)", this.getInputSchema().getType(References.PLAYER), this::fixEntity)});
    }

    private Typed<?> fixDataComponents(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), $$02 -> $$02.update("minecraft:attribute_modifiers", $$0 -> $$0.update("modifiers", $$02 -> (Dynamic)DataFixUtils.orElse($$02.asStreamOpt().result().map($$0 -> $$0.map(this::fixTypeField)).map(arg_0 -> ((Dynamic)$$02).createList(arg_0)), (Object)$$02))));
    }

    private Typed<?> fixEntity(Typed<?> $$02) {
        return $$02.update(DSL.remainderFinder(), $$0 -> $$0.update("attributes", $$02 -> (Dynamic)DataFixUtils.orElse($$02.asStreamOpt().result().map($$0 -> $$0.map(this::fixIdField)).map(arg_0 -> ((Dynamic)$$02).createList(arg_0)), (Object)$$02)));
    }

    private Dynamic<?> fixIdField(Dynamic<?> $$0) {
        return ExtraDataFixUtils.fixStringField($$0, "id", this.renames);
    }

    private Dynamic<?> fixTypeField(Dynamic<?> $$0) {
        return ExtraDataFixUtils.fixStringField($$0, "type", this.renames);
    }
}

