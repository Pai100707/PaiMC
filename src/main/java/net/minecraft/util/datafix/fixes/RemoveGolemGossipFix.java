package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class RemoveGolemGossipFix extends NamedEntityFix {
   public RemoveGolemGossipFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "Remove Golem Gossip Fix", References.ENTITY, "minecraft:villager");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), RemoveGolemGossipFix::fixValue);
   }

   private static Dynamic<?> fixValue(Dynamic<?> $$0) {
      return $$0.update("Gossips", $$1 -> $$0.createList($$1.asStream().filter($$0xx -> !$$0xx.get("Type").asString("").equals("golem"))));
   }
}
