package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.stream.IntStream;

public class WorldSpawnDataFix extends DataFix {
   public WorldSpawnDataFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "WorldSpawnDataFix",
         this.getInputSchema().getType(References.LEVEL),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> {
               int $$1 = $$0x.get("SpawnX").asInt(0);
               int $$2 = $$0x.get("SpawnY").asInt(0);
               int $$3 = $$0x.get("SpawnZ").asInt(0);
               float $$4 = $$0x.get("SpawnAngle").asFloat(0.0F);
               Dynamic<?> $$5 = $$0x.emptyMap()
                  .set("dimension", $$0x.createString("minecraft:overworld"))
                  .set("pos", $$0x.createIntList(IntStream.of($$1, $$2, $$3)))
                  .set("yaw", $$0x.createFloat($$4))
                  .set("pitch", $$0x.createFloat(0.0F));
               $$0x = $$0x.remove("SpawnX").remove("SpawnY").remove("SpawnZ").remove("SpawnAngle");
               return $$0x.set("spawn", $$5);
            }
         )
      );
   }
}
