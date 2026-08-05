/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntityRenameFix
extends DataFix {
    private final String name;
    private final UnaryOperator<String> nameChangeLookup;

    private BlockEntityRenameFix(Schema $$0, String $$1, UnaryOperator<String> $$2) {
        super($$0, true);
        this.name = $$1;
        this.nameChangeLookup = $$2;
    }

    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType $$0 = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        TaggedChoice.TaggedChoiceType $$1 = this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY);
        return this.fixTypeEverywhere(this.name, (Type)$$0, (Type)$$1, $$02 -> $$0 -> $$0.mapFirst(this.nameChangeLookup));
    }

    public static DataFix create(Schema $$0, String $$1, UnaryOperator<String> $$2) {
        return new BlockEntityRenameFix($$0, $$1, $$2);
    }
}

