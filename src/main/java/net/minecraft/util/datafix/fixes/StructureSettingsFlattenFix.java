package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

public class StructureSettingsFlattenFix extends DataFix {
   public StructureSettingsFlattenFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.WORLD_GEN_SETTINGS);
      OpticFinder<?> $$1 = $$0.findField("dimensions");
      return this.fixTypeEverywhereTyped(
         "StructureSettingsFlatten",
         $$0,
         $$1x -> $$1x.updateTyped(
            $$1,
            $$1xx -> net.minecraft.util.Util.writeAndReadTypedOrThrow(
               $$1xx, $$1.type(), $$0xxx -> $$0xxx.updateMapValues(StructureSettingsFlattenFix::fixDimension)
            )
         )
      );
   }

   private static Pair<Dynamic<?>, Dynamic<?>> fixDimension(Pair<Dynamic<?>, Dynamic<?>> $$0) {
      Dynamic<?> $$1 = (Dynamic<?>)$$0.getSecond();
      return Pair.of(
         (Dynamic)$$0.getFirst(),
         $$1.update("generator", $$0x -> $$0x.update("settings", $$0xx -> $$0xx.update("structures", StructureSettingsFlattenFix::fixStructures)))
      );
   }

   private static Dynamic<?> fixStructures(Dynamic<?> $$0) {
      Dynamic<?> $$1 = $$0.get("structures")
         .orElseEmptyMap()
         .updateMapValues($$1x -> $$1x.mapSecond($$1xx -> $$1xx.set("type", $$0.createString("minecraft:random_spread"))));
      return (Dynamic<?>)DataFixUtils.orElse(
         $$0.get("stronghold").result().map($$2 -> $$1.set("minecraft:stronghold", $$2.set("type", $$0.createString("minecraft:concentric_rings")))), $$1
      );
   }
}
