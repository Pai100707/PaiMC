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
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlockNameFlatteningFix
extends DataFix {
    public BlockNameFlatteningFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.BLOCK_NAME);
        Type $$1 = this.getOutputSchema().getType(References.BLOCK_NAME);
        Type $$2 = DSL.named((String)References.BLOCK_NAME.typeName(), (Type)DSL.or((Type)DSL.intType(), NamespacedSchema.namespacedString()));
        Type $$3 = DSL.named((String)References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals($$0, $$2) || !Objects.equals($$1, $$3)) {
            throw new IllegalStateException("Expected and actual types don't match.");
        }
        return this.fixTypeEverywhere("BlockNameFlatteningFix", $$2, $$3, $$02 -> $$0 -> $$0.mapSecond($$02 -> (String)$$02.map(BlockStateData::upgradeBlock, $$0 -> BlockStateData.upgradeBlock(NamespacedSchema.ensureNamespaced($$0)))));
    }
}

