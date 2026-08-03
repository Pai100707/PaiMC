package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record EntityFlagsPredicate(
   Optional<Boolean> isOnGround,
   Optional<Boolean> isOnFire,
   Optional<Boolean> isCrouching,
   Optional<Boolean> isSprinting,
   Optional<Boolean> isSwimming,
   Optional<Boolean> isFlying,
   Optional<Boolean> isBaby,
   Optional<Boolean> isInWater,
   Optional<Boolean> isFallFlying
) {
   public static final Codec<EntityFlagsPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.BOOL.optionalFieldOf("is_on_ground").forGetter(EntityFlagsPredicate::isOnGround),
            Codec.BOOL.optionalFieldOf("is_on_fire").forGetter(EntityFlagsPredicate::isOnFire),
            Codec.BOOL.optionalFieldOf("is_sneaking").forGetter(EntityFlagsPredicate::isCrouching),
            Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(EntityFlagsPredicate::isSprinting),
            Codec.BOOL.optionalFieldOf("is_swimming").forGetter(EntityFlagsPredicate::isSwimming),
            Codec.BOOL.optionalFieldOf("is_flying").forGetter(EntityFlagsPredicate::isFlying),
            Codec.BOOL.optionalFieldOf("is_baby").forGetter(EntityFlagsPredicate::isBaby),
            Codec.BOOL.optionalFieldOf("is_in_water").forGetter(EntityFlagsPredicate::isInWater),
            Codec.BOOL.optionalFieldOf("is_fall_flying").forGetter(EntityFlagsPredicate::isFallFlying)
         )
         .apply($$0, EntityFlagsPredicate::new)
   );

   public boolean matches(Entity $$0) {
      if (this.isOnGround.isPresent() && $$0.onGround() != this.isOnGround.get()) {
         return false;
      } else if (this.isOnFire.isPresent() && $$0.isOnFire() != this.isOnFire.get()) {
         return false;
      } else if (this.isCrouching.isPresent() && $$0.isCrouching() != this.isCrouching.get()) {
         return false;
      } else if (this.isSprinting.isPresent() && $$0.isSprinting() != this.isSprinting.get()) {
         return false;
      } else if (this.isSwimming.isPresent() && $$0.isSwimming() != this.isSwimming.get()) {
         return false;
      } else {
         if (this.isFlying.isPresent()) {
            boolean $$3 = $$0 instanceof LivingEntity $$1 && ($$1.isFallFlying() || $$1 instanceof Player $$2 && $$2.getAbilities().flying);
            if ($$3 != this.isFlying.get()) {
               return false;
            }
         }

         if (this.isInWater.isPresent() && $$0.isInWater() != this.isInWater.get()) {
            return false;
         } else {
            return this.isFallFlying.isPresent() && $$0 instanceof LivingEntity $$4 && $$4.isFallFlying() != this.isFallFlying.get()
               ? false
               : !(this.isBaby.isPresent() && $$0 instanceof LivingEntity $$5) || $$5.isBaby() == this.isBaby.get();
         }
      }
   }

   public static class Builder {
      private Optional<Boolean> isOnGround = Optional.empty();
      private Optional<Boolean> isOnFire = Optional.empty();
      private Optional<Boolean> isCrouching = Optional.empty();
      private Optional<Boolean> isSprinting = Optional.empty();
      private Optional<Boolean> isSwimming = Optional.empty();
      private Optional<Boolean> isFlying = Optional.empty();
      private Optional<Boolean> isBaby = Optional.empty();
      private Optional<Boolean> isInWater = Optional.empty();
      private Optional<Boolean> isFallFlying = Optional.empty();

      public static EntityFlagsPredicate.Builder flags() {
         return new EntityFlagsPredicate.Builder();
      }

      public EntityFlagsPredicate.Builder setOnGround(Boolean $$0) {
         this.isOnGround = Optional.of($$0);
         return this;
      }

      public EntityFlagsPredicate.Builder setOnFire(Boolean $$0) {
         this.isOnFire = Optional.of($$0);
         return this;
      }

      public EntityFlagsPredicate.Builder setCrouching(Boolean $$0) {
         this.isCrouching = Optional.of($$0);
         return this;
      }

      public EntityFlagsPredicate.Builder setSprinting(Boolean $$0) {
         this.isSprinting = Optional.of($$0);
         return this;
      }

      public EntityFlagsPredicate.Builder setSwimming(Boolean $$0) {
         this.isSwimming = Optional.of($$0);
         return this;
      }

      public EntityFlagsPredicate.Builder setIsFlying(Boolean $$0) {
         this.isFlying = Optional.of($$0);
         return this;
      }

      public EntityFlagsPredicate.Builder setIsBaby(Boolean $$0) {
         this.isBaby = Optional.of($$0);
         return this;
      }

      public EntityFlagsPredicate.Builder setIsInWater(Boolean $$0) {
         this.isInWater = Optional.of($$0);
         return this;
      }

      public EntityFlagsPredicate.Builder setIsFallFlying(Boolean $$0) {
         this.isFallFlying = Optional.of($$0);
         return this;
      }

      public EntityFlagsPredicate build() {
         return new EntityFlagsPredicate(
            this.isOnGround, this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isFlying, this.isBaby, this.isInWater, this.isFallFlying
         );
      }
   }
}
