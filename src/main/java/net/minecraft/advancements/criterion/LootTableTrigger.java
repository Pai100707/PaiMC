package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance> {
   @Override
   public Codec<LootTableTrigger.TriggerInstance> codec() {
      return LootTableTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ResourceKey<LootTable> $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<LootTable> lootTable) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<LootTableTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(LootTableTrigger.TriggerInstance::player),
               LootTable.KEY_CODEC.fieldOf("loot_table").forGetter(LootTableTrigger.TriggerInstance::lootTable)
            )
            .apply($$0, LootTableTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<LootTableTrigger.TriggerInstance> lootTableUsed(ResourceKey<LootTable> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.GENERATE_LOOT.createCriterion(new LootTableTrigger.TriggerInstance(Optional.empty(), $$0));
      }

      public boolean matches(ResourceKey<LootTable> $$0) {
         return this.lootTable == $$0;
      }
   }
}
