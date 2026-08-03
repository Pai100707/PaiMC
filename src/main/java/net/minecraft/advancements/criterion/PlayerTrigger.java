package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
   @Override
   public Codec<PlayerTrigger.TriggerInstance> codec() {
      return PlayerTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0) {
      this.trigger($$0, $$0x -> true);
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<PlayerTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PlayerTrigger.TriggerInstance::player))
            .apply($$0, PlayerTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<PlayerTrigger.TriggerInstance> located(LocationPredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.LOCATION
            .createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().located($$0)))));
      }

      public static net.minecraft.advancements.Criterion<PlayerTrigger.TriggerInstance> located(EntityPredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.LOCATION
            .createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap($$0.build()))));
      }

      public static net.minecraft.advancements.Criterion<PlayerTrigger.TriggerInstance> located(Optional<EntityPredicate> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.LOCATION.createCriterion(new PlayerTrigger.TriggerInstance(EntityPredicate.wrap($$0)));
      }

      public static net.minecraft.advancements.Criterion<PlayerTrigger.TriggerInstance> sleptInBed() {
         return net.minecraft.advancements.CriteriaTriggers.SLEPT_IN_BED.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<PlayerTrigger.TriggerInstance> raidWon() {
         return net.minecraft.advancements.CriteriaTriggers.RAID_WIN.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<PlayerTrigger.TriggerInstance> avoidVibration() {
         return net.minecraft.advancements.CriteriaTriggers.AVOID_VIBRATION.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<PlayerTrigger.TriggerInstance> tick() {
         return net.minecraft.advancements.CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<PlayerTrigger.TriggerInstance> walkOnBlockWithEquipment(
         HolderGetter<Block> $$0, HolderGetter<Item> $$1, Block $$2, Item $$3
      ) {
         return located(
            EntityPredicate.Builder.entity()
               .equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of($$1, $$3)))
               .steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of($$0, $$2)))
         );
      }
   }
}
