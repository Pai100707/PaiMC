package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FallAfterExplosionTrigger extends SimpleCriterionTrigger<FallAfterExplosionTrigger.TriggerInstance> {
   @Override
   public Codec<FallAfterExplosionTrigger.TriggerInstance> codec() {
      return FallAfterExplosionTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Vec3 $$1, @Nullable Entity $$2) {
      Vec3 $$3 = $$0.position();
      LootContext $$4 = $$2 != null ? EntityPredicate.createContext($$0, $$2) : null;
      this.trigger($$0, $$4x -> $$4x.matches($$0.level(), $$1, $$3, $$4));
   }

   public record TriggerInstance(
      Optional<ContextAwarePredicate> player,
      Optional<LocationPredicate> startPosition,
      Optional<DistancePredicate> distance,
      Optional<ContextAwarePredicate> cause
   ) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<FallAfterExplosionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(FallAfterExplosionTrigger.TriggerInstance::player),
               LocationPredicate.CODEC.optionalFieldOf("start_position").forGetter(FallAfterExplosionTrigger.TriggerInstance::startPosition),
               DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(FallAfterExplosionTrigger.TriggerInstance::distance),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("cause").forGetter(FallAfterExplosionTrigger.TriggerInstance::cause)
            )
            .apply($$0, FallAfterExplosionTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<FallAfterExplosionTrigger.TriggerInstance> fallAfterExplosion(
         DistancePredicate $$0, EntityPredicate.Builder $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.FALL_AFTER_EXPLOSION
            .createCriterion(
               new FallAfterExplosionTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of($$0), Optional.of(EntityPredicate.wrap($$1)))
            );
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.cause(), "cause");
      }

      public boolean matches(ServerLevel $$0, Vec3 $$1, Vec3 $$2, @Nullable LootContext $$3) {
         if (this.startPosition.isPresent() && !this.startPosition.get().matches($$0, $$1.x, $$1.y, $$1.z)) {
            return false;
         } else {
            return this.distance.isPresent() && !this.distance.get().matches($$1.x, $$1.y, $$1.z, $$2.x, $$2.y, $$2.z)
               ? false
               : !this.cause.isPresent() || $$3 != null && this.cause.get().matches($$3);
         }
      }
   }
}
