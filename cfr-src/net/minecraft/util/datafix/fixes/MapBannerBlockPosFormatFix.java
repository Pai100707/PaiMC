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
 *  com.mojang.datafixers.types.templates.List$ListType
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class MapBannerBlockPosFormatFix
extends DataFix {
    public MapBannerBlockPosFormatFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA);
        OpticFinder $$1 = $$0.findField("data");
        OpticFinder $$2 = $$1.type().findField("banners");
        OpticFinder $$32 = DSL.typeFinder((Type)((List.ListType)$$2.type()).getElement());
        return this.fixTypeEverywhereTyped("MapBannerBlockPosFormatFix", $$0, $$3 -> $$3.updateTyped($$1, $$2 -> $$2.updateTyped($$2, $$1 -> $$1.updateTyped($$32, $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.update("Pos", ExtraDataFixUtils::fixBlockPos))))));
    }
}

