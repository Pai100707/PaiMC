/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
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
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NamespacedTypeRenameFix
extends DataFix {
    private final String name;
    private final DSL.TypeReference type;
    private final UnaryOperator<String> renamer;

    public NamespacedTypeRenameFix(Schema $$0, String $$1, DSL.TypeReference $$2, UnaryOperator<String> $$3) {
        super($$0, false);
        this.name = $$1;
        this.type = $$2;
        this.renamer = $$3;
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = DSL.named((String)this.type.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals($$0, this.getInputSchema().getType(this.type))) {
            throw new IllegalStateException("\"" + this.type.typeName() + "\" is not what was expected.");
        }
        return this.fixTypeEverywhere(this.name, $$0, $$02 -> $$0 -> $$0.mapSecond(this.renamer));
    }
}

