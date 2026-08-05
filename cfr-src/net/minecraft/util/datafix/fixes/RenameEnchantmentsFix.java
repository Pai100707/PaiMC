/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RenameEnchantmentsFix
extends DataFix {
    final String name;
    final Map<String, String> renames;

    public RenameEnchantmentsFix(Schema $$0, String $$1, Map<String, String> $$2) {
        super($$0, false);
        this.name = $$1;
        this.renames = $$2;
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder $$12 = $$0.findField("tag");
        return this.fixTypeEverywhereTyped(this.name, $$0, $$1 -> $$1.updateTyped($$12, $$0 -> $$0.update(DSL.remainderFinder(), this::fixTag)));
    }

    private Dynamic<?> fixTag(Dynamic<?> $$0) {
        $$0 = this.fixEnchantmentList($$0, "Enchantments");
        $$0 = this.fixEnchantmentList($$0, "StoredEnchantments");
        return $$0;
    }

    private Dynamic<?> fixEnchantmentList(Dynamic<?> $$02, String $$1) {
        return $$02.update($$1, $$0 -> (Dynamic)$$0.asStreamOpt().map($$02 -> $$02.map($$0 -> $$0.update("id", $$12 -> (Dynamic)$$12.asString().map($$1 -> $$0.createString(this.renames.getOrDefault(NamespacedSchema.ensureNamespaced($$1), (String)$$1))).mapOrElse(Function.identity(), $$1 -> $$12)))).map(arg_0 -> ((Dynamic)$$0).createList(arg_0)).mapOrElse(Function.identity(), $$1 -> $$0));
    }
}

