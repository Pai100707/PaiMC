package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class RedstoneWireConnectionsFix extends DataFix {
   public RedstoneWireConnectionsFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      Schema $$0 = this.getInputSchema();
      return this.fixTypeEverywhereTyped(
         "RedstoneConnectionsFix", $$0.getType(References.BLOCK_STATE), $$0x -> $$0x.update(DSL.remainderFinder(), this::updateRedstoneConnections)
      );
   }

   private <T> Dynamic<T> updateRedstoneConnections(Dynamic<T> $$0) {
      boolean $$1 = $$0.get("Name").asString().result().filter("minecraft:redstone_wire"::equals).isPresent();
      return !$$1
         ? $$0
         : $$0.update(
            "Properties",
            $$0x -> {
               String $$1x = $$0x.get("east").asString("none");
               String $$2 = $$0x.get("west").asString("none");
               String $$3 = $$0x.get("north").asString("none");
               String $$4 = $$0x.get("south").asString("none");
               boolean $$5 = isConnected($$1x) || isConnected($$2);
               boolean $$6 = isConnected($$3) || isConnected($$4);
               String $$7 = !isConnected($$1x) && !$$6 ? "side" : $$1x;
               String $$8 = !isConnected($$2) && !$$6 ? "side" : $$2;
               String $$9 = !isConnected($$3) && !$$5 ? "side" : $$3;
               String $$10 = !isConnected($$4) && !$$5 ? "side" : $$4;
               return $$0x.update("east", $$1xx -> $$1xx.createString($$7))
                  .update("west", $$1xx -> $$1xx.createString($$8))
                  .update("north", $$1xx -> $$1xx.createString($$9))
                  .update("south", $$1xx -> $$1xx.createString($$10));
            }
         );
   }

   private static boolean isConnected(String $$0) {
      return !"none".equals($$0);
   }
}
