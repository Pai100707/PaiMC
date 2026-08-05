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
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.fixes.References;

public class EquippableAssetRenameFix
extends DataFix {
    public EquippableAssetRenameFix(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.DATA_COMPONENTS);
        OpticFinder $$12 = $$0.findField("minecraft:equippable");
        return this.fixTypeEverywhereTyped("equippable asset rename fix", $$0, $$1 -> $$1.updateTyped($$12, $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.renameField("model", "asset_id"))));
    }
}

