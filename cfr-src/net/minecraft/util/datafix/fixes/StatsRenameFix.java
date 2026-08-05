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
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Map;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class StatsRenameFix
extends DataFix {
    private final String name;
    private final Map<String, String> renames;

    public StatsRenameFix(Schema $$0, String $$1, Map<String, String> $$2) {
        super($$0, false);
        this.name = $$1;
        this.renames = $$2;
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.createStatRule(), (TypeRewriteRule)this.createCriteriaRule());
    }

    private TypeRewriteRule createCriteriaRule() {
        Type $$0 = this.getOutputSchema().getType(References.OBJECTIVE);
        Type $$1 = this.getInputSchema().getType(References.OBJECTIVE);
        OpticFinder $$2 = $$1.findField("CriteriaType");
        TaggedChoice.TaggedChoiceType $$32 = (TaggedChoice.TaggedChoiceType)$$2.type().findChoiceType("type", -1).orElseThrow(() -> new IllegalStateException("Can't find choice type for criteria"));
        Type $$4 = (Type)$$32.types().get("minecraft:custom");
        if ($$4 == null) {
            throw new IllegalStateException("Failed to find custom criterion type variant");
        }
        OpticFinder $$5 = DSL.namedChoice((String)"minecraft:custom", (Type)$$4);
        OpticFinder $$6 = DSL.fieldFinder((String)"id", NamespacedSchema.namespacedString());
        return this.fixTypeEverywhereTyped(this.name, $$1, $$0, $$3 -> $$3.updateTyped($$2, $$2 -> $$2.updateTyped($$5, $$1 -> $$1.update($$6, $$0 -> this.renames.getOrDefault($$0, (String)$$0)))));
    }

    private TypeRewriteRule createStatRule() {
        Type $$0 = this.getOutputSchema().getType(References.STATS);
        Type $$1 = this.getInputSchema().getType(References.STATS);
        OpticFinder $$2 = $$1.findField("stats");
        OpticFinder $$32 = $$2.type().findField("minecraft:custom");
        OpticFinder $$4 = NamespacedSchema.namespacedString().finder();
        return this.fixTypeEverywhereTyped(this.name, $$1, $$0, $$3 -> $$3.updateTyped($$2, $$2 -> $$2.updateTyped($$32, $$1 -> $$1.update($$4, $$0 -> this.renames.getOrDefault($$0, (String)$$0)))));
    }
}

