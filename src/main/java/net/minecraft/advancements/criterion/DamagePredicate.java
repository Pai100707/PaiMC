package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public record DamagePredicate(
   MinMaxBounds.Doubles dealtDamage,
   MinMaxBounds.Doubles takenDamage,
   Optional<EntityPredicate> sourceEntity,
   Optional<Boolean> blocked,
   Optional<DamageSourcePredicate> type
) {
   public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("dealt", MinMaxBounds.Doubles.ANY).forGetter(DamagePredicate::dealtDamage),
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("taken", MinMaxBounds.Doubles.ANY).forGetter(DamagePredicate::takenDamage),
            EntityPredicate.CODEC.optionalFieldOf("source_entity").forGetter(DamagePredicate::sourceEntity),
            Codec.BOOL.optionalFieldOf("blocked").forGetter(DamagePredicate::blocked),
            DamageSourcePredicate.CODEC.optionalFieldOf("type").forGetter(DamagePredicate::type)
         )
         .apply($$0, DamagePredicate::new)
   );

   public boolean matches(ServerPlayer $$0, DamageSource $$1, float $$2, float $$3, boolean $$4) {
      if (!this.dealtDamage.matches($$2)) {
         return false;
      } else if (!this.takenDamage.matches($$3)) {
         return false;
      } else if (this.sourceEntity.isPresent() && !this.sourceEntity.get().matches($$0, $$1.getEntity())) {
         return false;
      } else {
         return this.blocked.isPresent() && this.blocked.get() != $$4 ? false : !this.type.isPresent() || this.type.get().matches($$0, $$1);
      }
   }

   public static class Builder {
      private MinMaxBounds.Doubles dealtDamage = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles takenDamage = MinMaxBounds.Doubles.ANY;
      private Optional<EntityPredicate> sourceEntity = Optional.empty();
      private Optional<Boolean> blocked = Optional.empty();
      private Optional<DamageSourcePredicate> type = Optional.empty();

      public static DamagePredicate.Builder damageInstance() {
         return new DamagePredicate.Builder();
      }

      public DamagePredicate.Builder dealtDamage(MinMaxBounds.Doubles $$0) {
         this.dealtDamage = $$0;
         return this;
      }

      public DamagePredicate.Builder takenDamage(MinMaxBounds.Doubles $$0) {
         this.takenDamage = $$0;
         return this;
      }

      public DamagePredicate.Builder sourceEntity(EntityPredicate $$0) {
         this.sourceEntity = Optional.of($$0);
         return this;
      }

      public DamagePredicate.Builder blocked(Boolean $$0) {
         this.blocked = Optional.of($$0);
         return this;
      }

      public DamagePredicate.Builder type(DamageSourcePredicate $$0) {
         this.type = Optional.of($$0);
         return this;
      }

      public DamagePredicate.Builder type(DamageSourcePredicate.Builder $$0) {
         this.type = Optional.of($$0.build());
         return this;
      }

      public DamagePredicate build() {
         return new DamagePredicate(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
      }
   }
}
