package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityShulkerColorFix extends NamedEntityFix {
   public EntityShulkerColorFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "EntityShulkerColorFix", References.ENTITY, "minecraft:shulker");
   }

   public Dynamic<?> fixTag(Dynamic<?> $$0) {
      return $$0.get("Color").map(Dynamic::asNumber).result().isEmpty() ? $$0.set("Color", $$0.createByte((byte)10)) : $$0;
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), this::fixTag);
   }
}
