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
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.FluidPredicate;
import net.minecraft.advancements.criterion.LightPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;

public record LocationPredicate(Optional<PositionPredicate> position, Optional<HolderSet<Biome>> biomes, Optional<HolderSet<Structure>> structures, Optional<ResourceKey<Level>> dimension, Optional<Boolean> smokey, Optional<LightPredicate> light, Optional<BlockPredicate> block, Optional<FluidPredicate> fluid, Optional<Boolean> canSeeSky) {
    public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)PositionPredicate.CODEC.optionalFieldOf("position").forGetter(LocationPredicate::position), (App)RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(LocationPredicate::biomes), (App)RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("structures").forGetter(LocationPredicate::structures), (App)ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(LocationPredicate::dimension), (App)Codec.BOOL.optionalFieldOf("smokey").forGetter(LocationPredicate::smokey), (App)LightPredicate.CODEC.optionalFieldOf("light").forGetter(LocationPredicate::light), (App)BlockPredicate.CODEC.optionalFieldOf("block").forGetter(LocationPredicate::block), (App)FluidPredicate.CODEC.optionalFieldOf("fluid").forGetter(LocationPredicate::fluid), (App)Codec.BOOL.optionalFieldOf("can_see_sky").forGetter(LocationPredicate::canSeeSky)).apply((Applicative)$$0, LocationPredicate::new));

    public boolean matches(ServerLevel $$0, double $$1, double $$2, double $$3) {
        if (this.position.isPresent() && !this.position.get().matches($$1, $$2, $$3)) {
            return false;
        }
        if (this.dimension.isPresent() && this.dimension.get() != $$0.dimension()) {
            return false;
        }
        BlockPos $$4 = BlockPos.containing($$1, $$2, $$3);
        boolean $$5 = $$0.isLoaded($$4);
        if (!(!this.biomes.isPresent() || $$5 && this.biomes.get().contains($$0.getBiome($$4)))) {
            return false;
        }
        if (!(!this.structures.isPresent() || $$5 && $$0.structureManager().getStructureWithPieceAt($$4, this.structures.get()).isValid())) {
            return false;
        }
        if (this.smokey.isPresent() && (!$$5 || this.smokey.get() != CampfireBlock.isSmokeyPos($$0, $$4))) {
            return false;
        }
        if (this.light.isPresent() && !this.light.get().matches($$0, $$4)) {
            return false;
        }
        if (this.block.isPresent() && !this.block.get().matches($$0, $$4)) {
            return false;
        }
        if (this.fluid.isPresent() && !this.fluid.get().matches($$0, $$4)) {
            return false;
        }
        return !this.canSeeSky.isPresent() || this.canSeeSky.get().booleanValue() == $$0.canSeeSky($$4);
    }

    record PositionPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z) {
        public static final Codec<PositionPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", (Object)MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::x), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", (Object)MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::y), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", (Object)MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::z)).apply((Applicative)$$0, PositionPredicate::new));

        static Optional<PositionPredicate> of(MinMaxBounds.Doubles $$0, MinMaxBounds.Doubles $$1, MinMaxBounds.Doubles $$2) {
            if ($$0.isAny() && $$1.isAny() && $$2.isAny()) {
                return Optional.empty();
            }
            return Optional.of(new PositionPredicate($$0, $$1, $$2));
        }

        public boolean matches(double $$0, double $$1, double $$2) {
            return this.x.matches($$0) && this.y.matches($$1) && this.z.matches($$2);
        }
    }

    public static class Builder {
        private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
        private Optional<HolderSet<Biome>> biomes = Optional.empty();
        private Optional<HolderSet<Structure>> structures = Optional.empty();
        private Optional<ResourceKey<Level>> dimension = Optional.empty();
        private Optional<Boolean> smokey = Optional.empty();
        private Optional<LightPredicate> light = Optional.empty();
        private Optional<BlockPredicate> block = Optional.empty();
        private Optional<FluidPredicate> fluid = Optional.empty();
        private Optional<Boolean> canSeeSky = Optional.empty();

        public static Builder location() {
            return new Builder();
        }

        public static Builder inBiome(Holder<Biome> $$0) {
            return Builder.location().setBiomes(HolderSet.direct($$0));
        }

        public static Builder inDimension(ResourceKey<Level> $$0) {
            return Builder.location().setDimension($$0);
        }

        public static Builder inStructure(Holder<Structure> $$0) {
            return Builder.location().setStructures(HolderSet.direct($$0));
        }

        public static Builder atYLocation(MinMaxBounds.Doubles $$0) {
            return Builder.location().setY($$0);
        }

        public Builder setX(MinMaxBounds.Doubles $$0) {
            this.x = $$0;
            return this;
        }

        public Builder setY(MinMaxBounds.Doubles $$0) {
            this.y = $$0;
            return this;
        }

        public Builder setZ(MinMaxBounds.Doubles $$0) {
            this.z = $$0;
            return this;
        }

        public Builder setBiomes(HolderSet<Biome> $$0) {
            this.biomes = Optional.of($$0);
            return this;
        }

        public Builder setStructures(HolderSet<Structure> $$0) {
            this.structures = Optional.of($$0);
            return this;
        }

        public Builder setDimension(ResourceKey<Level> $$0) {
            this.dimension = Optional.of($$0);
            return this;
        }

        public Builder setLight(LightPredicate.Builder $$0) {
            this.light = Optional.of($$0.build());
            return this;
        }

        public Builder setBlock(BlockPredicate.Builder $$0) {
            this.block = Optional.of($$0.build());
            return this;
        }

        public Builder setFluid(FluidPredicate.Builder $$0) {
            this.fluid = Optional.of($$0.build());
            return this;
        }

        public Builder setSmokey(boolean $$0) {
            this.smokey = Optional.of($$0);
            return this;
        }

        public Builder setCanSeeSky(boolean $$0) {
            this.canSeeSky = Optional.of($$0);
            return this;
        }

        public LocationPredicate build() {
            Optional<PositionPredicate> $$0 = PositionPredicate.of(this.x, this.y, this.z);
            return new LocationPredicate($$0, this.biomes, this.structures, this.dimension, this.smokey, this.light, this.block, this.fluid, this.canSeeSky);
        }
    }
}

