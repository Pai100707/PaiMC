package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityWolfColorFix extends NamedEntityFix {
   public EntityWolfColorFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "EntityWolfColorFix", References.ENTITY, "minecraft:wolf");
   }

   public Dynamic<?> fixTag(Dynamic<?> $$0) {
      return $$0.update("CollarColor", $$0x -> $$0x.createByte((byte)(15 - $$0x.asInt(0))));
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), this::fixTag);
   }
}
