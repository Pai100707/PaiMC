package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class StriderGravityFix extends NamedEntityFix {
   public StriderGravityFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "StriderGravityFix", References.ENTITY, "minecraft:strider");
   }

   public Dynamic<?> fixTag(Dynamic<?> $$0) {
      return $$0.get("NoGravity").asBoolean(false) ? $$0.set("NoGravity", $$0.createBoolean(false)) : $$0;
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), this::fixTag);
   }
}
