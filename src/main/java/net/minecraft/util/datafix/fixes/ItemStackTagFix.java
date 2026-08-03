package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class ItemStackTagFix extends DataFix {
   private final String name;
   private final Predicate<String> idFilter;

   public ItemStackTagFix(Schema $$0, String $$1, Predicate<String> $$2) {
      super($$0, false);
      this.name = $$1;
      this.idFilter = $$2;
   }

   public final TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.ITEM_STACK);
      return this.fixTypeEverywhereTyped(this.name, $$0, createFixer($$0, this.idFilter, this::fixItemStackTag));
   }

   public static UnaryOperator<Typed<?>> createFixer(Type<?> $$0, Predicate<String> $$1, UnaryOperator<Typed<?>> $$2) {
      OpticFinder<Pair<String, String>> $$3 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> $$4 = $$0.findField("tag");
      return $$4x -> {
         Optional<Pair<String, String>> $$5 = $$4x.getOptional($$3);
         return $$5.isPresent() && $$1.test((String)$$5.get().getSecond()) ? $$4x.updateTyped($$4, $$2) : $$4x;
      };
   }

   protected abstract Typed<?> fixItemStackTag(Typed<?> var1);
}
