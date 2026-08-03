package net.minecraft.world.level;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PathNavigationRegion implements net.minecraft.world.level.CollisionGetter {
   protected final int centerX;
   protected final int centerZ;
   protected final ChunkAccess[][] chunks;
   protected boolean allEmpty;
   protected final net.minecraft.world.level.Level level;
   private final Supplier<Holder<Biome>> plains;

   public PathNavigationRegion(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockPos $$2) {
      this.level = $$0;
      this.plains = Suppliers.memoize(() -> $$0.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS));
      this.centerX = SectionPos.blockToSectionCoord($$1.getX());
      this.centerZ = SectionPos.blockToSectionCoord($$1.getZ());
      int $$3 = SectionPos.blockToSectionCoord($$2.getX());
      int $$4 = SectionPos.blockToSectionCoord($$2.getZ());
      this.chunks = new ChunkAccess[$$3 - this.centerX + 1][$$4 - this.centerZ + 1];
      ChunkSource $$5 = $$0.getChunkSource();
      this.allEmpty = true;

      for (int $$6 = this.centerX; $$6 <= $$3; $$6++) {
         for (int $$7 = this.centerZ; $$7 <= $$4; $$7++) {
            this.chunks[$$6 - this.centerX][$$7 - this.centerZ] = $$5.getChunkNow($$6, $$7);
         }
      }

      for (int $$8 = SectionPos.blockToSectionCoord($$1.getX()); $$8 <= SectionPos.blockToSectionCoord($$2.getX()); $$8++) {
         for (int $$9 = SectionPos.blockToSectionCoord($$1.getZ()); $$9 <= SectionPos.blockToSectionCoord($$2.getZ()); $$9++) {
            ChunkAccess $$10 = this.chunks[$$8 - this.centerX][$$9 - this.centerZ];
            if ($$10 != null && !$$10.isYSpaceEmpty($$1.getY(), $$2.getY())) {
               this.allEmpty = false;
               return;
            }
         }
      }
   }

   private ChunkAccess getChunk(BlockPos $$0) {
      return this.getChunk(SectionPos.blockToSectionCoord($$0.getX()), SectionPos.blockToSectionCoord($$0.getZ()));
   }

   private ChunkAccess getChunk(int $$0, int $$1) {
      int $$2 = $$0 - this.centerX;
      int $$3 = $$1 - this.centerZ;
      if ($$2 >= 0 && $$2 < this.chunks.length && $$3 >= 0 && $$3 < this.chunks[$$2].length) {
         ChunkAccess $$4 = this.chunks[$$2][$$3];
         return (ChunkAccess)($$4 != null ? $$4 : new EmptyLevelChunk(this.level, new net.minecraft.world.level.ChunkPos($$0, $$1), this.plains.get()));
      } else {
         return new EmptyLevelChunk(this.level, new net.minecraft.world.level.ChunkPos($$0, $$1), this.plains.get());
      }
   }

   @Override
   public WorldBorder getWorldBorder() {
      return this.level.getWorldBorder();
   }

   @Override
   public net.minecraft.world.level.BlockGetter getChunkForCollisions(int $$0, int $$1) {
      return this.getChunk($$0, $$1);
   }

   @Override
   public List<VoxelShape> getEntityCollisions(@Nullable Entity $$0, AABB $$1) {
      return List.of();
   }

   @Nullable
   @Override
   public BlockEntity getBlockEntity(BlockPos $$0) {
      ChunkAccess $$1 = this.getChunk($$0);
      return $$1.getBlockEntity($$0);
   }

   @Override
   public BlockState getBlockState(BlockPos $$0) {
      if (this.isOutsideBuildHeight($$0)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         ChunkAccess $$1 = this.getChunk($$0);
         return $$1.getBlockState($$0);
      }
   }

   @Override
   public FluidState getFluidState(BlockPos $$0) {
      if (this.isOutsideBuildHeight($$0)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         ChunkAccess $$1 = this.getChunk($$0);
         return $$1.getFluidState($$0);
      }
   }

   @Override
   public int getMinY() {
      return this.level.getMinY();
   }

   @Override
   public int getHeight() {
      return this.level.getHeight();
   }
}
