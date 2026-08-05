package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import org.slf4j.Logger;

public class GameTestRunner {
   public static final int DEFAULT_TESTS_PER_ROW = 8;
   private static final Logger LOGGER = LogUtils.getLogger();
   final ServerLevel level;
   private final GameTestTicker testTicker;
   private final List<GameTestInfo> allTestInfos;
   private ImmutableList<GameTestBatch> batches;
   final List<GameTestBatchListener> batchListeners = Lists.newArrayList();
   private final List<GameTestInfo> scheduledForRerun = Lists.newArrayList();
   private final GameTestRunner.GameTestBatcher testBatcher;
   private boolean stopped = true;
   
   private Holder<TestEnvironmentDefinition> currentEnvironment;
   private final GameTestRunner.StructureSpawner existingStructureSpawner;
   private final GameTestRunner.StructureSpawner newStructureSpawner;
   final boolean haltOnError;
   private final boolean clearBetweenBatches;

   protected GameTestRunner(
      GameTestRunner.GameTestBatcher $$0,
      Collection<GameTestBatch> $$1,
      ServerLevel $$2,
      GameTestTicker $$3,
      GameTestRunner.StructureSpawner $$4,
      GameTestRunner.StructureSpawner $$5,
      boolean $$6,
      boolean $$7
   ) {
      this.level = $$2;
      this.testTicker = $$3;
      this.testBatcher = $$0;
      this.existingStructureSpawner = $$4;
      this.newStructureSpawner = $$5;
      this.batches = ImmutableList.copyOf($$1);
      this.haltOnError = $$6;
      this.clearBetweenBatches = $$7;
      this.allTestInfos = this.batches.stream().flatMap($$0x -> $$0x.gameTestInfos().stream()).collect(Util.toMutableList());
      $$3.setRunner(this);
      this.allTestInfos.forEach($$0x -> $$0x.addListener(new ReportGameListener()));
   }

   public List<GameTestInfo> getTestInfos() {
      return this.allTestInfos;
   }

   public void start() {
      this.stopped = false;
      this.runBatch(0);
   }

   public void stop() {
      this.stopped = true;
      if (this.currentEnvironment != null) {
         this.endCurrentEnvironment();
      }
   }

   public void rerunTest(GameTestInfo $$0) {
      GameTestInfo $$1 = $$0.copyReset();
      $$0.getListeners().forEach($$2 -> $$2.testAddedForRerun($$0, $$1, this));
      this.allTestInfos.add($$1);
      this.scheduledForRerun.add($$1);
      if (this.stopped) {
         this.runScheduledRerunTests();
      }
   }

   void runBatch(final int $$0) {
      if ($$0 >= this.batches.size()) {
         this.endCurrentEnvironment();
         this.runScheduledRerunTests();
      } else {
         if ($$0 > 0 && this.clearBetweenBatches) {
            GameTestBatch $$1 = (GameTestBatch)this.batches.get($$0 - 1);
            $$1.gameTestInfos().forEach($$0x -> {
               TestInstanceBlockEntity $$1x = $$0x.getTestInstanceBlockEntity();
               StructureUtils.clearSpaceForStructure($$1x.getStructureBoundingBox(), this.level);
               this.level.destroyBlock($$1x.getBlockPos(), false);
            });
         }

         final GameTestBatch $$2 = (GameTestBatch)this.batches.get($$0);
         this.existingStructureSpawner.onBatchStart(this.level);
         this.newStructureSpawner.onBatchStart(this.level);
         Collection<GameTestInfo> $$3 = this.createStructuresForBatch($$2.gameTestInfos());
         LOGGER.info("Running test environment '{}' batch {} ({} tests)...", new Object[]{$$2.environment().getRegisteredName(), $$2.index(), $$3.size()});
         this.endCurrentEnvironment();
         this.currentEnvironment = $$2.environment();
         ((TestEnvironmentDefinition)this.currentEnvironment.value()).setup(this.level);
         this.batchListeners.forEach($$1 -> $$1.testBatchStarting($$2));
         final MultipleTestTracker $$4 = new MultipleTestTracker();
         $$3.forEach($$4::addTestToTrack);
         $$4.addListener(new GameTestListener() {
            private void testCompleted(GameTestInfo $$0x) {
               $$0.getTestInstanceBlockEntity().removeBarriers();
               if ($$4.isDone()) {
                  GameTestRunner.this.batchListeners.forEach($$1x -> $$1x.testBatchFinished($$2));
                  LongSet $$1 = new LongArraySet(GameTestRunner.this.level.getForceLoadedChunks());
                  $$1.forEach($$0xxx -> GameTestRunner.this.level.setChunkForced(ChunkPos.getX($$0xxx), ChunkPos.getZ($$0xxx), false));
                  GameTestRunner.this.runBatch($$0 + 1);
               }
            }

            @Override
            public void testStructureLoaded(GameTestInfo $$0x) {
            }

            @Override
            public void testPassed(GameTestInfo $$0x, GameTestRunner $$1) {
               this.testCompleted($$0);
            }

            @Override
            public void testFailed(GameTestInfo $$0x, GameTestRunner $$1) {
               if (GameTestRunner.this.haltOnError) {
                  GameTestRunner.this.endCurrentEnvironment();
                  LongSet $$2x = new LongArraySet(GameTestRunner.this.level.getForceLoadedChunks());
                  $$2x.forEach($$0xxx -> GameTestRunner.this.level.setChunkForced(ChunkPos.getX($$0xxx), ChunkPos.getZ($$0xxx), false));
                  GameTestTicker.SINGLETON.clear();
                  $$0.getTestInstanceBlockEntity().removeBarriers();
               } else {
                  this.testCompleted($$0);
               }
            }

            @Override
            public void testAddedForRerun(GameTestInfo $$0x, GameTestInfo $$1, GameTestRunner $$2x) {
            }
         });
         $$3.forEach(this.testTicker::add);
      }
   }

