package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityArmorStandSilentFix extends NamedEntityFix {
   public EntityArmorStandSilentFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "EntityArmorStandSilentFix", References.ENTITY, "ArmorStand");
   }

   public Dynamic<?> fixTag(Dynamic<?> $$0) {
      return $$0.get("Silent").asBoolean(false) && !$$0.get("Marker").asBoolean(false) ? $$0.remove("Silent") : $$0;
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), this::fixTag);
   }
}
