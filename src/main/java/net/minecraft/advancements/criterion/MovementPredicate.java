package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

public record MovementPredicate(
   MinMaxBounds.Doubles x,
   MinMaxBounds.Doubles y,
   MinMaxBounds.Doubles z,
   MinMaxBounds.Doubles speed,
   MinMaxBounds.Doubles horizontalSpeed,
   MinMaxBounds.Doubles verticalSpeed,
   MinMaxBounds.Doubles fallDistance
) {
   public static final Codec<MovementPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::x),
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::y),
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::z),
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::speed),
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal_speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::horizontalSpeed),
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("vertical_speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::verticalSpeed),
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("fall_distance", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::fallDistance)
         )
         .apply($$0, MovementPredicate::new)
   );

   public static MovementPredicate speed(MinMaxBounds.Doubles $$0) {
      return new MovementPredicate(
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         $$0,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY
      );
   }

   public static MovementPredicate horizontalSpeed(MinMaxBounds.Doubles $$0) {
      return new MovementPredicate(
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         $$0,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY
      );
   }

   public static MovementPredicate verticalSpeed(MinMaxBounds.Doubles $$0) {
      return new MovementPredicate(
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         $$0,
         MinMaxBounds.Doubles.ANY
      );
   }

   public static MovementPredicate fallDistance(MinMaxBounds.Doubles $$0) {
      return new MovementPredicate(
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         MinMaxBounds.Doubles.ANY,
         $$0
      );
   }

   public boolean matches(double $$0, double $$1, double $$2, double $$3) {
      if (this.x.matches($$0) && this.y.matches($$1) && this.z.matches($$2)) {
         double $$4 = Mth.lengthSquared($$0, $$1, $$2);
         if (!this.speed.matchesSqr($$4)) {
            return false;
         } else {
            double $$5 = Mth.lengthSquared($$0, $$2);
            if (!this.horizontalSpeed.matchesSqr($$5)) {
               return false;
            } else {
               double $$6 = Math.abs($$1);
               return !this.verticalSpeed.matches($$6) ? false : this.fallDistance.matches($$3);
            }
         }
      } else {
         return false;
      }
   }
}
