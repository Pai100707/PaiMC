package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BeeNestDestroyedTrigger extends SimpleCriterionTrigger<BeeNestDestroyedTrigger.TriggerInstance> {
   @Override
   public Codec<BeeNestDestroyedTrigger.TriggerInstance> codec() {
      return BeeNestDestroyedTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, BlockState $$1, ItemStack $$2, int $$3) {
      this.trigger($$0, $$3x -> $$3x.matches($$1, $$2, $$3));
   }

   public record TriggerInstance(
      Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<ItemPredicate> item, MinMaxBounds.Ints beesInside
   ) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<BeeNestDestroyedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(BeeNestDestroyedTrigger.TriggerInstance::player),
               BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(BeeNestDestroyedTrigger.TriggerInstance::block),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(BeeNestDestroyedTrigger.TriggerInstance::item),
               MinMaxBounds.Ints.CODEC.optionalFieldOf("num_bees_inside", MinMaxBounds.Ints.ANY).forGetter(BeeNestDestroyedTrigger.TriggerInstance::beesInside)
            )
            .apply($$0, BeeNestDestroyedTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<BeeNestDestroyedTrigger.TriggerInstance> destroyedBeeNest(
         Block $$0, ItemPredicate.Builder $$1, MinMaxBounds.Ints $$2
      ) {
         return net.minecraft.advancements.CriteriaTriggers.BEE_NEST_DESTROYED
            .createCriterion(
               new BeeNestDestroyedTrigger.TriggerInstance(Optional.empty(), Optional.of($$0.builtInRegistryHolder()), Optional.of($$1.build()), $$2)
            );
      }

      public boolean matches(BlockState $$0, ItemStack $$1, int $$2) {
         if (this.block.isPresent() && !$$0.is(this.block.get())) {
            return false;
         } else {
            return this.item.isPresent() && !this.item.get().test($$1) ? false : this.beesInside.matches($$2);
         }
      }
   }
}
