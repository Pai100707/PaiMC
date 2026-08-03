package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;

public class PrimedTntBlockStateFixer extends NamedEntityWriteReadFix {
   public PrimedTntBlockStateFixer(Schema $$0) {
      super($$0, true, "PrimedTnt BlockState fixer", References.ENTITY, "minecraft:tnt");
   }

   private static <T> Dynamic<T> renameFuse(Dynamic<T> $$0) {
      Optional<Dynamic<T>> $$1 = $$0.get("Fuse").get().result();
      return $$1.isPresent() ? $$0.set("fuse", $$1.get()) : $$0;
   }

   private static <T> Dynamic<T> insertBlockState(Dynamic<T> $$0) {
      return $$0.set("block_state", $$0.createMap(Map.of($$0.createString("Name"), $$0.createString("minecraft:tnt"))));
   }

   @Override
   protected <T> Dynamic<T> fix(Dynamic<T> $$0) {
      return renameFuse(insertBlockState($$0));
   }
}
