package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
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

public record LocationPredicate(
   Optional<LocationPredicate.PositionPredicate> position,
   Optional<HolderSet<Biome>> biomes,
   Optional<HolderSet<Structure>> structures,
   Optional<ResourceKey<Level>> dimension,
   Optional<Boolean> smokey,
   Optional<LightPredicate> light,
   Optional<BlockPredicate> block,
   Optional<FluidPredicate> fluid,
   Optional<Boolean> canSeeSky
) {
   public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            LocationPredicate.PositionPredicate.CODEC.optionalFieldOf("position").forGetter(LocationPredicate::position),
            RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(LocationPredicate::biomes),
            RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("structures").forGetter(LocationPredicate::structures),
            ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(LocationPredicate::dimension),
            Codec.BOOL.optionalFieldOf("smokey").forGetter(LocationPredicate::smokey),
            LightPredicate.CODEC.optionalFieldOf("light").forGetter(LocationPredicate::light),
            BlockPredicate.CODEC.optionalFieldOf("block").forGetter(LocationPredicate::block),
            FluidPredicate.CODEC.optionalFieldOf("fluid").forGetter(LocationPredicate::fluid),
            Codec.BOOL.optionalFieldOf("can_see_sky").forGetter(LocationPredicate::canSeeSky)
         )
         .apply($$0, LocationPredicate::new)
   );

   public boolean matches(ServerLevel $$0, double $$1, double $$2, double $$3) {
      if (this.position.isPresent() && !this.position.get().matches($$1, $$2, $$3)) {
         return false;
      } else if (this.dimension.isPresent() && this.dimension.get() != $$0.dimension()) {
         return false;
      } else {
         BlockPos $$4 = BlockPos.containing($$1, $$2, $$3);
         boolean $$5 = $$0.isLoaded($$4);
         if (!this.biomes.isPresent() || $$5 && this.biomes.get().contains($$0.getBiome($$4))) {
            if (!this.structures.isPresent() || $$5 && $$0.structureManager().getStructureWithPieceAt($$4, this.structures.get()).isValid()) {
               if (!this.smokey.isPresent() || $$5 && this.smokey.get() == CampfireBlock.isSmokeyPos($$0, $$4)) {
                  if (this.light.isPresent() && !this.light.get().matches($$0, $$4)) {
                     return false;
                  } else if (this.block.isPresent() && !this.block.get().matches($$0, $$4)) {
                     return false;
                  } else {
                     return this.fluid.isPresent() && !this.fluid.get().matches($$0, $$4)
                        ? false
                        : !this.canSeeSky.isPresent() || this.canSeeSky.get() == $$0.canSeeSky($$4);
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
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

      public static LocationPredicate.Builder location() {
         return new LocationPredicate.Builder();
      }

      public static LocationPredicate.Builder inBiome(Holder<Biome> $$0) {
         return location().setBiomes(HolderSet.direct(new Holder[]{$$0}));
      }

      public static LocationPredicate.Builder inDimension(ResourceKey<Level> $$0) {
         return location().setDimension($$0);
      }

      public static LocationPredicate.Builder inStructure(Holder<Structure> $$0) {
         return location().setStructures(HolderSet.direct(new Holder[]{$$0}));
      }

      public static LocationPredicate.Builder atYLocation(MinMaxBounds.Doubles $$0) {
         return location().setY($$0);
      }

      public LocationPredicate.Builder setX(MinMaxBounds.Doubles $$0) {
         this.x = $$0;
         return this;
      }

      public LocationPredicate.Builder setY(MinMaxBounds.Doubles $$0) {
         this.y = $$0;
         return this;
      }

      public LocationPredicate.Builder setZ(MinMaxBounds.Doubles $$0) {
         this.z = $$0;
         return this;
      }

      public LocationPredicate.Builder setBiomes(HolderSet<Biome> $$0) {
         this.biomes = Optional.of($$0);
         return this;
      }

      public LocationPredicate.Builder setStructures(HolderSet<Structure> $$0) {
         this.structures = Optional.of($$0);
         return this;
      }

      public LocationPredicate.Builder setDimension(ResourceKey<Level> $$0) {
         this.dimension = Optional.of($$0);
         return this;
      }

      public LocationPredicate.Builder setLight(LightPredicate.Builder $$0) {
         this.light = Optional.of($$0.build());
         return this;
      }

      public LocationPredicate.Builder setBlock(BlockPredicate.Builder $$0) {
         this.block = Optional.of($$0.build());
         return this;
      }

      public LocationPredicate.Builder setFluid(FluidPredicate.Builder $$0) {
         this.fluid = Optional.of($$0.build());
         return this;
      }

      public LocationPredicate.Builder setSmokey(boolean $$0) {
         this.smokey = Optional.of($$0);
         return this;
      }

      public LocationPredicate.Builder setCanSeeSky(boolean $$0) {
         this.canSeeSky = Optional.of($$0);
         return this;
      }

      public LocationPredicate build() {
         Optional<LocationPredicate.PositionPredicate> $$0 = LocationPredicate.PositionPredicate.of(this.x, this.y, this.z);
         return new LocationPredicate($$0, this.biomes, this.structures, this.dimension, this.smokey, this.light, this.block, this.fluid, this.canSeeSky);
      }
   }

   record PositionPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z) {
      public static final Codec<LocationPredicate.PositionPredicate> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", MinMaxBounds.Doubles.ANY).forGetter(LocationPredicate.PositionPredicate::x),
               MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", MinMaxBounds.Doubles.ANY).forGetter(LocationPredicate.PositionPredicate::y),
               MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", MinMaxBounds.Doubles.ANY).forGetter(LocationPredicate.PositionPredicate::z)
            )
            .apply($$0, LocationPredicate.PositionPredicate::new)
      );

      static Optional<LocationPredicate.PositionPredicate> of(MinMaxBounds.Doubles $$0, MinMaxBounds.Doubles $$1, MinMaxBounds.Doubles $$2) {
         return $$0.isAny() && $$1.isAny() && $$2.isAny() ? Optional.empty() : Optional.of(new LocationPredicate.PositionPredicate($$0, $$1, $$2));
      }

      public boolean matches(double $$0, double $$1, double $$2) {
         return this.x.matches($$0) && this.y.matches($$1) && this.z.matches($$2);
      }
   }
}
