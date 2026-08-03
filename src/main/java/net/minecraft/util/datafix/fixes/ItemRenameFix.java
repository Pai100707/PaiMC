package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class ItemRenameFix extends DataFix {
   private final String name;

   public ItemRenameFix(Schema $$0, String $$1) {
      super($$0, false);
      this.name = $$1;
   }

   public TypeRewriteRule makeRule() {
      Type<Pair<String, String>> $$0 = DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString());
      if (!Objects.equals(this.getInputSchema().getType(References.ITEM_NAME), $$0)) {
         throw new IllegalStateException("item name type is not what was expected.");
      } else {
         return this.fixTypeEverywhere(this.name, $$0, $$0x -> $$0xx -> $$0xx.mapSecond(this::fixItem));
      }
   }

   protected abstract String fixItem(String var1);

   public static DataFix create(Schema $$0, String $$1, final Function<String, String> $$2) {
      return new ItemRenameFix($$0, $$1) {
         @Override
         protected String fixItem(String $$0) {
            return $$2.apply($$0);
         }
      };
   }
}
