package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block.UpdateFlags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.WorldGenTickAccess;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class WorldGenRegion implements WorldGenLevel {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final StaticCache2D<GenerationChunkHolder> cache;
   private final ChunkAccess center;
   private final ServerLevel level;
   private final long seed;
   private final LevelData levelData;
   private final RandomSource random;
   private final DimensionType dimensionType;
   private final WorldGenTickAccess<Block> blockTicks = new WorldGenTickAccess($$0x -> this.getChunk($$0x).getBlockTicks());
   private final WorldGenTickAccess<Fluid> fluidTicks = new WorldGenTickAccess($$0x -> this.getChunk($$0x).getFluidTicks());
   private final BiomeManager biomeManager;
   private final ChunkStep generatingStep;
   @Nullable
   private Supplier<String> currentlyGenerating;
   private final AtomicLong subTickCount = new AtomicLong();
   private static final Identifier WORLDGEN_REGION_RANDOM = Identifier.withDefaultNamespace("worldgen_region_random");

   public WorldGenRegion(ServerLevel $$0, StaticCache2D<GenerationChunkHolder> $$1, ChunkStep $$2, ChunkAccess $$3) {
      this.generatingStep = $$2;
      this.cache = $$1;
      this.center = $$3;
      this.level = $$0;
      this.seed = $$0.getSeed();
      this.levelData = $$0.getLevelData();
      this.random = $$0.getChunkSource().randomState().getOrCreateRandomFactory(WORLDGEN_REGION_RANDOM).at(this.center.getPos().getWorldPosition());
      this.dimensionType = $$0.dimensionType();
      this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(this.seed));
   }

   public boolean isOldChunkAround(ChunkPos $$0, int $$1) {
      return this.level.getChunkSource().chunkMap.isOldChunkAround($$0, $$1);
   }

   public ChunkPos getCenter() {
      return this.center.getPos();
   }

   public void setCurrentlyGenerating(@Nullable Supplier<String> $$0) {
      this.currentlyGenerating = $$0;
   }

   public ChunkAccess getChunk(int $$0, int $$1) {
      return this.getChunk($$0, $$1, ChunkStatus.EMPTY);
   }

   @Nullable
   public ChunkAccess getChunk(int $$0, int $$1, ChunkStatus $$2, boolean $$3) {
      int $$4 = this.center.getPos().getChessboardDistance($$0, $$1);
      ChunkStatus $$5 = $$4 >= this.generatingStep.directDependencies().size() ? null : this.generatingStep.directDependencies().get($$4);
      GenerationChunkHolder $$6;
      if ($$5 != null) {
         $$6 = (GenerationChunkHolder)this.cache.get($$0, $$1);
         if ($$2.isOrBefore($$5)) {
            ChunkAccess $$7 = $$6.getChunkIfPresentUnchecked($$5);
            if ($$7 != null) {
               return $$7;
            }
         }
      } else {
         $$6 = null;
      }

      CrashReport $$9 = CrashReport.forThrowable(
         new IllegalStateException("Requested chunk unavailable during world generation"), "Exception generating new chunk"
      );
      CrashReportCategory $$10 = $$9.addCategory("Chunk request details");
      $$10.setDetail("Requested chunk", String.format(Locale.ROOT, "%d, %d", $$0, $$1));
      $$10.setDetail("Generating status", () -> this.generatingStep.targetStatus().getName());
      $$10.setDetail("Requested status", $$2::getName);
      $$10.setDetail("Actual status", () -> $$6 == null ? "[out of cache bounds]" : $$6.getPersistedStatus().getName());
      $$10.setDetail("Maximum allowed status", () -> $$5 == null ? "null" : $$5.getName());
      $$10.setDetail("Dependencies", this.generatingStep.directDependencies()::toString);
      $$10.setDetail("Requested distance", $$4);
      $$10.setDetail("Generating chunk", this.center.getPos()::toString);
      throw new ReportedException($$9);
   }

   public boolean hasChunk(int $$0, int $$1) {
      int $$2 = this.center.getPos().getChessboardDistance($$0, $$1);
      return $$2 < this.generatingStep.directDependencies().size();
   }

   public BlockState getBlockState(BlockPos $$0) {
      return this.getChunk(SectionPos.blockToSectionCoord($$0.getX()), SectionPos.blockToSectionCoord($$0.getZ())).getBlockState($$0);
   }

   public FluidState getFluidState(BlockPos $$0) {
      return this.getChunk($$0).getFluidState($$0);
   }

   @Nullable
   public Player getNearestPlayer(double $$0, double $$1, double $$2, double $$3, @Nullable Predicate<Entity> $$4) {
      return null;
   }

   public int getSkyDarken() {
      return 0;
   }

   public BiomeManager getBiomeManager() {
      return this.biomeManager;
   }

   public Holder<Biome> getUncachedNoiseBiome(int $$0, int $$1, int $$2) {
      return this.level.getUncachedNoiseBiome($$0, $$1, $$2);
   }

   public float getShade(Direction $$0, boolean $$1) {
      return 1.0F;
   }

   public LevelLightEngine getLightEngine() {
      return this.level.getLightEngine();
   }

   public boolean destroyBlock(BlockPos $$0, boolean $$1, @Nullable Entity $$2, int $$3) {
      BlockState $$4 = this.getBlockState($$0);
      if ($$4.isAir()) {
         return false;
      } else {
         if ($$1) {
            BlockEntity $$5 = $$4.hasBlockEntity() ? this.getBlockEntity($$0) : null;
            Block.dropResources($$4, this.level, $$0, $$5, $$2, ItemStack.EMPTY);
         }

         return this.setBlock($$0, Blocks.AIR.defaultBlockState(), 3, $$3);
      }
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos $$0) {
      ChunkAccess $$1 = this.getChunk($$0);
      BlockEntity $$2 = $$1.getBlockEntity($$0);
      if ($$2 != null) {
         return $$2;
      } else {
         CompoundTag $$3 = $$1.getBlockEntityNbt($$0);
         BlockState $$4 = $$1.getBlockState($$0);
         if ($$3 != null) {
            if ("DUMMY".equals($$3.getStringOr("id", ""))) {
               if (!$$4.hasBlockEntity()) {
                  return null;
               }

               $$2 = ((EntityBlock)$$4.getBlock()).newBlockEntity($$0, $$4);
            } else {
               $$2 = BlockEntity.loadStatic($$0, $$4, $$3, this.level.registryAccess());
            }

            if ($$2 != null) {
               $$1.setBlockEntity($$2);
               return $$2;
            }
         }

         if ($$4.hasBlockEntity()) {
            LOGGER.warn("Tried to access a block entity before it was created. {}", $$0);
         }

         return null;
      }
   }

   public boolean ensureCanWrite(BlockPos $$0) {
      int $$1 = SectionPos.blockToSectionCoord($$0.getX());
      int $$2 = SectionPos.blockToSectionCoord($$0.getZ());
      ChunkPos $$3 = this.getCenter();
      int $$4 = Math.abs($$3.x - $$1);
      int $$5 = Math.abs($$3.z - $$2);
      if ($$4 <= this.generatingStep.blockStateWriteRadius() && $$5 <= this.generatingStep.blockStateWriteRadius()) {
         if (this.center.isUpgrading()) {
            LevelHeightAccessor $$6 = this.center.getHeightAccessorForGeneration();
            if ($$6.isOutsideBuildHeight($$0.getY())) {
               return false;
            }
         }

         return true;
      } else {
         Util.logAndPauseIfInIde(
            "Detected setBlock in a far chunk ["
               + $$1
               + ", "
               + $$2
               + "], pos: "
               + $$0
               + ", status: "
               + this.generatingStep.targetStatus()
               + (this.currentlyGenerating == null ? "" : ", currently generating: " + this.currentlyGenerating.get())
         );
         return false;
      }
   }

   public boolean setBlock(BlockPos $$0, BlockState $$1, @UpdateFlags int $$2, int $$3) {
      if (!this.ensureCanWrite($$0)) {
         return false;
      } else {
         ChunkAccess $$4 = this.getChunk($$0);
         BlockState $$5 = $$4.setBlockState($$0, $$1, $$2);
         if ($$5 != null) {
            this.level.updatePOIOnBlockStateChange($$0, $$5, $$1);
         }

         if ($$1.hasBlockEntity()) {
            if ($$4.getPersistedStatus().getChunkType() == ChunkType.LEVELCHUNK) {
               BlockEntity $$6 = ((EntityBlock)$$1.getBlock()).newBlockEntity($$0, $$1);
               if ($$6 != null) {
                  $$4.setBlockEntity($$6);
               } else {
                  $$4.removeBlockEntity($$0);
               }
            } else {
               CompoundTag $$7 = new CompoundTag();
               $$7.putInt("x", $$0.getX());
               $$7.putInt("y", $$0.getY());
               $$7.putInt("z", $$0.getZ());
               $$7.putString("id", "DUMMY");
               $$4.setBlockEntityNbt($$7);
            }
         } else if ($$5 != null && $$5.hasBlockEntity()) {
            $$4.removeBlockEntity($$0);
         }

         if ($$1.hasPostProcess(this, $$0) && ($$2 & 16) == 0) {
            this.markPosForPostprocessing($$0);
         }

         return true;
      }
   }

   private void markPosForPostprocessing(BlockPos $$0) {
      this.getChunk($$0).markPosForPostprocessing($$0);
   }

   public boolean addFreshEntity(Entity $$0) {
      int $$1 = SectionPos.blockToSectionCoord($$0.getBlockX());
      int $$2 = SectionPos.blockToSectionCoord($$0.getBlockZ());
      this.getChunk($$1, $$2).addEntity($$0);
      return true;
   }

   public boolean removeBlock(BlockPos $$0, boolean $$1) {
      return this.setBlock($$0, Blocks.AIR.defaultBlockState(), 3);
   }

   public WorldBorder getWorldBorder() {
      return this.level.getWorldBorder();
   }

   public boolean isClientSide() {
      return false;
   }

   @Deprecated
   public ServerLevel getLevel() {
      return this.level;
   }

   public RegistryAccess registryAccess() {
      return this.level.registryAccess();
   }

   public FeatureFlagSet enabledFeatures() {
      return this.level.enabledFeatures();
   }

   public LevelData getLevelData() {
      return this.levelData;
   }

   public DifficultyInstance getCurrentDifficultyAt(BlockPos $$0) {
      if (!this.hasChunk(SectionPos.blockToSectionCoord($$0.getX()), SectionPos.blockToSectionCoord($$0.getZ()))) {
         throw new RuntimeException("We are asking a region for a chunk out of bound");
      } else {
         return new DifficultyInstance(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness($$0));
      }
   }

   @Nullable
   public net.minecraft.server.MinecraftServer getServer() {
      return this.level.getServer();
   }

   public ChunkSource getChunkSource() {
      return this.level.getChunkSource();
   }

   public long getSeed() {
      return this.seed;
   }

   public LevelTickAccess<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public LevelTickAccess<Fluid> getFluidTicks() {
      return this.fluidTicks;
   }

   public int getSeaLevel() {
      return this.level.getSeaLevel();
   }

   public RandomSource getRandom() {
      return this.random;
   }

   public int getHeight(Types $$0, int $$1, int $$2) {
      return this.getChunk(SectionPos.blockToSectionCoord($$1), SectionPos.blockToSectionCoord($$2)).getHeight($$0, $$1 & 15, $$2 & 15) + 1;
   }

   public void playSound(@Nullable Entity $$0, BlockPos $$1, SoundEvent $$2, SoundSource $$3, float $$4, float $$5) {
   }

   public void addParticle(ParticleOptions $$0, double $$1, double $$2, double $$3, double $$4, double $$5, double $$6) {
   }

   public void levelEvent(@Nullable Entity $$0, int $$1, BlockPos $$2, int $$3) {
   }

   public void gameEvent(Holder<GameEvent> $$0, Vec3 $$1, Context $$2) {
   }

   public DimensionType dimensionType() {
      return this.dimensionType;
   }

   public boolean isStateAtPosition(BlockPos $$0, Predicate<BlockState> $$1) {
      return $$1.test(this.getBlockState($$0));
   }

   public boolean isFluidAtPosition(BlockPos $$0, Predicate<FluidState> $$1) {
      return $$1.test(this.getFluidState($$0));
   }

   public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> $$0, AABB $$1, Predicate<? super T> $$2) {
      return Collections.emptyList();
   }

   public List<Entity> getEntities(@Nullable Entity $$0, AABB $$1, @Nullable Predicate<? super Entity> $$2) {
      return Collections.emptyList();
   }

   public List<Player> players() {
      return Collections.emptyList();
   }

   public int getMinY() {
      return this.level.getMinY();
   }

   public int getHeight() {
      return this.level.getHeight();
   }

   public long nextSubTickCount() {
      return this.subTickCount.getAndIncrement();
   }

   public EnvironmentAttributeReader environmentAttributes() {
      return EnvironmentAttributeReader.EMPTY;
   }
}
