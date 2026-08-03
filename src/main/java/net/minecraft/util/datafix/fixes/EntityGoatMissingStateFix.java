package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class EntityGoatMissingStateFix extends NamedEntityFix {
   public EntityGoatMissingStateFix(Schema $$0) {
      super($$0, false, "EntityGoatMissingStateFix", References.ENTITY, "minecraft:goat");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), $$0x -> $$0x.set("HasLeftHorn", $$0x.createBoolean(true)).set("HasRightHorn", $$0x.createBoolean(true)));
   }
}
