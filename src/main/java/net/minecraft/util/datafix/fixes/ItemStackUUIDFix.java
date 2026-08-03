package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackUUIDFix extends AbstractUUIDFix {
   public ItemStackUUIDFix(Schema $$0) {
      super($$0, References.ITEM_STACK);
   }

   public TypeRewriteRule makeRule() {
      OpticFinder<Pair<String, String>> $$0 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      return this.fixTypeEverywhereTyped("ItemStackUUIDFix", this.getInputSchema().getType(this.typeReference), $$1 -> {
         OpticFinder<?> $$2 = $$1.getType().findField("tag");
         return $$1.updateTyped($$2, $$2x -> $$2x.update(DSL.remainderFinder(), $$2xx -> {
            $$2xx = this.updateAttributeModifiers($$2xx);
            if ($$1.getOptional($$0).map($$0xxxx -> "minecraft:player_head".equals($$0xxxx.getSecond())).orElse(false)) {
               $$2xx = this.updateSkullOwner($$2xx);
            }

            return $$2xx;
         }));
      });
   }

   private Dynamic<?> updateAttributeModifiers(Dynamic<?> $$0) {
      return $$0.update(
         "AttributeModifiers", $$1 -> $$0.createList($$1.asStream().map($$0xx -> (Dynamic)replaceUUIDLeastMost($$0xx, "UUID", "UUID").orElse($$0xx)))
      );
   }

   private Dynamic<?> updateSkullOwner(Dynamic<?> $$0) {
      return $$0.update("SkullOwner", $$0x -> replaceUUIDString($$0x, "Id", "Id").orElse($$0x));
   }
}
