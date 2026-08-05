package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public class ProtoChunk extends ChunkAccess {
   private static final Logger LOGGER = LogUtils.getLogger();
   
   private volatile LevelLightEngine lightEngine;
   private volatile ChunkStatus status = ChunkStatus.EMPTY;
   private final List<CompoundTag> entities = Lists.newArrayList();
   
   private CarvingMask carvingMask;
   
   private BelowZeroRetrogen belowZeroRetrogen;
   private final ProtoChunkTicks<Block> blockTicks;
   private final ProtoChunkTicks<Fluid> fluidTicks;

   public ProtoChunk(
      net.minecraft.world.level.ChunkPos $$0,
      UpgradeData $$1,
      net.minecraft.world.level.LevelHeightAccessor $$2,
      PalettedContainerFactory $$3,
      BlendingData $$4
   ) {
      this($$0, $$1, null, new ProtoChunkTicks(), new ProtoChunkTicks(), $$2, $$3, $$4);
   }

   public ProtoChunk(
      net.minecraft.world.level.ChunkPos $$0,
      UpgradeData $$1,
      LevelChunkSection[] $$2,
      ProtoChunkTicks<Block> $$3,
      ProtoChunkTicks<Fluid> $$4,
      net.minecraft.world.level.LevelHeightAccessor $$5,
      PalettedContainerFactory $$6,
      BlendingData $$7
   ) {
      super($$0, $$1, $$5, $$6, 0L, $$2, $$7);
      this.blockTicks = $$3;
      this.fluidTicks = $$4;
   }

   @Override
   public TickContainerAccess<Block> getBlockTicks() {
      return this.blockTicks;
   }

   @Override
   public TickContainerAccess<Fluid> getFluidTicks() {
      return this.fluidTicks;
   }

   @Override
   public ChunkAccess.PackedTicks getTicksForSerialization(long $$0) {
      return new ChunkAccess.PackedTicks(this.blockTicks.pack($$0), this.fluidTicks.pack($$0));
   }

   @Override
   public BlockState getBlockState(BlockPos $$0) {
      int $$1 = $$0.getY();
      if (this.isOutsideBuildHeight($$1)) {
         return Blocks.VOID_AIR.defaultBlockState();
      } else {
         LevelChunkSection $$2 = this.getSection(this.getSectionIndex($$1));
         return $$2.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : $$2.getBlockState($$0.getX() & 15, $$1 & 15, $$0.getZ() & 15);
      }
   }

   @Override
   public FluidState getFluidState(BlockPos $$0) {
      int $$1 = $$0.getY();
      if (this.isOutsideBuildHeight($$1)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         LevelChunkSection $$2 = this.getSection(this.getSectionIndex($$1));
         return $$2.hasOnlyAir() ? Fluids.EMPTY.defaultFluidState() : $$2.getFluidState($$0.getX() & 15, $$1 & 15, $$0.getZ() & 15);
      }
   }

   
   @Override
   public BlockState setBlockState(BlockPos $$0, BlockState $$1, @Block.UpdateFlags int $$2) {
      int $$3 = $$0.getX();
      int $$4 = $$0.getY();
      int $$5 = $$0.getZ();
      if (this.isOutsideBuildHeight($$4)) {
         return Blocks.VOID_AIR.defaultBlockState();
      } else {
         int $$6 = this.getSectionIndex($$4);
         LevelChunkSection $$7 = this.getSection($$6);
         boolean $$8 = $$7.hasOnlyAir();
         if ($$8 && $$1.is(Blocks.AIR)) {
            return $$1;
         } else {
            int $$9 = SectionPos.sectionRelative($$3);
            int $$10 = SectionPos.sectionRelative($$4);
            int $$11 = SectionPos.sectionRelative($$5);
            BlockState $$12 = $$7.setBlockState($$9, $$10, $$11, $$1);
            if (this.status.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
               boolean $$13 = $$7.hasOnlyAir();
               if ($$13 != $$8) {
                  this.lightEngine.updateSectionStatus($$0, $$13);
               }

               if (LightEngine.hasDifferentLightProperties($$12, $$1)) {
                  this.skyLightSources.update(this, $$9, $$4, $$11);
                  this.lightEngine.checkBlock($$0);
               }
            }

            EnumSet<Heightmap.Types> $$14 = this.getPersistedStatus().heightmapsAfter();
            EnumSet<Heightmap.Types> $$15 = null;

            for (Heightmap.Types $$16 : $$14) {
               Heightmap $$17 = this.heightmaps.get($$16);
               if ($$17 == null) {
                  if ($$15 == null) {
                     $$15 = EnumSet.noneOf(Heightmap.Types.class);
                  }

                  $$15.add($$16);
               }
            }

            if ($$15 != null) {
               Heightmap.primeHeightmaps(this, $$15);
            }

            for (Heightmap.Types $$18 : $$14) {
               this.heightmaps.get($$18).update($$9, $$4, $$11, $$1);
            }

            return $$12;
         }
      }
   }

   @Override
   public void setBlockEntity(BlockEntity $$0) {
      this.pendingBlockEntities.remove($$0.getBlockPos());
      this.blockEntities.put($$0.getBlockPos(), $$0);
   }

   
   @Override
   public BlockEntity getBlockEntity(BlockPos $$0) {
      return this.blockEntities.get($$0);
   }

   public Map<BlockPos, BlockEntity> getBlockEntities() {
      return this.blockEntities;
   }

   public void addEntity(CompoundTag $$0) {
      this.entities.add($$0);
   }

   @Override
   public void addEntity(Entity $$0) {
      if (!$$0.isPassenger()) {
         ScopedCollector $$1 = new ScopedCollector($$0.problemPath(), LOGGER);

         try {
            TagValueOutput $$2 = TagValueOutput.createWithContext($$1, $$0.registryAccess());
            $$0.save($$2);
            this.addEntity($$2.buildResult());
         } catch (Throwable var6) {
            try {
               $$1.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         $$1.close();
      }
   }

   @Override
   public void setStartForStructure(Structure $$0, StructureStart $$1) {
      BelowZeroRetrogen $$2 = this.getBelowZeroRetrogen();
      if ($$2 != null && $$1.isValid()) {
         BoundingBox $$3 = $$1.getBoundingBox();
         net.minecraft.world.level.LevelHeightAccessor $$4 = this.getHeightAccessorForGeneration();
         if ($$3.minY() < $$4.getMinY() || $$3.maxY() > $$4.getMaxY()) {
            return;
         }
      }

      super.setStartForStructure($$0, $$1);
   }

   public List<CompoundTag> getEntities() {
      return this.entities;
   }

   @Override
   public ChunkStatus getPersistedStatus() {
      return this.status;
   }

   public void setPersistedStatus(ChunkStatus $$0) {
      this.status = $$0;
      if (this.belowZeroRetrogen != null && $$0.isOrAfter(this.belowZeroRetrogen.targetStatus())) {
         this.setBelowZeroRetrogen(null);
      }

      this.markUnsaved();
   }

   @Override
   public Holder<Biome> getNoiseBiome(int $$0, int $$1, int $$2) {
      if (this.getHighestGeneratedStatus().isOrAfter(ChunkStatus.BIOMES)) {
         return super.getNoiseBiome($$0, $$1, $$2);
      } else {
         throw new IllegalStateException("Asking for biomes before we have biomes");
      }
   }

   public static short packOffsetCoordinates(BlockPos $$0) {
      int $$1 = $$0.getX();
      int $$2 = $$0.getY();
      int $$3 = $$0.getZ();
      int $$4 = $$1 & 15;
      int $$5 = $$2 & 15;
      int $$6 = $$3 & 15;
      return (short)($$4 | $$5 << 4 | $$6 << 8);
   }

   public static BlockPos unpackOffsetCoordinates(short $$0, int $$1, net.minecraft.world.level.ChunkPos $$2) {
      int $$3 = SectionPos.sectionToBlockCoord($$2.x, $$0 & 15);
      int $$4 = SectionPos.sectionToBlockCoord($$1, $$0 >>> 4 & 15);
      int $$5 = SectionPos.sectionToBlockCoord($$2.z, $$0 >>> 8 & 15);
      return new BlockPos($$3, $$4, $$5);
   }

   @Override
   public void markPosForPostprocessing(BlockPos $$0) {
      if (!this.isOutsideBuildHeight($$0)) {
         ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex($$0.getY())).add(packOffsetCoordinates($$0));
      }
   }

   @Override
   public void addPackedPostProcess(ShortList $$0, int $$1) {
      ChunkAccess.getOrCreateOffsetList(this.postProcessing, $$1).addAll($$0);
   }

   public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
      return Collections.unmodifiableMap(this.pendingBlockEntities);
   }

   
   @Override
   public CompoundTag getBlockEntityNbtForSaving(BlockPos $$0, Provider $$1) {
      BlockEntity $$2 = this.getBlockEntity($$0);
      return $$2 != null ? $$2.saveWithFullMetadata($$1) : this.pendingBlockEntities.get($$0);
   }

   @Override
   public void removeBlockEntity(BlockPos $$0) {
      this.blockEntities.remove($$0);
      this.pendingBlockEntities.remove($$0);
   }

   
   public CarvingMask getCarvingMask() {
      return this.carvingMask;
   }

   public CarvingMask getOrCreateCarvingMask() {
      if (this.carvingMask == null) {
         this.carvingMask = new CarvingMask(this.getHeight(), this.getMinY());
      }

      return this.carvingMask;
   }

   public void setCarvingMask(CarvingMask $$0) {
      this.carvingMask = $$0;
   }

   public void setLightEngine(LevelLightEngine $$0) {
      this.lightEngine = $$0;
   }

   public void setBelowZeroRetrogen(BelowZeroRetrogen $$0) {
      this.belowZeroRetrogen = $$0;
   }

   
   @Override
   public BelowZeroRetrogen getBelowZeroRetrogen() {
      return this.belowZeroRetrogen;
   }

   private static <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTicks<T> $$0) {
      return new LevelChunkTicks($$0.scheduledTicks());
   }

   public LevelChunkTicks<Block> unpackBlockTicks() {
      return unpackTicks(this.blockTicks);
   }

   public LevelChunkTicks<Fluid> unpackFluidTicks() {
      return unpackTicks(this.fluidTicks);
   }

   @Override
   public net.minecraft.world.level.LevelHeightAccessor getHeightAccessorForGeneration() {
      return (net.minecraft.world.level.LevelHeightAccessor)(this.isUpgrading() ? BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR : this);
   }
}
