/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.util.Mth;

public record MovementPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z, MinMaxBounds.Doubles speed, MinMaxBounds.Doubles horizontalSpeed, MinMaxBounds.Doubles verticalSpeed, MinMaxBounds.Doubles fallDistance) {
    public static final Codec<MovementPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::x), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::y), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::z), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("speed", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::speed), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal_speed", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::horizontalSpeed), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("vertical_speed", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::verticalSpeed), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("fall_distance", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::fallDistance)).apply((Applicative)$$0, MovementPredicate::new));

    public static MovementPredicate speed(MinMaxBounds.Doubles $$0) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, $$0, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static MovementPredicate horizontalSpeed(MinMaxBounds.Doubles $$0) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, $$0, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static MovementPredicate verticalSpeed(MinMaxBounds.Doubles $$0) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, $$0, MinMaxBounds.Doubles.ANY);
    }

    public static MovementPredicate fallDistance(MinMaxBounds.Doubles $$0) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, $$0);
    }

    public boolean matches(double $$0, double $$1, double $$2, double $$3) {
        if (!(this.x.matches($$0) && this.y.matches($$1) && this.z.matches($$2))) {
            return false;
        }
        double $$4 = Mth.lengthSquared($$0, $$1, $$2);
        if (!this.speed.matchesSqr($$4)) {
            return false;
        }
        double $$5 = Mth.lengthSquared($$0, $$2);
        if (!this.horizontalSpeed.matchesSqr($$5)) {
            return false;
        }
        double $$6 = Math.abs($$1);
        if (!this.verticalSpeed.matches($$6)) {
            return false;
        }
        return this.fallDistance.matches($$3);
    }
}

