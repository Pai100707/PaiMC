package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class VillagerSetCanPickUpLootFix extends NamedEntityFix {
   private static final String CAN_PICK_UP_LOOT = "CanPickUpLoot";

   public VillagerSetCanPickUpLootFix(Schema $$0) {
      super($$0, true, "Villager CanPickUpLoot default value", References.ENTITY, "Villager");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), VillagerSetCanPickUpLootFix::fixValue);
   }

   private static Dynamic<?> fixValue(Dynamic<?> $$0) {
      return $$0.set("CanPickUpLoot", $$0.createBoolean(true));
   }
}
