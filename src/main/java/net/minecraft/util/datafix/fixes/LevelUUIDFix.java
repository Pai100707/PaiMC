package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import org.slf4j.Logger;

public class LevelUUIDFix extends AbstractUUIDFix {
   private static final Logger LOGGER = LogUtils.getLogger();

   public LevelUUIDFix(Schema $$0) {
      super($$0, References.LEVEL);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(this.typeReference);
      OpticFinder<?> $$1 = $$0.findField("CustomBossEvents");
      OpticFinder<?> $$2 = DSL.typeFinder(
         DSL.and(DSL.optional(DSL.field("Name", this.getInputSchema().getTypeRaw(References.TEXT_COMPONENT))), DSL.remainderType())
      );
      return this.fixTypeEverywhereTyped("LevelUUIDFix", $$0, $$2x -> $$2x.update(DSL.remainderFinder(), $$0xx -> {
         $$0xx = this.updateDragonFight($$0xx);
         return this.updateWanderingTrader($$0xx);
      }).updateTyped($$1, $$1xx -> $$1xx.updateTyped($$2, $$0xxx -> $$0xxx.update(DSL.remainderFinder(), this::updateCustomBossEvent))));
   }

   private Dynamic<?> updateWanderingTrader(Dynamic<?> $$0) {
      return replaceUUIDString($$0, "WanderingTraderId", "WanderingTraderId").orElse($$0);
   }

   private Dynamic<?> updateDragonFight(Dynamic<?> $$0) {
      return $$0.update(
         "DimensionData",
         $$0x -> $$0x.updateMapValues(
            $$0xx -> $$0xx.mapSecond($$0xxx -> $$0xxx.update("DragonFight", $$0xxxx -> replaceUUIDLeastMost($$0xxxx, "DragonUUID", "Dragon").orElse($$0xxxx)))
         )
      );
   }

   private Dynamic<?> updateCustomBossEvent(Dynamic<?> $$0) {
      return $$0.update("Players", $$1 -> $$0.createList($$1.asStream().map($$0xx -> (Dynamic)createUUIDFromML($$0xx).orElseGet(() -> {
         LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
         return $$0xx;
      }))));
   }
}
