package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.Map;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class StatsRenameFix extends DataFix {
   private final String name;
   private final Map<String, String> renames;

   public StatsRenameFix(Schema $$0, String $$1, Map<String, String> $$2) {
      super($$0, false);
      this.name = $$1;
      this.renames = $$2;
   }

   protected TypeRewriteRule makeRule() {
      return TypeRewriteRule.seq(this.createStatRule(), this.createCriteriaRule());
   }

   private TypeRewriteRule createCriteriaRule() {
      Type<?> $$0 = this.getOutputSchema().getType(References.OBJECTIVE);
      Type<?> $$1 = this.getInputSchema().getType(References.OBJECTIVE);
      OpticFinder<?> $$2 = $$1.findField("CriteriaType");
      TaggedChoiceType<?> $$3 = (TaggedChoiceType<?>)$$2.type()
         .findChoiceType("type", -1)
         .orElseThrow(() -> new IllegalStateException("Can't find choice type for criteria"));
      Type<?> $$4 = (Type<?>)$$3.types().get("minecraft:custom");
      if ($$4 == null) {
         throw new IllegalStateException("Failed to find custom criterion type variant");
      } else {
         OpticFinder<?> $$5 = DSL.namedChoice("minecraft:custom", $$4);
         OpticFinder<String> $$6 = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
         return this.fixTypeEverywhereTyped(
            this.name,
            $$1,
            $$0,
            $$3x -> $$3x.updateTyped($$2, $$2xx -> $$2xx.updateTyped($$5, $$1xxx -> $$1xxx.update($$6, $$0xxxx -> this.renames.getOrDefault($$0xxxx, $$0xxxx))))
         );
      }
   }

   private TypeRewriteRule createStatRule() {
      Type<?> $$0 = this.getOutputSchema().getType(References.STATS);
      Type<?> $$1 = this.getInputSchema().getType(References.STATS);
      OpticFinder<?> $$2 = $$1.findField("stats");
      OpticFinder<?> $$3 = $$2.type().findField("minecraft:custom");
      OpticFinder<String> $$4 = NamespacedSchema.namespacedString().finder();
      return this.fixTypeEverywhereTyped(
         this.name,
         $$1,
         $$0,
         $$3x -> $$3x.updateTyped($$2, $$2xx -> $$2xx.updateTyped($$3, $$1xxx -> $$1xxx.update($$4, $$0xxxx -> this.renames.getOrDefault($$0xxxx, $$0xxxx))))
      );
   }
}
