/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;

public class LevelUUIDFix
extends AbstractUUIDFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public LevelUUIDFix(Schema $$0) {
        super($$0, References.LEVEL);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(this.typeReference);
        OpticFinder $$1 = $$0.findField("CustomBossEvents");
        OpticFinder $$22 = DSL.typeFinder((Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"Name", (Type)this.getInputSchema().getTypeRaw(References.TEXT_COMPONENT))), (Type)DSL.remainderType()));
        return this.fixTypeEverywhereTyped("LevelUUIDFix", $$0, $$2 -> $$2.update(DSL.remainderFinder(), $$0 -> {
            $$0 = this.updateDragonFight((Dynamic<?>)$$0);
            $$0 = this.updateWanderingTrader((Dynamic<?>)$$0);
            return $$0;
        }).updateTyped($$1, $$1 -> $$1.updateTyped($$22, $$0 -> $$0.update(DSL.remainderFinder(), this::updateCustomBossEvent))));
    }

    private Dynamic<?> updateWanderingTrader(Dynamic<?> $$0) {
        return LevelUUIDFix.replaceUUIDString($$0, "WanderingTraderId", "WanderingTraderId").orElse($$0);
    }

    private Dynamic<?> updateDragonFight(Dynamic<?> $$0) {
        return $$0.update("DimensionData", $$02 -> $$02.updateMapValues($$0 -> $$0.mapSecond($$02 -> $$02.update("DragonFight", $$0 -> LevelUUIDFix.replaceUUIDLeastMost($$0, "DragonUUID", "Dragon").orElse((Dynamic<?>)$$0)))));
    }

    private Dynamic<?> updateCustomBossEvent(Dynamic<?> $$0) {
        return $$0.update("Players", $$1 -> $$0.createList($$1.asStream().map($$0 -> LevelUUIDFix.createUUIDFromML($$0).orElseGet(() -> {
            LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
            return $$0;
        }))));
    }
}

