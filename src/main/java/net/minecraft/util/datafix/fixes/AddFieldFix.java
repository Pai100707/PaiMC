package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class AddFieldFix extends DataFix {
   private final String name;
   private final TypeReference type;
   private final String fieldName;
   private final String[] path;
   private final Function<Dynamic<?>, Dynamic<?>> fieldGenerator;

   public AddFieldFix(Schema $$0, TypeReference $$1, String $$2, Function<Dynamic<?>, Dynamic<?>> $$3, String... $$4) {
      super($$0, false);
      this.name = "Adding field `" + $$2 + "` to type `" + $$1.typeName().toLowerCase(Locale.ROOT) + "`";
      this.type = $$1;
      this.fieldName = $$2;
      this.path = $$4;
      this.fieldGenerator = $$3;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         this.name,
         this.getInputSchema().getType(this.type),
         this.getOutputSchema().getType(this.type),
         $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> this.addField($$0x, 0))
      );
   }

   private Dynamic<?> addField(Dynamic<?> $$0, int $$1) {
      if ($$1 >= this.path.length) {
         return $$0.set(this.fieldName, this.fieldGenerator.apply($$0));
      } else {
         Optional<? extends Dynamic<?>> $$2 = $$0.get(this.path[$$1]).result();
         return $$2.isEmpty() ? $$0 : this.addField((Dynamic<?>)$$2.get(), $$1 + 1);
      }
   }
}
