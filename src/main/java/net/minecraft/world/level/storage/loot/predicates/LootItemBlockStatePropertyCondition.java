package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record LootItemBlockStatePropertyCondition(Holder<Block> block, Optional<StatePropertiesPredicate> properties) implements LootItemCondition {
   public static final MapCodec<LootItemBlockStatePropertyCondition> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(LootItemBlockStatePropertyCondition::block),
               StatePropertiesPredicate.CODEC.optionalFieldOf("properties").forGetter(LootItemBlockStatePropertyCondition::properties)
            )
            .apply($$0, LootItemBlockStatePropertyCondition::new)
      )
      .validate(LootItemBlockStatePropertyCondition::validate);

   private static DataResult<LootItemBlockStatePropertyCondition> validate(LootItemBlockStatePropertyCondition $$0) {
      return $$0.properties()
         .flatMap($$1 -> $$1.checkState(((Block)$$0.block().value()).getStateDefinition()))
         .map($$1 -> DataResult.error(() -> "Block " + $$0.block() + " has no property" + $$1))
         .orElse(DataResult.success($$0));
   }

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.BLOCK_STATE_PROPERTY;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(LootContextParams.BLOCK_STATE);
   }

   public boolean test(LootContext $$0) {
      BlockState $$1 = $$0.getOptionalParameter(LootContextParams.BLOCK_STATE);
      return $$1 != null && $$1.is(this.block) && (this.properties.isEmpty() || this.properties.get().matches($$1));
   }

   public static LootItemBlockStatePropertyCondition.Builder hasBlockStateProperties(Block $$0) {
      return new LootItemBlockStatePropertyCondition.Builder($$0);
   }

   public static class Builder implements LootItemCondition.Builder {
      private final Holder<Block> block;
      private Optional<StatePropertiesPredicate> properties = Optional.empty();

      public Builder(Block $$0) {
         this.block = $$0.builtInRegistryHolder();
      }

      public LootItemBlockStatePropertyCondition.Builder setProperties(net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder $$0) {
         this.properties = $$0.build();
         return this;
      }

      @Override
      public LootItemCondition build() {
         return new LootItemBlockStatePropertyCondition(this.block, this.properties);
      }
   }
}
