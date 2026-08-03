package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class ColorlessShulkerEntityFix extends NamedEntityFix {
   public ColorlessShulkerEntityFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "Colorless shulker entity fix", References.ENTITY, "minecraft:shulker");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), $$0x -> $$0x.get("Color").asInt(0) == 10 ? $$0x.set("Color", $$0x.createByte((byte)16)) : $$0x);
   }
}
