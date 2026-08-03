package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter.ElementReferencePathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final MapCodec<FunctionReference> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0).and(ResourceKey.codec(Registries.ITEM_MODIFIER).fieldOf("name").forGetter($$0x -> $$0x.name)).apply($$0, FunctionReference::new)
   );
   private final ResourceKey<LootItemFunction> name;

   private FunctionReference(List<LootItemCondition> $$0, ResourceKey<LootItemFunction> $$1) {
      super($$0);
      this.name = $$1;
   }

   @Override
   public LootItemFunctionType<FunctionReference> getType() {
      return LootItemFunctions.REFERENCE;
   }

   @Override
   public void validate(ValidationContext $$0) {
      if (!$$0.allowsReferences()) {
         $$0.reportProblem(new ValidationContext.ReferenceNotAllowedProblem(this.name));
      } else if ($$0.hasVisitedElement(this.name)) {
         $$0.reportProblem(new ValidationContext.RecursiveReferenceProblem(this.name));
      } else {
         super.validate($$0);
         $$0.resolver()
            .get(this.name)
            .ifPresentOrElse(
               $$1 -> ((LootItemFunction)$$1.value()).validate($$0.enterElement(new ElementReferencePathElement(this.name), this.name)),
               () -> $$0.reportProblem(new ValidationContext.MissingReferenceProblem(this.name))
            );
      }
   }

   @Override
   protected ItemStack run(ItemStack $$0, LootContext $$1) {
      LootItemFunction $$2 = $$1.getResolver().get(this.name).<LootItemFunction>map(Holder::value).orElse(null);
      if ($$2 == null) {
         LOGGER.warn("Unknown function: {}", this.name.identifier());
         return $$0;
      } else {
         LootContext.VisitedEntry<?> $$3 = LootContext.createVisitedEntry($$2);
         if ($$1.pushVisitedElement($$3)) {
            ItemStack var5;
            try {
               var5 = $$2.apply($$0, $$1);
            } finally {
               $$1.popVisitedElement($$3);
            }

            return var5;
         } else {
            LOGGER.warn("Detected infinite loop in loot tables");
            return $$0;
         }
      }
   }

   public static LootItemConditionalFunction.Builder<?> functionReference(ResourceKey<LootItemFunction> $$0) {
      return simpleBuilder($$1 -> new FunctionReference($$1, $$0));
   }
}
