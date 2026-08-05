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

public record DistancePredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z, MinMaxBounds.Doubles horizontal, MinMaxBounds.Doubles absolute) {
    public static final Codec<DistancePredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::x), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::y), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::z), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::horizontal), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("absolute", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::absolute)).apply((Applicative)$$0, DistancePredicate::new));

    public static DistancePredicate horizontal(MinMaxBounds.Doubles $$0) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, $$0, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate vertical(MinMaxBounds.Doubles $$0) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, $$0, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate absolute(MinMaxBounds.Doubles $$0) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, $$0);
    }

    public boolean matches(double $$0, double $$1, double $$2, double $$3, double $$4, double $$5) {
        float $$6 = (float)($$0 - $$3);
        float $$7 = (float)($$1 - $$4);
        float $$8 = (float)($$2 - $$5);
        if (!(this.x.matches(Mth.abs($$6)) && this.y.matches(Mth.abs($$7)) && this.z.matches(Mth.abs($$8)))) {
            return false;
        }
        if (!this.horizontal.matchesSqr($$6 * $$6 + $$8 * $$8)) {
            return false;
        }
        return this.absolute.matchesSqr($$6 * $$6 + $$7 * $$7 + $$8 * $$8);
    }
}

