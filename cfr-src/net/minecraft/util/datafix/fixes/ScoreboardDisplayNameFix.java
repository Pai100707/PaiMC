/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
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
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class ScoreboardDisplayNameFix
extends DataFix {
    private final String name;
    private final DSL.TypeReference type;

    public ScoreboardDisplayNameFix(Schema $$0, String $$1, DSL.TypeReference $$2) {
        super($$0, false);
        this.name = $$1;
        this.type = $$2;
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(this.type);
        OpticFinder $$1 = $$0.findField("DisplayName");
        OpticFinder $$22 = DSL.typeFinder((Type)this.getInputSchema().getType(References.TEXT_COMPONENT));
        return this.fixTypeEverywhereTyped(this.name, $$0, $$2 -> $$2.updateTyped($$1, $$1 -> $$1.update($$22, $$0 -> $$0.mapSecond(LegacyComponentDataFixUtils::createTextComponentJson))));
    }
}

