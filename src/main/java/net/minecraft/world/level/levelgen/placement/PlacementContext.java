package net.minecraft.world.level.levelgen.placement;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class PlacementContext extends WorldGenerationContext {
   private final net.minecraft.world.level.WorldGenLevel level;
   private final ChunkGenerator generator;
   private final Optional<PlacedFeature> topFeature;

   public PlacementContext(net.minecraft.world.level.WorldGenLevel $$0, ChunkGenerator $$1, Optional<PlacedFeature> $$2) {
      super($$1, $$0);
      this.level = $$0;
      this.generator = $$1;
      this.topFeature = $$2;
   }

   public int getHeight(Heightmap.Types $$0, int $$1, int $$2) {
      return this.level.getHeight($$0, $$1, $$2);
   }

   public CarvingMask getCarvingMask(net.minecraft.world.level.ChunkPos $$0) {
      return ((ProtoChunk)this.level.getChunk($$0.x, $$0.z)).getOrCreateCarvingMask();
   }

   public BlockState getBlockState(BlockPos $$0) {
      return this.level.getBlockState($$0);
   }

   public int getMinY() {
      return this.level.getMinY();
   }

   public net.minecraft.world.level.WorldGenLevel getLevel() {
      return this.level;
   }

   public Optional<PlacedFeature> topFeature() {
      return this.topFeature;
   }

   public ChunkGenerator generator() {
      return this.generator;
   }
}
