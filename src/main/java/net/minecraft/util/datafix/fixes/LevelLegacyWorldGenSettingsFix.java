package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;

public class LevelLegacyWorldGenSettingsFix extends DataFix {
   private static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
   private static final List<String> OLD_SETTINGS_KEYS = List.of(
      "RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest"
   );

   public LevelLegacyWorldGenSettingsFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "LevelLegacyWorldGenSettingsFix", this.getInputSchema().getType(References.LEVEL), $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> {
            Dynamic<?> $$1 = $$0x.get("WorldGenSettings").orElseEmptyMap();

            for (String $$2 : OLD_SETTINGS_KEYS) {
               Optional<? extends Dynamic<?>> $$3 = $$0x.get($$2).result();
               if ($$3.isPresent()) {
                  $$0x = $$0x.remove($$2);
                  $$1 = $$1.set($$2, $$3.get());
               }
            }

            return $$0x.set("WorldGenSettings", $$1);
         })
      );
   }
}
