package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class AnyBlockInteractionTrigger extends SimpleCriterionTrigger<AnyBlockInteractionTrigger.TriggerInstance> {
   @Override
   public Codec<AnyBlockInteractionTrigger.TriggerInstance> codec() {
      return AnyBlockInteractionTrigger.TriggerInstance.CODEC;
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
      public static final Codec<AnyBlockInteractionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(AnyBlockInteractionTrigger.TriggerInstance::player),
               ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(AnyBlockInteractionTrigger.TriggerInstance::location)
            )
            .apply($$0, AnyBlockInteractionTrigger.TriggerInstance::new)
      );

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
