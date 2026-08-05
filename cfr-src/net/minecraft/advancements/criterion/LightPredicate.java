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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record LightPredicate(MinMaxBounds.Ints composite) {
    public static final Codec<LightPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("light", (Object)MinMaxBounds.Ints.ANY).forGetter(LightPredicate::composite)).apply((Applicative)$$0, LightPredicate::new));

    public boolean matches(ServerLevel $$0, BlockPos $$1) {
        if (!$$0.isLoaded($$1)) {
            return false;
        }
        return this.composite.matches($$0.getMaxLocalRawBrightness($$1));
    }

    public static class Builder {
        private MinMaxBounds.Ints composite = MinMaxBounds.Ints.ANY;

        public static Builder light() {
            return new Builder();
        }

        public Builder setComposite(MinMaxBounds.Ints $$0) {
            this.composite = $$0;
            return this;
        }

        public LightPredicate build() {
            return new LightPredicate(this.composite);
        }
    }
}

