package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SlideDownBlockTrigger extends SimpleCriterionTrigger<SlideDownBlockTrigger.TriggerInstance> {
   @Override
   public Codec<SlideDownBlockTrigger.TriggerInstance> codec() {
      return SlideDownBlockTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, BlockState $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<StatePropertiesPredicate> state)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<SlideDownBlockTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            $$0 -> $$0.group(
                  EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SlideDownBlockTrigger.TriggerInstance::player),
                  BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(SlideDownBlockTrigger.TriggerInstance::block),
                  StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(SlideDownBlockTrigger.TriggerInstance::state)
               )
               .apply($$0, SlideDownBlockTrigger.TriggerInstance::new)
         )
         .validate(SlideDownBlockTrigger.TriggerInstance::validate);

      private static DataResult<SlideDownBlockTrigger.TriggerInstance> validate(SlideDownBlockTrigger.TriggerInstance $$0) {
         return $$0.block
            .<DataResult<SlideDownBlockTrigger.TriggerInstance>>flatMap(
               $$1 -> $$0.state
                  .<String>flatMap($$1x -> $$1x.checkState(((Block)$$1.value()).getStateDefinition()))
                  .map($$1x -> DataResult.error(() -> "Block" + $$1 + " has no property " + $$1x))
            )
            .orElseGet(() -> DataResult.success($$0));
      }

      public static net.minecraft.advancements.Criterion<SlideDownBlockTrigger.TriggerInstance> slidesDownBlock(Block $$0) {
         return net.minecraft.advancements.CriteriaTriggers.HONEY_BLOCK_SLIDE
            .createCriterion(new SlideDownBlockTrigger.TriggerInstance(Optional.empty(), Optional.of($$0.builtInRegistryHolder()), Optional.empty()));
      }

      public boolean matches(BlockState $$0) {
         return this.block.isPresent() && !$$0.is(this.block.get()) ? false : !this.state.isPresent() || this.state.get().matches($$0);
      }
   }
}
