package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class ItemUsedOnLocationTrigger extends SimpleCriterionTrigger<ItemUsedOnLocationTrigger.TriggerInstance> {
   @Override
   public Codec<ItemUsedOnLocationTrigger.TriggerInstance> codec() {
      return ItemUsedOnLocationTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, BlockPos $$1, ItemStack $$2) {
      ServerLevel $$3 = $$0.level();
      BlockState $$4 = $$3.getBlockState($$1);
      LootParams $$5 = new Builder($$3)
         .withParameter(LootContextParams.ORIGIN, $$1.getCenter())
         .withParameter(LootContextParams.THIS_ENTITY, $$0)
         .withParameter(LootContextParams.BLOCK_STATE, $$4)
         .withParameter(LootContextParams.TOOL, $$2)
         .create(LootContextParamSets.ADVANCEMENT_LOCATION);
      LootContext $$6 = new net.minecraft.world.level.storage.loot.LootContext.Builder($$5).create(Optional.empty());
      this.trigger($$0, $$1x -> $$1x.matches($$6));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<ItemUsedOnLocationTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ItemUsedOnLocationTrigger.TriggerInstance::player),
               ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(ItemUsedOnLocationTrigger.TriggerInstance::location)
            )
            .apply($$0, ItemUsedOnLocationTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlock(Block $$0) {
         ContextAwarePredicate $$1 = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).build());
         return net.minecraft.advancements.CriteriaTriggers.PLACED_BLOCK
            .createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of($$1)));
      }

      public static net.minecraft.advancements.Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlock(
         net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder... $$0
      ) {
         ContextAwarePredicate $$1 = ContextAwarePredicate.create(
            Arrays.stream($$0).map(net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder::build).toArray(LootItemCondition[]::new)
         );
         return net.minecraft.advancements.CriteriaTriggers.PLACED_BLOCK
            .createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of($$1)));
      }

      public static <T extends Comparable<T>> net.minecraft.advancements.Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlockWithProperties(
         Block $$0, Property<T> $$1, String $$2
      ) {
         StatePropertiesPredicate.Builder $$3 = StatePropertiesPredicate.Builder.properties().hasProperty($$1, $$2);
         ContextAwarePredicate $$4 = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties($$3).build());
         return net.minecraft.advancements.CriteriaTriggers.PLACED_BLOCK
            .createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of($$4)));
      }

      public static net.minecraft.advancements.Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlockWithProperties(
         Block $$0, Property<Boolean> $$1, boolean $$2
      ) {
         return placedBlockWithProperties($$0, $$1, String.valueOf($$2));
      }

      public static net.minecraft.advancements.Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlockWithProperties(
         Block $$0, Property<Integer> $$1, int $$2
      ) {
         return placedBlockWithProperties($$0, $$1, String.valueOf($$2));
      }

      public static <T extends Comparable<T> & StringRepresentable> net.minecraft.advancements.Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlockWithProperties(
         Block $$0, Property<T> $$1, T $$2
      ) {
         return placedBlockWithProperties($$0, $$1, $$2.getSerializedName());
      }

      private static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnLocation(LocationPredicate.Builder $$0, ItemPredicate.Builder $$1) {
         ContextAwarePredicate $$2 = ContextAwarePredicate.create(LocationCheck.checkLocation($$0).build(), MatchTool.toolMatches($$1).build());
         return new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of($$2));
      }

      public static net.minecraft.advancements.Criterion<ItemUsedOnLocationTrigger.TriggerInstance> itemUsedOnBlock(
         LocationPredicate.Builder $$0, ItemPredicate.Builder $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(itemUsedOnLocation($$0, $$1));
      }

      public static net.minecraft.advancements.Criterion<ItemUsedOnLocationTrigger.TriggerInstance> allayDropItemOnBlock(
         LocationPredicate.Builder $$0, ItemPredicate.Builder $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.createCriterion(itemUsedOnLocation($$0, $$1));
      }

      public boolean matches(LootContext $$0) {
         return this.location.isEmpty() || this.location.get().matches($$0);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         this.location.ifPresent($$1 -> $$0.validate($$1, LootContextParamSets.ADVANCEMENT_LOCATION, "location"));
      }
   }
}
