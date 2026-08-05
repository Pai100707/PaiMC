/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class WeaponSmithChestLootTableFix
extends NamedEntityFix {
    public WeaponSmithChestLootTableFix(Schema $$0, boolean $$1) {
        super($$0, $$1, "WeaponSmithChestLootTableFix", References.BLOCK_ENTITY, "minecraft:chest");
    }

    @Override
    protected Typed<?> fix(Typed<?> $$02) {
        return $$02.update(DSL.remainderFinder(), $$0 -> {
            String $$1 = $$0.get("LootTable").asString("");
            return $$1.equals("minecraft:chests/village_blacksmith") ? $$0.set("LootTable", $$0.createString("minecraft:chests/village/village_weaponsmith")) : $$0;
        });
    }
}

