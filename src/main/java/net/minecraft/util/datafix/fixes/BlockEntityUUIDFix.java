package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityUUIDFix extends AbstractUUIDFix {
   public BlockEntityUUIDFix(Schema $$0) {
      super($$0, References.BLOCK_ENTITY);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("BlockEntityUUIDFix", this.getInputSchema().getType(this.typeReference), $$0 -> {
         $$0 = this.updateNamedChoice($$0, "minecraft:conduit", this::updateConduit);
         return this.updateNamedChoice($$0, "minecraft:skull", this::updateSkull);
      });
   }

   private Dynamic<?> updateSkull(Dynamic<?> $$0) {
      return $$0.get("Owner")
         .get()
         .map($$0x -> replaceUUIDString($$0x, "Id", "Id").orElse($$0x))
         .map($$1 -> $$0.remove("Owner").set("SkullOwner", $$1))
         .result()
         .orElse($$0);
   }

   private Dynamic<?> updateConduit(Dynamic<?> $$0) {
      return replaceUUIDMLTag($$0, "target_uuid", "Target").orElse($$0);
   }
}
