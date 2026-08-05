/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class BlockRenameFix
extends DataFix {
    private final String name;

    public BlockRenameFix(Schema $$0, String $$1) {
        super($$0, false);
        this.name = $$1;
    }

    public TypeRewriteRule makeRule() {
        Type $$1;
        Type $$03 = this.getInputSchema().getType(References.BLOCK_NAME);
        if (!Objects.equals($$03, $$1 = DSL.named((String)References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString()))) {
            throw new IllegalStateException("block type is not what was expected.");
        }
        TypeRewriteRule $$2 = this.fixTypeEverywhere(this.name + " for block", $$1, $$02 -> $$0 -> $$0.mapSecond(this::renameBlock));
        TypeRewriteRule $$3 = this.fixTypeEverywhereTyped(this.name + " for block_state", this.getInputSchema().getType(References.BLOCK_STATE), $$0 -> $$0.update(DSL.remainderFinder(), this::fixBlockState));
        TypeRewriteRule $$4 = this.fixTypeEverywhereTyped(this.name + " for flat_block_state", this.getInputSchema().getType(References.FLAT_BLOCK_STATE), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> (Dynamic)DataFixUtils.orElse($$0.asString().result().map(this::fixFlatBlockState).map(arg_0 -> ((Dynamic)$$0).createString(arg_0)), (Object)$$0)));
        return TypeRewriteRule.seq((TypeRewriteRule)$$2, (TypeRewriteRule[])new TypeRewriteRule[]{$$3, $$4});
    }

    private Dynamic<?> fixBlockState(Dynamic<?> $$0) {
        Optional $$1 = $$0.get("Name").asString().result();
        if ($$1.isPresent()) {
            return $$0.set("Name", $$0.createString(this.renameBlock((String)$$1.get())));
        }
        return $$0;
    }

    private String fixFlatBlockState(String $$0) {
        int $$1 = $$0.indexOf(91);
        int $$2 = $$0.indexOf(123);
        int $$3 = $$0.length();
        if ($$1 > 0) {
            $$3 = $$1;
        }
        if ($$2 > 0) {
            $$3 = Math.min($$3, $$2);
        }
        String $$4 = $$0.substring(0, $$3);
        String $$5 = this.renameBlock($$4);
        return $$5 + $$0.substring($$3);
    }

    protected abstract String renameBlock(String var1);

    public static DataFix create(Schema $$0, String $$1, final Function<String, String> $$2) {
        return new BlockRenameFix($$0, $$1){

            @Override
            protected String renameBlock(String $$0) {
                return (String)$$2.apply($$0);
            }
        };
    }
}

