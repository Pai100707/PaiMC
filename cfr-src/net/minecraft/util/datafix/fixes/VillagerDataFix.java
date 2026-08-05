/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class VillagerDataFix
extends NamedEntityFix {
    public VillagerDataFix(Schema $$0, String $$1) {
        super($$0, false, "Villager profession data fix (" + $$1 + ")", References.ENTITY, $$1);
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        Dynamic $$1 = (Dynamic)$$0.get(DSL.remainderFinder());
        return $$0.set(DSL.remainderFinder(), (Object)$$1.remove("Profession").remove("Career").remove("CareerLevel").set("VillagerData", $$1.createMap((Map)ImmutableMap.of((Object)$$1.createString("type"), (Object)$$1.createString("minecraft:plains"), (Object)$$1.createString("profession"), (Object)$$1.createString(VillagerDataFix.upgradeData($$1.get("Profession").asInt(0), $$1.get("Career").asInt(0))), (Object)$$1.createString("level"), (Object)((Dynamic)DataFixUtils.orElse((Optional)$$1.get("CareerLevel").result(), (Object)$$1.createInt(1)))))));
    }

    private static String upgradeData(int $$0, int $$1) {
        if ($$0 == 0) {
            if ($$1 == 2) {
                return "minecraft:fisherman";
            }
            if ($$1 == 3) {
                return "minecraft:shepherd";
            }
            if ($$1 == 4) {
                return "minecraft:fletcher";
            }
            return "minecraft:farmer";
        }
        if ($$0 == 1) {
            if ($$1 == 2) {
                return "minecraft:cartographer";
            }
            return "minecraft:librarian";
        }
        if ($$0 == 2) {
            return "minecraft:cleric";
        }
        if ($$0 == 3) {
            if ($$1 == 2) {
                return "minecraft:weaponsmith";
            }
            if ($$1 == 3) {
                return "minecraft:toolsmith";
            }
            return "minecraft:armorer";
        }
        if ($$0 == 4) {
            if ($$1 == 2) {
                return "minecraft:leatherworker";
            }
            return "minecraft:butcher";
        }
        if ($$0 == 5) {
            return "minecraft:nitwit";
        }
        return "minecraft:none";
    }
}

