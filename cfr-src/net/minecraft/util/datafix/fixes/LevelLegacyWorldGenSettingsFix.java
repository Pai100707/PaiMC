/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class LevelLegacyWorldGenSettingsFix
extends DataFix {
    private static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
    private static final List<String> OLD_SETTINGS_KEYS = List.of("RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest");

    public LevelLegacyWorldGenSettingsFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LevelLegacyWorldGenSettingsFix", this.getInputSchema().getType(References.LEVEL), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> {
            Dynamic $$1 = $$0.get(WORLD_GEN_SETTINGS).orElseEmptyMap();
            for (String $$2 : OLD_SETTINGS_KEYS) {
                Optional $$3 = $$0.get($$2).result();
                if (!$$3.isPresent()) continue;
                $$0 = $$0.remove($$2);
                $$1 = $$1.set($$2, (Dynamic)$$3.get());
            }
            return $$0.set(WORLD_GEN_SETTINGS, $$1);
        }));
    }
}

