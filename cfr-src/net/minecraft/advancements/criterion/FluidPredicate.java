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
import java.util.Optional;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public record FluidPredicate(Optional<HolderSet<Fluid>> fluids, Optional<StatePropertiesPredicate> properties) {
    public static final Codec<FluidPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)RegistryCodecs.homogeneousList(Registries.FLUID).optionalFieldOf("fluids").forGetter(FluidPredicate::fluids), (App)StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(FluidPredicate::properties)).apply((Applicative)$$0, FluidPredicate::new));

    public boolean matches(ServerLevel $$0, BlockPos $$1) {
        if (!$$0.isLoaded($$1)) {
            return false;
        }
        FluidState $$2 = $$0.getFluidState($$1);
        if (this.fluids.isPresent() && !$$2.is(this.fluids.get())) {
            return false;
        }
        return !this.properties.isPresent() || this.properties.get().matches($$2);
    }

    public static class Builder {
        private Optional<HolderSet<Fluid>> fluids = Optional.empty();
        private Optional<StatePropertiesPredicate> properties = Optional.empty();

        private Builder() {
        }

        public static Builder fluid() {
            return new Builder();
        }

        public Builder of(Fluid $$0) {
            this.fluids = Optional.of(HolderSet.direct($$0.builtInRegistryHolder()));
            return this;
        }

        public Builder of(HolderSet<Fluid> $$0) {
            this.fluids = Optional.of($$0);
            return this;
        }

        public Builder setProperties(StatePropertiesPredicate $$0) {
            this.properties = Optional.of($$0);
            return this;
        }

        public FluidPredicate build() {
            return new FluidPredicate(this.fluids, this.properties);
        }
    }
}

