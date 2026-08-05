/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record SlimePredicate(MinMaxBounds.Ints size) implements EntitySubPredicate
{
    public static final MapCodec<SlimePredicate> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("size", (Object)MinMaxBounds.Ints.ANY).forGetter(SlimePredicate::size)).apply((Applicative)$$0, SlimePredicate::new));

    public static SlimePredicate sized(MinMaxBounds.Ints $$0) {
        return new SlimePredicate($$0);
    }

    @Override
    public boolean matches(Entity $$0, ServerLevel $$1, @Nullable Vec3 $$2) {
        if ($$0 instanceof Slime) {
            Slime $$3 = (Slime)$$0;
            return this.size.matches($$3.getSize());
        }
        return false;
    }

    public MapCodec<SlimePredicate> codec() {
        return EntitySubPredicates.SLIME;
    }
}

