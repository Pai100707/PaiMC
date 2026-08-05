/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class SimplestEntityRenameFix
extends DataFix {
    private final String name;

    public SimplestEntityRenameFix(String $$0, Schema $$1, boolean $$2) {
        super($$1, $$2);
        this.name = $$0;
    }

    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType $$0 = this.getInputSchema().findChoiceType(References.ENTITY);
        TaggedChoice.TaggedChoiceType $$1 = this.getOutputSchema().findChoiceType(References.ENTITY);
        Type $$22 = DSL.named((String)References.ENTITY_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(this.getOutputSchema().getType(References.ENTITY_NAME), $$22)) {
            throw new IllegalStateException("Entity name type is not what was expected.");
        }
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhere(this.name, (Type)$$0, (Type)$$1, $$2 -> $$22 -> $$22.mapFirst($$2 -> {
            String $$3 = this.rename((String)$$2);
            Type $$4 = (Type)$$0.types().get($$2);
            Type $$5 = (Type)$$1.types().get($$3);
            if (!$$5.equals((Object)$$4, true, true)) {
                throw new IllegalStateException(String.format(Locale.ROOT, "Dynamic type check failed: %s not equal to %s", $$5, $$4));
            }
            return $$3;
        })), (TypeRewriteRule)this.fixTypeEverywhere(this.name + " for entity name", $$22, $$02 -> $$0 -> $$0.mapSecond(this::rename)));
    }

    protected abstract String rename(String var1);
}

