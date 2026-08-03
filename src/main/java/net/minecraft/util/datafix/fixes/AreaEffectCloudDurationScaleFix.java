package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class AreaEffectCloudDurationScaleFix extends NamedEntityFix {
   public AreaEffectCloudDurationScaleFix(Schema $$0) {
      super($$0, false, "AreaEffectCloudDurationScaleFix", References.ENTITY, "minecraft:area_effect_cloud");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), $$0x -> $$0x.set("potion_duration_scale", $$0x.createFloat(0.25F)));
   }
}
