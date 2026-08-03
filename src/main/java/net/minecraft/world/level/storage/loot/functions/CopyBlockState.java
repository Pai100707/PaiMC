package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyBlockState extends LootItemConditionalFunction {
   public static final MapCodec<CopyBlockState> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter($$0x -> $$0x.block),
               Codec.STRING.listOf().fieldOf("properties").forGetter($$0x -> $$0x.properties.stream().map(Property::getName).toList())
            )
         )
         .apply($$0, CopyBlockState::new)
   );
   private final Holder<Block> block;
   private final Set<Property<?>> properties;

   CopyBlockState(List<LootItemCondition> $$0, Holder<Block> $$1, Set<Property<?>> $$2) {
      super($$0);
      this.block = $$1;
      this.properties = $$2;
   }

   private CopyBlockState(List<LootItemCondition> $$0, Holder<Block> $$1, List<String> $$2) {
      this($$0, $$1, $$2.stream().map(((Block)$$1.value()).getStateDefinition()::getProperty).filter(Objects::nonNull).collect(Collectors.toSet()));
   }

   @Override
   public LootItemFunctionType<CopyBlockState> getType() {
      return LootItemFunctions.COPY_STATE;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(LootContextParams.BLOCK_STATE);
   }

   @Override
   protected ItemStack run(ItemStack $$0, LootContext $$1) {
      BlockState $$2 = $$1.getOptionalParameter(LootContextParams.BLOCK_STATE);
      if ($$2 != null) {
         $$0.update(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY, $$1x -> {
            for (Property<?> $$2x : this.properties) {
               if ($$2.hasProperty($$2x)) {
                  $$1x = $$1x.with($$2x, $$2);
               }
            }

            return $$1x;
         });
      }

      return $$0;
   }

   public static CopyBlockState.Builder copyState(Block $$0) {
      return new CopyBlockState.Builder($$0);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<CopyBlockState.Builder> {
      private final Holder<Block> block;
      private final com.google.common.collect.ImmutableSet.Builder<Property<?>> properties = ImmutableSet.builder();

      Builder(Block $$0) {
         this.block = $$0.builtInRegistryHolder();
      }

      public CopyBlockState.Builder copy(Property<?> $$0) {
         if (!((Block)this.block.value()).getStateDefinition().getProperties().contains($$0)) {
            throw new IllegalStateException("Property " + $$0 + " is not present on block " + this.block);
         } else {
            this.properties.add($$0);
            return this;
         }
      }

      protected CopyBlockState.Builder getThis() {
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new CopyBlockState(this.getConditions(), this.block, this.properties.build());
      }
   }
}
