package net.minecraft.world.level;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public interface LevelReader
   extends net.minecraft.world.level.BlockAndTintGetter,
   net.minecraft.world.level.CollisionGetter,
   net.minecraft.world.level.SignalGetter,
   BiomeManager.NoiseBiomeSource {
   
   ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

   @Deprecated
   boolean hasChunk(int var1, int var2);

   int getHeight(Heightmap.Types var1, int var2, int var3);

   default int getHeight(Heightmap.Types $$0, BlockPos $$1) {
      return this.getHeight($$0, $$1.getX(), $$1.getZ());
   }

   int getSkyDarken();

   BiomeManager getBiomeManager();

   default Holder<Biome> getBiome(BlockPos $$0) {
      return this.getBiomeManager().getBiome($$0);
   }

   default Stream<BlockState> getBlockStatesIfLoaded(AABB $$0) {
      int $$1 = Mth.floor($$0.minX);
      int $$2 = Mth.floor($$0.maxX);
      int $$3 = Mth.floor($$0.minY);
      int $$4 = Mth.floor($$0.maxY);
      int $$5 = Mth.floor($$0.minZ);
      int $$6 = Mth.floor($$0.maxZ);
      return this.hasChunksAt($$1, $$3, $$5, $$2, $$4, $$6) ? this.getBlockStates($$0) : Stream.empty();
   }

   @Override
   default int getBlockTint(BlockPos $$0, net.minecraft.world.level.ColorResolver $$1) {
      return $$1.getColor((Biome)this.getBiome($$0).value(), $$0.getX(), $$0.getZ());
   }

   @Override
   default Holder<Biome> getNoiseBiome(int $$0, int $$1, int $$2) {
      ChunkAccess $$3 = this.getChunk(QuartPos.toSection($$0), QuartPos.toSection($$2), ChunkStatus.BIOMES, false);
      return $$3 != null ? $$3.getNoiseBiome($$0, $$1, $$2) : this.getUncachedNoiseBiome($$0, $$1, $$2);
   }

   Holder<Biome> getUncachedNoiseBiome(int var1, int var2, int var3);

   boolean isClientSide();

   int getSeaLevel();

   DimensionType dimensionType();

   @Override
   default int getMinY() {
      return this.dimensionType().minY();
   }

   @Override
   default int getHeight() {
      return this.dimensionType().height();
   }

   default BlockPos getHeightmapPos(Heightmap.Types $$0, BlockPos $$1) {
      return new BlockPos($$1.getX(), this.getHeight($$0, $$1.getX(), $$1.getZ()), $$1.getZ());
   }

   default boolean isEmptyBlock(BlockPos $$0) {
      return this.getBlockState($$0).isAir();
   }

   default boolean canSeeSkyFromBelowWater(BlockPos $$0) {
      if ($$0.getY() >= this.getSeaLevel()) {
         return this.canSeeSky($$0);
      } else {
         BlockPos $$1 = new BlockPos($$0.getX(), this.getSeaLevel(), $$0.getZ());
         if (!this.canSeeSky($$1)) {
            return false;
         } else {
            for (BlockPos var4 = $$1.below(); var4.getY() > $$0.getY(); var4 = var4.below()) {
               BlockState $$2 = this.getBlockState(var4);
               if ($$2.getLightBlock() > 0 && !$$2.liquid()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   default float getPathfindingCostFromLightLevels(BlockPos $$0) {
      return this.getLightLevelDependentMagicValue($$0) - 0.5F;
   }

   @Deprecated
   default float getLightLevelDependentMagicValue(BlockPos $$0) {
      float $$1 = this.getMaxLocalRawBrightness($$0) / 15.0F;
      float $$2 = $$1 / (4.0F - 3.0F * $$1);
      return Mth.lerp(this.dimensionType().ambientLight(), $$2, 1.0F);
   }

   default ChunkAccess getChunk(BlockPos $$0) {
      return this.getChunk(SectionPos.blockToSectionCoord($$0.getX()), SectionPos.blockToSectionCoord($$0.getZ()));
   }

   default ChunkAccess getChunk(int $$0, int $$1) {
      return this.getChunk($$0, $$1, ChunkStatus.FULL, true);
   }

   default ChunkAccess getChunk(int $$0, int $$1, ChunkStatus $$2) {
      return this.getChunk($$0, $$1, $$2, true);
   }

   
   @Override
   default net.minecraft.world.level.BlockGetter getChunkForCollisions(int $$0, int $$1) {
      return this.getChunk($$0, $$1, ChunkStatus.EMPTY, false);
   }

   default boolean isWaterAt(BlockPos $$0) {
      return this.getFluidState($$0).is(FluidTags.WATER);
   }

   default boolean containsAnyLiquid(AABB $$0) {
      int $$1 = Mth.floor($$0.minX);
      int $$2 = Mth.ceil($$0.maxX);
      int $$3 = Mth.floor($$0.minY);
      int $$4 = Mth.ceil($$0.maxY);
      int $$5 = Mth.floor($$0.minZ);
      int $$6 = Mth.ceil($$0.maxZ);
      MutableBlockPos $$7 = new MutableBlockPos();

      for (int $$8 = $$1; $$8 < $$2; $$8++) {
         for (int $$9 = $$3; $$9 < $$4; $$9++) {
            for (int $$10 = $$5; $$10 < $$6; $$10++) {
               BlockState $$11 = this.getBlockState($$7.set($$8, $$9, $$10));
               if (!$$11.getFluidState().isEmpty()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   default int getMaxLocalRawBrightness(BlockPos $$0) {
      return this.getMaxLocalRawBrightness($$0, this.getSkyDarken());
   }

   default int getMaxLocalRawBrightness(BlockPos $$0, int $$1) {
      return $$0.getX() >= -30000000 && $$0.getZ() >= -30000000 && $$0.getX() < 30000000 && $$0.getZ() < 30000000 ? this.getRawBrightness($$0, $$1) : 15;
   }

   @Deprecated
   default boolean hasChunkAt(int $$0, int $$1) {
      return this.hasChunk(SectionPos.blockToSectionCoord($$0), SectionPos.blockToSectionCoord($$1));
   }

   @Deprecated
   default boolean hasChunkAt(BlockPos $$0) {
      return this.hasChunkAt($$0.getX(), $$0.getZ());
   }

   @Deprecated
   default boolean hasChunksAt(BlockPos $$0, BlockPos $$1) {
      return this.hasChunksAt($$0.getX(), $$0.getY(), $$0.getZ(), $$1.getX(), $$1.getY(), $$1.getZ());
   }

   @Deprecated
   default boolean hasChunksAt(int $$0, int $$1, int $$2, int $$3, int $$4, int $$5) {
      return $$4 >= this.getMinY() && $$1 <= this.getMaxY() ? this.hasChunksAt($$0, $$2, $$3, $$5) : false;
   }

   @Deprecated
   default boolean hasChunksAt(int $$0, int $$1, int $$2, int $$3) {
      int $$4 = SectionPos.blockToSectionCoord($$0);
      int $$5 = SectionPos.blockToSectionCoord($$2);
      int $$6 = SectionPos.blockToSectionCoord($$1);
      int $$7 = SectionPos.blockToSectionCoord($$3);

      for (int $$8 = $$4; $$8 <= $$5; $$8++) {
         for (int $$9 = $$6; $$9 <= $$7; $$9++) {
            if (!this.hasChunk($$8, $$9)) {
               return false;
            }
         }
      }

      return true;
   }

   RegistryAccess registryAccess();

   FeatureFlagSet enabledFeatures();

   default <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<? extends T>> $$0) {
      Registry<T> $$1 = this.registryAccess().lookupOrThrow($$0);
      return $$1.filterFeatures(this.enabledFeatures());
   }

   EnvironmentAttributeReader environmentAttributes();
}
