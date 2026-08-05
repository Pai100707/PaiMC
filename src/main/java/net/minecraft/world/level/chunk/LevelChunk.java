package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.util.debug.DebugStructureInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.util.debug.DebugStructureInfo.Piece;
import net.minecraft.util.debug.DebugValueSource.Registration;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.EuclideanGameEventListenerRegistry;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public class LevelChunk extends ChunkAccess implements DebugValueSource {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final TickingBlockEntity NULL_TICKER = new TickingBlockEntity() {
      @Override
      public void tick() {
      }

      @Override
      public boolean isRemoved() {
         return true;
      }

      @Override
      public BlockPos getPos() {
         return BlockPos.ZERO;
      }

      @Override
      public String getType() {
         return "<null>";
      }
   };
   private final Map<BlockPos, LevelChunk.RebindableTickingBlockEntityWrapper> tickersInLevel = Maps.newHashMap();
   private boolean loaded;
   final net.minecraft.world.level.Level level;
   
   private Supplier<FullChunkStatus> fullStatus;
   
   private LevelChunk.PostLoadProcessor postLoad;
   private final Int2ObjectMap<GameEventListenerRegistry> gameEventListenerRegistrySections;
   private final LevelChunkTicks<Block> blockTicks;
   private final LevelChunkTicks<Fluid> fluidTicks;
   private LevelChunk.UnsavedListener unsavedListener = $$0x -> {};

   public LevelChunk(net.minecraft.world.level.Level $$0, net.minecraft.world.level.ChunkPos $$1) {
      this($$0, $$1, UpgradeData.EMPTY, new LevelChunkTicks(), new LevelChunkTicks(), 0L, null, null, null);
   }

   public LevelChunk(
      net.minecraft.world.level.Level $$0,
      net.minecraft.world.level.ChunkPos $$1,
      UpgradeData $$2,
      LevelChunkTicks<Block> $$3,
      LevelChunkTicks<Fluid> $$4,
      long $$5,
      LevelChunkSection[] $$6,
      LevelChunk.PostLoadProcessor $$7,
      BlendingData $$8
   ) {
      super($$1, $$2, $$0, $$0.palettedContainerFactory(), $$5, $$6, $$8);
      this.level = $$0;
      this.gameEventListenerRegistrySections = new Int2ObjectOpenHashMap();

      for (Heightmap.Types $$9 : Heightmap.Types.values()) {
         if (ChunkStatus.FULL.heightmapsAfter().contains($$9)) {
            this.heightmaps.put($$9, new Heightmap(this, $$9));
         }
      }

      this.postLoad = $$7;
      this.blockTicks = $$3;
      this.fluidTicks = $$4;
   }

   public LevelChunk(ServerLevel $$0, ProtoChunk $$1, LevelChunk.PostLoadProcessor $$2) {
      this(
         $$0,
         $$1.getPos(),
         $$1.getUpgradeData(),
         $$1.unpackBlockTicks(),
         $$1.unpackFluidTicks(),
         $$1.getInhabitedTime(),
         $$1.getSections(),
         $$2,
         $$1.getBlendingData()
      );
      if (!Collections.disjoint($$1.pendingBlockEntities.keySet(), $$1.blockEntities.keySet())) {
         LOGGER.error("Chunk at {} contains duplicated block entities", $$1.getPos());
      }

      for (BlockEntity $$3 : $$1.getBlockEntities().values()) {
         this.setBlockEntity($$3);
      }

      this.pendingBlockEntities.putAll($$1.getBlockEntityNbts());

      for (int $$4 = 0; $$4 < $$1.getPostProcessing().length; $$4++) {
         this.postProcessing[$$4] = $$1.getPostProcessing()[$$4];
      }

      this.setAllStarts($$1.getAllStarts());
      this.setAllReferences($$1.getAllReferences());

      for (Entry<Heightmap.Types, Heightmap> $$5 : $$1.getHeightmaps()) {
         if (ChunkStatus.FULL.heightmapsAfter().contains($$5.getKey())) {
            this.setHeightmap($$5.getKey(), $$5.getValue().getRawData());
         }
      }

      this.skyLightSources = $$1.skyLightSources;
      this.setLightCorrect($$1.isLightCorrect());
      this.markUnsaved();
   }

   public void setUnsavedListener(LevelChunk.UnsavedListener $$0) {
      this.unsavedListener = $$0;
      if (this.isUnsaved()) {
         $$0.setUnsaved(this.chunkPos);
      }
   }

   @Override
   public void markUnsaved() {
      boolean $$0 = this.isUnsaved();
      super.markUnsaved();
      if (!$$0) {
         this.unsavedListener.setUnsaved(this.chunkPos);
      }
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
   public GameEventListenerRegistry getListenerRegistry(int $$0) {
      return this.level instanceof ServerLevel $$1
         ? (GameEventListenerRegistry)this.gameEventListenerRegistrySections
            .computeIfAbsent($$0, $$2 -> new EuclideanGameEventListenerRegistry($$1, $$0, this::removeGameEventListenerRegistry))
         : super.getListenerRegistry($$0);
   }

   @Override
   public BlockState getBlockState(BlockPos $$0) {
      int $$1 = $$0.getX();
      int $$2 = $$0.getY();
      int $$3 = $$0.getZ();
      if (this.level.isDebug()) {
         BlockState $$4 = null;
         if ($$2 == 60) {
            $$4 = Blocks.BARRIER.defaultBlockState();
         }

         if ($$2 == 70) {
            $$4 = DebugLevelSource.getBlockStateFor($$1, $$3);
         }

         return $$4 == null ? Blocks.AIR.defaultBlockState() : $$4;
      } else {
         try {
            int $$5 = this.getSectionIndex($$2);
            if ($$5 >= 0 && $$5 < this.sections.length) {
               LevelChunkSection $$6 = this.sections[$$5];
               if (!$$6.hasOnlyAir()) {
                  return $$6.getBlockState($$1 & 15, $$2 & 15, $$3 & 15);
               }
            }

            return Blocks.AIR.defaultBlockState();
         } catch (Throwable var8) {
            CrashReport $$8 = CrashReport.forThrowable(var8, "Getting block state");
            CrashReportCategory $$9 = $$8.addCategory("Block being got");
            $$9.setDetail("Location", () -> CrashReportCategory.formatLocation(this, $$1, $$2, $$3));
            throw new ReportedException($$8);
         }
      }
   }

   @Override
   public FluidState getFluidState(BlockPos $$0) {
      return this.getFluidState($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public FluidState getFluidState(int $$0, int $$1, int $$2) {
      try {
         int $$3 = this.getSectionIndex($$1);
         if ($$3 >= 0 && $$3 < this.sections.length) {
            LevelChunkSection $$4 = this.sections[$$3];
            if (!$$4.hasOnlyAir()) {
               return $$4.getFluidState($$0 & 15, $$1 & 15, $$2 & 15);
            }
         }

         return Fluids.EMPTY.defaultFluidState();
      } catch (Throwable var7) {
         CrashReport $$6 = CrashReport.forThrowable(var7, "Getting fluid state");
         CrashReportCategory $$7 = $$6.addCategory("Block being got");
         $$7.setDetail("Location", () -> CrashReportCategory.formatLocation(this, $$0, $$1, $$2));
         throw new ReportedException($$6);
      }
   }

   
   @Override
   public BlockState setBlockState(BlockPos $$0, BlockState $$1, @Block.UpdateFlags int $$2) {
      int $$3 = $$0.getY();
      LevelChunkSection $$4 = this.getSection(this.getSectionIndex($$3));
      boolean $$5 = $$4.hasOnlyAir();
      if ($$5 && $$1.isAir()) {
         return null;
      } else {
         int $$6 = $$0.getX() & 15;
         int $$7 = $$3 & 15;
         int $$8 = $$0.getZ() & 15;
         BlockState $$9 = $$4.setBlockState($$6, $$7, $$8, $$1);
         if ($$9 == $$1) {
            return null;
         } else {
            Block $$10 = $$1.getBlock();
            this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update($$6, $$3, $$8, $$1);
            this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update($$6, $$3, $$8, $$1);
            this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update($$6, $$3, $$8, $$1);
            this.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update($$6, $$3, $$8, $$1);
            boolean $$11 = $$4.hasOnlyAir();
            if ($$5 != $$11) {
               this.level.getChunkSource().getLightEngine().updateSectionStatus($$0, $$11);
               this.level.getChunkSource().onSectionEmptinessChanged(this.chunkPos.x, SectionPos.blockToSectionCoord($$3), this.chunkPos.z, $$11);
            }

            if (LightEngine.hasDifferentLightProperties($$9, $$1)) {
               ProfilerFiller $$12 = Profiler.get();
               $$12.push("updateSkyLightSources");
               this.skyLightSources.update(this, $$6, $$3, $$8);
               $$12.popPush("queueCheckLight");
               this.level.getChunkSource().getLightEngine().checkBlock($$0);
               $$12.pop();
            }

            boolean $$13 = !$$9.is($$10);
            boolean $$14 = ($$2 & 64) != 0;
            boolean $$15 = ($$2 & 256) == 0;
            if ($$13 && $$9.hasBlockEntity() && !$$1.shouldChangedStateKeepBlockEntity($$9)) {
               if (!this.level.isClientSide() && $$15) {
                  BlockEntity $$16 = this.level.getBlockEntity($$0);
                  if ($$16 != null) {
                     $$16.preRemoveSideEffects($$0, $$9);
                  }
               }

               this.removeBlockEntity($$0);
            }

            if (($$13 || $$10 instanceof BaseRailBlock) && this.level instanceof ServerLevel $$17 && (($$2 & 1) != 0 || $$14)) {
               $$9.affectNeighborsAfterRemoval($$17, $$0, $$14);
            }

            if (!$$4.getBlockState($$6, $$7, $$8).is($$10)) {
               return null;
            } else {
               if (!this.level.isClientSide() && ($$2 & 512) == 0) {
                  $$1.onPlace(this.level, $$0, $$9, $$14);
               }

               if ($$1.hasBlockEntity()) {
                  BlockEntity $$18 = this.getBlockEntity($$0, LevelChunk.EntityCreationType.CHECK);
                  if ($$18 != null && !$$18.isValidBlockState($$1)) {
                     LOGGER.warn(
                        "Found mismatched block entity @ {}: type = {}, state = {}",
                        new Object[]{$$0, $$18.getType().builtInRegistryHolder().key().identifier(), $$1}
                     );
                     this.removeBlockEntity($$0);
                     $$18 = null;
                  }

                  if ($$18 == null) {
                     $$18 = ((EntityBlock)$$10).newBlockEntity($$0, $$1);
                     if ($$18 != null) {
                        this.addAndRegisterBlockEntity($$18);
                     }
                  } else {
                     $$18.setBlockState($$1);
                     this.updateBlockEntityTicker($$18);
                  }
               }

               this.markUnsaved();
               return $$9;
            }
         }
      }
   }

   @Deprecated
   @Override
   public void addEntity(Entity $$0) {
   }

   
   private BlockEntity createBlockEntity(BlockPos $$0) {
      BlockState $$1 = this.getBlockState($$0);
      return !$$1.hasBlockEntity() ? null : ((EntityBlock)$$1.getBlock()).newBlockEntity($$0, $$1);
   }

   
   @Override
   public BlockEntity getBlockEntity(BlockPos $$0) {
      return this.getBlockEntity($$0, LevelChunk.EntityCreationType.CHECK);
   }

   
   public BlockEntity getBlockEntity(BlockPos $$0, LevelChunk.EntityCreationType $$1) {
      BlockEntity $$2 = this.blockEntities.get($$0);
      if ($$2 == null) {
         CompoundTag $$3 = this.pendingBlockEntities.remove($$0);
         if ($$3 != null) {
            BlockEntity $$4 = this.promotePendingBlockEntity($$0, $$3);
            if ($$4 != null) {
               return $$4;
            }
         }
      }

      if ($$2 == null) {
         if ($$1 == LevelChunk.EntityCreationType.IMMEDIATE) {
            $$2 = this.createBlockEntity($$0);
            if ($$2 != null) {
               this.addAndRegisterBlockEntity($$2);
            }
         }
      } else if ($$2.isRemoved()) {
         this.blockEntities.remove($$0);
         return null;
      }

      return $$2;
   }

   public void addAndRegisterBlockEntity(BlockEntity $$0) {
      this.setBlockEntity($$0);
      if (this.isInLevel()) {
         if (this.level instanceof ServerLevel $$1) {
            this.addGameEventListener($$0, $$1);
         }

         this.level.onBlockEntityAdded($$0);
         this.updateBlockEntityTicker($$0);
      }
   }

   private boolean isInLevel() {
      return this.loaded || this.level.isClientSide();
   }

   boolean isTicking(BlockPos $$0) {
      if (!this.level.getWorldBorder().isWithinBounds($$0)) {
         return false;
      } else {
         return !(this.level instanceof ServerLevel $$1)
            ? true
            : this.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING) && $$1.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong($$0));
      }
   }

   @Override
   public void setBlockEntity(BlockEntity $$0) {
      BlockPos $$1 = $$0.getBlockPos();
      BlockState $$2 = this.getBlockState($$1);
      if (!$$2.hasBlockEntity()) {
         LOGGER.warn("Trying to set block entity {} at position {}, but state {} does not allow it", new Object[]{$$0, $$1, $$2});
      } else {
         BlockState $$3 = $$0.getBlockState();
         if ($$2 != $$3) {
            if (!$$0.getType().isValid($$2)) {
               LOGGER.warn("Trying to set block entity {} at position {}, but state {} does not allow it", new Object[]{$$0, $$1, $$2});
               return;
            }

            if ($$2.getBlock() != $$3.getBlock()) {
               LOGGER.warn("Block state mismatch on block entity {} in position {}, {} != {}, updating", new Object[]{$$0, $$1, $$2, $$3});
            }

            $$0.setBlockState($$2);
         }

         $$0.setLevel(this.level);
         $$0.clearRemoved();
         BlockEntity $$4 = this.blockEntities.put($$1.immutable(), $$0);
         if ($$4 != null && $$4 != $$0) {
            $$4.setRemoved();
         }
      }
   }

   
   @Override
   public CompoundTag getBlockEntityNbtForSaving(BlockPos $$0, Provider $$1) {
      BlockEntity $$2 = this.getBlockEntity($$0);
      if ($$2 != null && !$$2.isRemoved()) {
         CompoundTag $$3 = $$2.saveWithFullMetadata(this.level.registryAccess());
         $$3.putBoolean("keepPacked", false);
         return $$3;
      } else {
         CompoundTag $$4 = this.pendingBlockEntities.get($$0);
         if ($$4 != null) {
            $$4 = $$4.copy();
            $$4.putBoolean("keepPacked", true);
         }

         return $$4;
      }
   }

   @Override
   public void removeBlockEntity(BlockPos $$0) {
      if (this.isInLevel()) {
         BlockEntity $$1 = this.blockEntities.remove($$0);
         if ($$1 != null) {
            if (this.level instanceof ServerLevel $$2) {
               this.removeGameEventListener($$1, $$2);
               $$2.debugSynchronizers().dropBlockEntity($$0);
            }

            $$1.setRemoved();
         }
      }

      this.removeBlockEntityTicker($$0);
   }

   private <T extends BlockEntity> void removeGameEventListener(T $$0, ServerLevel $$1) {
      Block $$2 = $$0.getBlockState().getBlock();
      if ($$2 instanceof EntityBlock) {
         GameEventListener $$3 = ((EntityBlock)$$2).getListener($$1, $$0);
         if ($$3 != null) {
            int $$4 = SectionPos.blockToSectionCoord($$0.getBlockPos().getY());
            GameEventListenerRegistry $$5 = this.getListenerRegistry($$4);
            $$5.unregister($$3);
         }
      }
   }

   private void removeGameEventListenerRegistry(int $$0) {
      this.gameEventListenerRegistrySections.remove($$0);
   }

   private void removeBlockEntityTicker(BlockPos $$0) {
      LevelChunk.RebindableTickingBlockEntityWrapper $$1 = this.tickersInLevel.remove($$0);
      if ($$1 != null) {
         $$1.rebind(NULL_TICKER);
      }
   }

   public void runPostLoad() {
      if (this.postLoad != null) {
         this.postLoad.run(this);
         this.postLoad = null;
      }
   }

   public boolean isEmpty() {
      return false;
   }

   public void replaceWithPacketData(FriendlyByteBuf $$0, Map<Heightmap.Types, long[]> $$1, Consumer<BlockEntityTagOutput> $$2) {
      this.clearAllBlockEntities();

      for (LevelChunkSection $$3 : this.sections) {
         $$3.read($$0);
      }

      $$1.forEach(this::setHeightmap);
      this.initializeLightSources();
      ScopedCollector $$4 = new ScopedCollector(this.problemPath(), LOGGER);

      try {
         $$2.accept(($$1x, $$2x, $$3x) -> {
            BlockEntity $$4x = this.getBlockEntity($$1x, LevelChunk.EntityCreationType.IMMEDIATE);
            if ($$4x != null && $$3x != null && $$4x.getType() == $$2x) {
               $$4x.loadWithComponents(TagValueInput.create($$4.forChild($$4x.problemPath()), this.level.registryAccess(), $$3x));
            }
         });
      } catch (Throwable var9) {
         try {
            $$4.close();
         } catch (Throwable var8) {
            var9.addSuppressed(var8);
         }

         throw var9;
      }

      $$4.close();
   }

   public void replaceBiomes(FriendlyByteBuf $$0) {
      for (LevelChunkSection $$1 : this.sections) {
         $$1.readBiomes($$0);
      }
   }

   public void setLoaded(boolean $$0) {
      this.loaded = $$0;
   }

   public net.minecraft.world.level.Level getLevel() {
      return this.level;
   }

   public Map<BlockPos, BlockEntity> getBlockEntities() {
      return this.blockEntities;
   }

   public void postProcessGeneration(ServerLevel $$0) {
      net.minecraft.world.level.ChunkPos $$1 = this.getPos();

      for (int $$2 = 0; $$2 < this.postProcessing.length; $$2++) {
         ShortList $$3 = this.postProcessing[$$2];
         if ($$3 != null) {
            ShortListIterator var5 = $$3.iterator();

            while (var5.hasNext()) {
               Short $$4 = (Short)var5.next();
               BlockPos $$5 = ProtoChunk.unpackOffsetCoordinates($$4, this.getSectionYFromSectionIndex($$2), $$1);
               BlockState $$6 = this.getBlockState($$5);
               FluidState $$7 = $$6.getFluidState();
               if (!$$7.isEmpty()) {
                  $$7.tick($$0, $$5, $$6);
               }

               if (!($$6.getBlock() instanceof LiquidBlock)) {
                  BlockState $$8 = Block.updateFromNeighbourShapes($$6, $$0, $$5);
                  if ($$8 != $$6) {
                     $$0.setBlock($$5, $$8, 276);
                  }
               }
            }

            $$3.clear();
         }
      }

      UnmodifiableIterator var11 = ImmutableList.copyOf(this.pendingBlockEntities.keySet()).iterator();

      while (var11.hasNext()) {
         BlockPos $$9 = (BlockPos)var11.next();
         this.getBlockEntity($$9);
      }

      this.pendingBlockEntities.clear();
      this.upgradeData.upgrade(this);
   }

   
   private BlockEntity promotePendingBlockEntity(BlockPos $$0, CompoundTag $$1) {
      BlockState $$2 = this.getBlockState($$0);
      BlockEntity $$3;
      if ("DUMMY".equals($$1.getStringOr("id", ""))) {
         if ($$2.hasBlockEntity()) {
            $$3 = ((EntityBlock)$$2.getBlock()).newBlockEntity($$0, $$2);
         } else {
            $$3 = null;
            LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", $$0, $$2);
         }
      } else {
         $$3 = BlockEntity.loadStatic($$0, $$2, $$1, this.level.registryAccess());
      }

      if ($$3 != null) {
         $$3.setLevel(this.level);
         this.addAndRegisterBlockEntity($$3);
      } else {
         LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", $$2, $$0);
      }

      return $$3;
   }

   public void unpackTicks(long $$0) {
      this.blockTicks.unpack($$0);
      this.fluidTicks.unpack($$0);
   }

   public void registerTickContainerInLevel(ServerLevel $$0) {
      $$0.getBlockTicks().addContainer(this.chunkPos, this.blockTicks);
      $$0.getFluidTicks().addContainer(this.chunkPos, this.fluidTicks);
   }

   public void unregisterTickContainerFromLevel(ServerLevel $$0) {
      $$0.getBlockTicks().removeContainer(this.chunkPos);
      $$0.getFluidTicks().removeContainer(this.chunkPos);
   }

   public void registerDebugValues(ServerLevel $$0, Registration $$1) {
      if (!this.getAllStarts().isEmpty()) {
         $$1.register(DebugSubscriptions.STRUCTURES, () -> {
            List<DebugStructureInfo> $$0x = new ArrayList<>();

            for (StructureStart $$1x : this.getAllStarts().values()) {
               BoundingBox $$2 = $$1x.getBoundingBox();
               List<StructurePiece> $$3 = $$1x.getPieces();
               List<Piece> $$4 = new ArrayList<>($$3.size());

               for (int $$5 = 0; $$5 < $$3.size(); $$5++) {
                  boolean $$6 = $$5 == 0;
                  $$4.add(new Piece($$3.get($$5).getBoundingBox(), $$6));
               }

               $$0x.add(new DebugStructureInfo($$2, $$4));
            }

            return $$0x;
         });
      }

      $$1.register(DebugSubscriptions.RAIDS, () -> $$0.getRaids().getRaidCentersInChunk(this.chunkPos));
   }

   @Override
   public ChunkStatus getPersistedStatus() {
      return ChunkStatus.FULL;
   }

   public FullChunkStatus getFullStatus() {
      return this.fullStatus == null ? FullChunkStatus.FULL : this.fullStatus.get();
   }

   public void setFullStatus(Supplier<FullChunkStatus> $$0) {
      this.fullStatus = $$0;
   }

   public void clearAllBlockEntities() {
      this.blockEntities.values().forEach(BlockEntity::setRemoved);
      this.blockEntities.clear();
      this.tickersInLevel.values().forEach($$0 -> $$0.rebind(NULL_TICKER));
      this.tickersInLevel.clear();
   }

   public void registerAllBlockEntitiesAfterLevelLoad() {
      this.blockEntities.values().forEach($$0 -> {
         if (this.level instanceof ServerLevel $$2) {
            this.addGameEventListener($$0, $$2);
         }

         this.level.onBlockEntityAdded($$0);
         this.updateBlockEntityTicker($$0);
      });
   }

   private <T extends BlockEntity> void addGameEventListener(T $$0, ServerLevel $$1) {
      Block $$2 = $$0.getBlockState().getBlock();
      if ($$2 instanceof EntityBlock) {
         GameEventListener $$3 = ((EntityBlock)$$2).getListener($$1, $$0);
         if ($$3 != null) {
            this.getListenerRegistry(SectionPos.blockToSectionCoord($$0.getBlockPos().getY())).register($$3);
         }
      }
   }

   private <T extends BlockEntity> void updateBlockEntityTicker(T $$0) {
      BlockState $$1 = $$0.getBlockState();
      BlockEntityTicker<T> $$2 = $$1.getTicker(this.level, (BlockEntityType<T>)$$0.getType());
      if ($$2 == null) {
         this.removeBlockEntityTicker($$0.getBlockPos());
      } else {
         this.tickersInLevel.compute($$0.getBlockPos(), ($$2x, $$3) -> {
            TickingBlockEntity $$4 = this.createTicker($$0, $$2);
            if ($$3 != null) {
               $$3.rebind($$4);
               return (LevelChunk.RebindableTickingBlockEntityWrapper)$$3;
            } else if (this.isInLevel()) {
               LevelChunk.RebindableTickingBlockEntityWrapper $$5 = new LevelChunk.RebindableTickingBlockEntityWrapper($$4);
               this.level.addBlockEntityTicker($$5);
               return $$5;
            } else {
               return null;
            }
         });
      }
   }

   private <T extends BlockEntity> TickingBlockEntity createTicker(T $$0, BlockEntityTicker<T> $$1) {
      return new LevelChunk.BoundTickingBlockEntity<>($$0, $$1);
   }

   class BoundTickingBlockEntity<T extends BlockEntity> implements TickingBlockEntity {
      private final T blockEntity;
      private final BlockEntityTicker<T> ticker;
      private boolean loggedInvalidBlockState;

      BoundTickingBlockEntity(final T $$0, final BlockEntityTicker<T> $$1) {
         this.blockEntity = $$0;
         this.ticker = $$1;
      }

      @Override
      public void tick() {
         if (!this.blockEntity.isRemoved() && this.blockEntity.hasLevel()) {
            BlockPos $$0 = this.blockEntity.getBlockPos();
            if (LevelChunk.this.isTicking($$0)) {
               try {
                  ProfilerFiller $$1 = Profiler.get();
                  $$1.push(this::getType);
                  BlockState $$2 = LevelChunk.this.getBlockState($$0);
                  if (this.blockEntity.getType().isValid($$2)) {
                     this.ticker.tick(LevelChunk.this.level, this.blockEntity.getBlockPos(), $$2, this.blockEntity);
                     this.loggedInvalidBlockState = false;
                  } else if (!this.loggedInvalidBlockState) {
                     this.loggedInvalidBlockState = true;
                     LevelChunk.LOGGER
                        .warn(
                           "Block entity {} @ {} state {} invalid for ticking:", new Object[]{LogUtils.defer(this::getType), LogUtils.defer(this::getPos), $$2}
                        );
                  }

                  $$1.pop();
               } catch (Throwable var5) {
                  CrashReport $$4 = CrashReport.forThrowable(var5, "Ticking block entity");
                  CrashReportCategory $$5 = $$4.addCategory("Block entity being ticked");
                  this.blockEntity.fillCrashReportCategory($$5);
                  throw new ReportedException($$4);
               }
            }
         }
      }

      @Override
      public boolean isRemoved() {
         return this.blockEntity.isRemoved();
      }

      @Override
      public BlockPos getPos() {
         return this.blockEntity.getBlockPos();
      }

      @Override
      public String getType() {
         return BlockEntityType.getKey(this.blockEntity.getType()).toString();
      }

      @Override
      public String toString() {
         return "Level ticker for " + this.getType() + "@" + this.getPos();
      }
   }

   public static enum EntityCreationType {
      IMMEDIATE,
      QUEUED,
      CHECK;
   }

   @FunctionalInterface
   public interface PostLoadProcessor {
      void run(LevelChunk var1);
   }

   static class RebindableTickingBlockEntityWrapper implements TickingBlockEntity {
      private TickingBlockEntity ticker;

      RebindableTickingBlockEntityWrapper(TickingBlockEntity $$0) {
         this.ticker = $$0;
      }

      void rebind(TickingBlockEntity $$0) {
         this.ticker = $$0;
      }

      @Override
      public void tick() {
         this.ticker.tick();
      }

      @Override
      public boolean isRemoved() {
         return this.ticker.isRemoved();
      }

      @Override
      public BlockPos getPos() {
         return this.ticker.getPos();
      }

      @Override
      public String getType() {
         return this.ticker.getType();
      }

      @Override
      public String toString() {
         return this.ticker + " <wrapped>";
      }
   }

   @FunctionalInterface
   public interface UnsavedListener {
      void setUnsaved(net.minecraft.world.level.ChunkPos var1);
   }
}
