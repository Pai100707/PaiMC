package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import java.util.UUID;

public class EntityStringUuidFix extends DataFix {
   public EntityStringUuidFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "EntityStringUuidFix",
         this.getInputSchema().getType(References.ENTITY),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> {
               Optional<String> $$1 = $$0x.get("UUID").asString().result();
               if ($$1.isPresent()) {
                  UUID $$2 = UUID.fromString($$1.get());
                  return $$0x.remove("UUID")
                     .set("UUIDMost", $$0x.createLong($$2.getMostSignificantBits()))
                     .set("UUIDLeast", $$0x.createLong($$2.getLeastSignificantBits()));
               } else {
                  return $$0x;
               }
            }
         )
      );
   }
}