   void endCurrentEnvironment() {
      if (this.currentEnvironment != null) {
         ((TestEnvironmentDefinition)this.currentEnvironment.value()).teardown(this.level);
         this.currentEnvironment = null;
      }
   }

   private void runScheduledRerunTests() {
      if (!this.scheduledForRerun.isEmpty()) {
         LOGGER.info("Starting re-run of tests: {}", this.scheduledForRerun.stream().map($$0 -> $$0.id().toString()).collect(Collectors.joining(", ")));
         this.batches = ImmutableList.copyOf(this.testBatcher.batch(this.scheduledForRerun));
         this.scheduledForRerun.clear();
         this.stopped = false;
         this.runBatch(0);
      } else {
         this.batches = ImmutableList.of();
         this.stopped = true;
      }
   }

   public void addListener(GameTestBatchListener $$0) {
      this.batchListeners.add($$0);
   }

   private Collection<GameTestInfo> createStructuresForBatch(Collection<GameTestInfo> $$0) {
      return $$0.stream().map(this::spawn).flatMap(Optional::stream).toList();
   }

   private Optional<GameTestInfo> spawn(GameTestInfo $$0) {
      return $$0.getTestBlockPos() == null ? this.newStructureSpawner.spawnStructure($$0) : this.existingStructureSpawner.spawnStructure($$0);
   }

   public static class Builder {
      private final ServerLevel level;
      private final GameTestTicker testTicker = GameTestTicker.SINGLETON;
      private GameTestRunner.GameTestBatcher batcher = GameTestBatchFactory.fromGameTestInfo();
      private GameTestRunner.StructureSpawner existingStructureSpawner = GameTestRunner.StructureSpawner.IN_PLACE;
      private GameTestRunner.StructureSpawner newStructureSpawner = GameTestRunner.StructureSpawner.NOT_SET;
      private final Collection<GameTestBatch> batches;
      private boolean haltOnError = false;
      private boolean clearBetweenBatches = false;

      private Builder(Collection<GameTestBatch> $$0, ServerLevel $$1) {
         this.batches = $$0;
         this.level = $$1;
      }

      public static GameTestRunner.Builder fromBatches(Collection<GameTestBatch> $$0, ServerLevel $$1) {
         return new GameTestRunner.Builder($$0, $$1);
      }

      public static GameTestRunner.Builder fromInfo(Collection<GameTestInfo> $$0, ServerLevel $$1) {
         return fromBatches(GameTestBatchFactory.fromGameTestInfo().batch($$0), $$1);
      }

      public GameTestRunner.Builder haltOnError() {
         this.haltOnError = true;
         return this;
      }

      public GameTestRunner.Builder clearBetweenBatches() {
         this.clearBetweenBatches = true;
         return this;
      }

      public GameTestRunner.Builder newStructureSpawner(GameTestRunner.StructureSpawner $$0) {
         this.newStructureSpawner = $$0;
         return this;
      }

      public GameTestRunner.Builder existingStructureSpawner(StructureGridSpawner $$0) {
         this.existingStructureSpawner = $$0;
         return this;
      }

      public GameTestRunner.Builder batcher(GameTestRunner.GameTestBatcher $$0) {
         this.batcher = $$0;
         return this;
      }

      public GameTestRunner build() {
         return new GameTestRunner(
            this.batcher,
            this.batches,
            this.level,
            this.testTicker,
            this.existingStructureSpawner,
            this.newStructureSpawner,
            this.haltOnError,
            this.clearBetweenBatches
         );
      }
   }

   public interface GameTestBatcher {
      Collection<GameTestBatch> batch(Collection<GameTestInfo> var1);
   }

   public interface StructureSpawner {
      GameTestRunner.StructureSpawner IN_PLACE = $$0 -> Optional.ofNullable($$0.prepareTestStructure()).map($$0x -> $$0x.startExecution(1));
      GameTestRunner.StructureSpawner NOT_SET = $$0 -> Optional.empty();

      Optional<GameTestInfo> spawnStructure(GameTestInfo var1);

      default void onBatchStart(ServerLevel $$0) {
      }
   }
}
