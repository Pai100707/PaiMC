package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityItemFrameDirectionFix extends NamedEntityFix {
   public EntityItemFrameDirectionFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "EntityItemFrameDirectionFix", References.ENTITY, "minecraft:item_frame");
   }

   public Dynamic<?> fixTag(Dynamic<?> $$0) {
      return $$0.set("Facing", $$0.createByte(direction2dTo3d($$0.get("Facing").asByte((byte)0))));
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), this::fixTag);
   }

   private static byte direction2dTo3d(byte $$0) {
      switch ($$0) {
         case 0:
            return 3;
         case 1:
            return 4;
         case 2:
         default:
            return 2;
         case 3:
            return 5;
      }
   }
}
