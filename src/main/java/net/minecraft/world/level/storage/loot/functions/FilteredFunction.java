package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.util.ProblemReporter.FieldPathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FilteredFunction extends LootItemConditionalFunction {
   public static final MapCodec<FilteredFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               ItemPredicate.CODEC.fieldOf("item_filter").forGetter($$0x -> $$0x.filter),
               LootItemFunctions.ROOT_CODEC.optionalFieldOf("on_pass").forGetter($$0x -> $$0x.onPass),
               LootItemFunctions.ROOT_CODEC.optionalFieldOf("on_fail").forGetter($$0x -> $$0x.onFail)
            )
         )
         .apply($$0, FilteredFunction::new)
   );
   private final ItemPredicate filter;
   private final Optional<LootItemFunction> onPass;
   private final Optional<LootItemFunction> onFail;

   FilteredFunction(List<LootItemCondition> $$0, ItemPredicate $$1, Optional<LootItemFunction> $$2, Optional<LootItemFunction> $$3) {
      super($$0);
      this.filter = $$1;
      this.onPass = $$2;
      this.onFail = $$3;
   }

   @Override
   public LootItemFunctionType<FilteredFunction> getType() {
      return LootItemFunctions.FILTERED;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      Optional<LootItemFunction> $$2 = this.filter.test($$0) ? this.onPass : this.onFail;
      return $$2.isPresent() ? $$2.get().apply($$0, $$1) : $$0;
   }

   @Override
   public void validate(ValidationContext $$0) {
      super.validate($$0);
      this.onPass.ifPresent($$1 -> $$1.validate($$0.forChild(new FieldPathElement("on_pass"))));
      this.onFail.ifPresent($$1 -> $$1.validate($$0.forChild(new FieldPathElement("on_fail"))));
   }

   public static FilteredFunction.Builder filtered(ItemPredicate $$0) {
      return new FilteredFunction.Builder($$0);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<FilteredFunction.Builder> {
      private final ItemPredicate itemPredicate;
      private Optional<LootItemFunction> onPass = Optional.empty();
      private Optional<LootItemFunction> onFail = Optional.empty();

      Builder(ItemPredicate $$0) {
         this.itemPredicate = $$0;
      }

      protected FilteredFunction.Builder getThis() {
         return this;
      }

      public FilteredFunction.Builder onPass(Optional<LootItemFunction> $$0) {
         this.onPass = $$0;
         return this;
      }

      public FilteredFunction.Builder onFail(Optional<LootItemFunction> $$0) {
         this.onFail = $$0;
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new FilteredFunction(this.getConditions(), this.itemPredicate, this.onPass, this.onFail);
      }
   }
}
