/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class ItemRenameFix
extends DataFix {
    private final String name;

    public ItemRenameFix(Schema $$0, String $$1) {
        super($$0, false);
        this.name = $$1;
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(this.getInputSchema().getType(References.ITEM_NAME), $$0)) {
            throw new IllegalStateException("item name type is not what was expected.");
        }
        return this.fixTypeEverywhere(this.name, $$0, $$02 -> $$0 -> $$0.mapSecond(this::fixItem));
    }

    protected abstract String fixItem(String var1);

    public static DataFix create(Schema $$0, String $$1, final Function<String, String> $$2) {
        return new ItemRenameFix($$0, $$1){

            @Override
            protected String fixItem(String $$0) {
                return (String)$$2.apply($$0);
            }
        };
    }
}

