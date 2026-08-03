package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class DefaultBlockInteractionTrigger extends SimpleCriterionTrigger<DefaultBlockInteractionTrigger.TriggerInstance> {
   @Override
   public Codec<DefaultBlockInteractionTrigger.TriggerInstance> codec() {
      return DefaultBlockInteractionTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, BlockPos $$1) {
      ServerLevel $$2 = $$0.level();
      BlockState $$3 = $$2.getBlockState($$1);
      LootParams $$4 = new Builder($$2)
         .withParameter(LootContextParams.ORIGIN, $$1.getCenter())
         .withParameter(LootContextParams.THIS_ENTITY, $$0)
         .withParameter(LootContextParams.BLOCK_STATE, $$3)
         .create(LootContextParamSets.BLOCK_USE);
      LootContext $$5 = new net.minecraft.world.level.storage.loot.LootContext.Builder($$4).create(Optional.empty());
      this.trigger($$0, $$1x -> $$1x.matches($$5));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<DefaultBlockInteractionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(DefaultBlockInteractionTrigger.TriggerInstance::player),
               ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(DefaultBlockInteractionTrigger.TriggerInstance::location)
            )
            .apply($$0, DefaultBlockInteractionTrigger.TriggerInstance::new)
      );

      public boolean matches(LootContext $$0) {
         return this.location.isEmpty() || this.location.get().matches($$0);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         this.location.ifPresent($$1 -> $$0.validate($$1, LootContextParamSets.BLOCK_USE, "location"));
      }
   }
}
