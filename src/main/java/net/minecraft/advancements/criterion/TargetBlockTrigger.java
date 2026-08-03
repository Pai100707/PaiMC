package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
   @Override
   public Codec<TargetBlockTrigger.TriggerInstance> codec() {
      return TargetBlockTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Entity $$1, Vec3 $$2, int $$3) {
      LootContext $$4 = EntityPredicate.createContext($$0, $$1);
      this.trigger($$0, $$3x -> $$3x.matches($$4, $$2, $$3));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints signalStrength, Optional<ContextAwarePredicate> projectile)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<TargetBlockTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TargetBlockTrigger.TriggerInstance::player),
               MinMaxBounds.Ints.CODEC.optionalFieldOf("signal_strength", MinMaxBounds.Ints.ANY).forGetter(TargetBlockTrigger.TriggerInstance::signalStrength),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("projectile").forGetter(TargetBlockTrigger.TriggerInstance::projectile)
            )
            .apply($$0, TargetBlockTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<TargetBlockTrigger.TriggerInstance> targetHit(
         MinMaxBounds.Ints $$0, Optional<ContextAwarePredicate> $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.TARGET_BLOCK_HIT
            .createCriterion(new TargetBlockTrigger.TriggerInstance(Optional.empty(), $$0, $$1));
      }

      public boolean matches(LootContext $$0, Vec3 $$1, int $$2) {
         return !this.signalStrength.matches($$2) ? false : !this.projectile.isPresent() || this.projectile.get().matches($$0);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.projectile, "projectile");
      }
   }
}
