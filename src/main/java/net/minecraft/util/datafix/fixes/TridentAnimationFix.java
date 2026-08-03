package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import org.jspecify.annotations.Nullable;

public class TridentAnimationFix extends DataComponentRemainderFix {
   public TridentAnimationFix(Schema $$0) {
      super($$0, "TridentAnimationFix", "minecraft:consumable");
   }

   @Nullable
   @Override
   protected <T> Dynamic<T> fixComponent(Dynamic<T> $$0) {
      return $$0.update("animation", $$0x -> {
         String $$1 = $$0x.asString().result().orElse("");
         return "spear".equals($$1) ? $$0x.createString("trident") : $$0x;
      });
   }
}
