/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
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
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.fixes.References;

public class AttributesRenameLegacy
extends DataFix {
    private final String name;
    private final UnaryOperator<String> renames;

    public AttributesRenameLegacy(Schema $$0, String $$1, UnaryOperator<String> $$2) {
        super($$0, false);
        this.name = $$1;
        this.renames = $$2;
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder $$12 = $$0.findField("tag");
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped(this.name + " (ItemStack)", $$0, $$1 -> $$1.updateTyped($$12, this::fixItemStackTag)), (TypeRewriteRule[])new TypeRewriteRule[]{this.fixTypeEverywhereTyped(this.name + " (Entity)", this.getInputSchema().getType(References.ENTITY), this::fixEntity), this.fixTypeEverywhereTyped(this.name + " (Player)", this.getInputSchema().getType(References.PLAYER), this::fixEntity)});
    }

    private Dynamic<?> fixName(Dynamic<?> $$0) {
        return (Dynamic)DataFixUtils.orElse($$0.asString().result().map(this.renames).map(arg_0 -> $$0.createString(arg_0)), $$0);
    }

    private Typed<?> fixItemStackTag(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), $$02 -> $$02.update("AttributeModifiers", $$0 -> (Dynamic)DataFixUtils.orElse($$0.asStreamOpt().result().map($$02 -> $$02.map($$0 -> $$0.update("AttributeName", this::fixName))).map(arg_0 -> ((Dynamic)$$0).createList(arg_0)), (Object)$$0)));
    }

    private Typed<?> fixEntity(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), $$02 -> $$02.update("Attributes", $$0 -> (Dynamic)DataFixUtils.orElse($$0.asStreamOpt().result().map($$02 -> $$02.map($$0 -> $$0.update("Name", this::fixName))).map(arg_0 -> ((Dynamic)$$0).createList(arg_0)), (Object)$$0)));
    }
}

