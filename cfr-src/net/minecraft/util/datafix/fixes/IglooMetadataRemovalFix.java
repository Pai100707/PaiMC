/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class IglooMetadataRemovalFix
extends DataFix {
    public IglooMetadataRemovalFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    protected TypeRewriteRule makeRule() {
        Type $$02 = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
        return this.fixTypeEverywhereTyped("IglooMetadataRemovalFix", $$02, $$0 -> $$0.update(DSL.remainderFinder(), IglooMetadataRemovalFix::fixTag));
    }

    private static <T> Dynamic<T> fixTag(Dynamic<T> $$02) {
        boolean $$1 = $$02.get("Children").asStreamOpt().map($$0 -> $$0.allMatch(IglooMetadataRemovalFix::isIglooPiece)).result().orElse(false);
        if ($$1) {
            return $$02.set("id", $$02.createString("Igloo")).remove("Children");
        }
        return $$02.update("Children", IglooMetadataRemovalFix::removeIglooPieces);
    }

    private static <T> Dynamic<T> removeIglooPieces(Dynamic<T> $$0) {
        return $$0.asStreamOpt().map($$02 -> $$02.filter($$0 -> !IglooMetadataRemovalFix.isIglooPiece($$0))).map(arg_0 -> $$0.createList(arg_0)).result().orElse($$0);
    }

    private static boolean isIglooPiece(Dynamic<?> $$0) {
        return $$0.get("id").asString("").equals("Iglu");
    }
}

