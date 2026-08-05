/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongArraySet
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchFactory;
import net.minecraft.gametest.framework.GameTestBatchListener;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.ReportGameListener;
import net.minecraft.gametest.framework.StructureGridSpawner;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import org.jspecify.annotations.Nullable;
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
    private final GameTestBatcher testBatcher;
    private boolean stopped = true;
    private @Nullable Holder<TestEnvironmentDefinition> currentEnvironment;
    private final StructureSpawner existingStructureSpawner;
    private final StructureSpawner newStructureSpawner;
    final boolean haltOnError;
    private final boolean clearBetweenBatches;

    protected GameTestRunner(GameTestBatcher $$02, Collection<GameTestBatch> $$1, ServerLevel $$2, GameTestTicker $$3, StructureSpawner $$4, StructureSpawner $$5, boolean $$6, boolean $$7) {
        this.level = $$2;
        this.testTicker = $$3;
        this.testBatcher = $$02;
        this.existingStructureSpawner = $$4;
        this.newStructureSpawner = $$5;
        this.batches = ImmutableList.copyOf($$1);
        this.haltOnError = $$6;
        this.clearBetweenBatches = $$7;
        this.allTestInfos = this.batches.stream().flatMap($$0 -> $$0.gameTestInfos().stream()).collect(Util.toMutableList());
        $$3.setRunner(this);
        this.allTestInfos.forEach($$0 -> $$0.addListener(new ReportGameListener()));
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

    void runBatch(final int $$02) {
        if ($$02 >= this.batches.size()) {
            this.endCurrentEnvironment();
            this.runScheduledRerunTests();
            return;
        }
        if ($$02 > 0 && this.clearBetweenBatches) {
            GameTestBatch $$12 = (GameTestBatch)this.batches.get($$02 - 1);
            $$12.gameTestInfos().forEach($$0 -> {
                TestInstanceBlockEntity $$1 = $$0.getTestInstanceBlockEntity();
                StructureUtils.clearSpaceForStructure($$1.getStructureBoundingBox(), this.level);
                this.level.destroyBlock($$1.getBlockPos(), false);
            });
        }
        final GameTestBatch $$2 = (GameTestBatch)this.batches.get($$02);
        this.existingStructureSpawner.onBatchStart(this.level);
        this.newStructureSpawner.onBatchStart(this.level);
        Collection<GameTestInfo> $$3 = this.createStructuresForBatch($$2.gameTestInfos());
        LOGGER.info("Running test environment '{}' batch {} ({} tests)...", new Object[]{$$2.environment().getRegisteredName(), $$2.index(), $$3.size()});
        this.endCurrentEnvironment();
        this.currentEnvironment = $$2.environment();
        this.currentEnvironment.value().setup(this.level);
        this.batchListeners.forEach($$1 -> $$1.testBatchStarting($$2));
        final MultipleTestTracker $$4 = new MultipleTestTracker();
        $$3.forEach($$4::addTestToTrack);
        $$4.addListener(new GameTestListener(){

            private void testCompleted(GameTestInfo $$022) {
                $$022.getTestInstanceBlockEntity().removeBarriers();
                if ($$4.isDone()) {
                    GameTestRunner.this.batchListeners.forEach($$1 -> $$1.testBatchFinished($$2));
                    LongArraySet $$12 = new LongArraySet(GameTestRunner.this.level.getForceLoadedChunks());
                    $$12.forEach($$0 -> GameTestRunner.this.level.setChunkForced(ChunkPos.getX($$0), ChunkPos.getZ($$0), false));
                    GameTestRunner.this.runBatch($$02 + 1);
                }
            }

            @Override
            public void testStructureLoaded(GameTestInfo $$0) {
            }

            @Override
            public void testPassed(GameTestInfo $$0, GameTestRunner $$1) {
                this.testCompleted($$0);
            }

            @Override
            public void testFailed(GameTestInfo $$022, GameTestRunner $$1) {
                if (GameTestRunner.this.haltOnError) {
                    GameTestRunner.this.endCurrentEnvironment();
                    LongArraySet $$22 = new LongArraySet(GameTestRunner.this.level.getForceLoadedChunks());
                    $$22.forEach($$0 -> GameTestRunner.this.level.setChunkForced(ChunkPos.getX($$0), ChunkPos.getZ($$0), false));
                    GameTestTicker.SINGLETON.clear();
                    $$022.getTestInstanceBlockEntity().removeBarriers();
                } else {
                    this.testCompleted($$022);
                }
            }

            @Override
            public void testAddedForRerun(GameTestInfo $$0, GameTestInfo $$1, GameTestRunner $$22) {
            }
        });
        $$3.forEach(this.testTicker::add);
    }

    void endCurrentEnvironment() {
        if (this.currentEnvironment != null) {
            this.currentEnvironment.value().teardown(this.level);
            this.currentEnvironment = null;
        }
    }

    private void runScheduledRerunTests() {
        if (!this.scheduledForRerun.isEmpty()) {
            LOGGER.info("Starting re-run of tests: {}", (Object)this.scheduledForRerun.stream().map($$0 -> $$0.id().toString()).collect(Collectors.joining(", ")));
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
        if ($$0.getTestBlockPos() == null) {
            return this.newStructureSpawner.spawnStructure($$0);
        }
        return this.existingStructureSpawner.spawnStructure($$0);
    }

    public static interface GameTestBatcher {
        public Collection<GameTestBatch> batch(Collection<GameTestInfo> var1);
    }

    public static interface StructureSpawner {
        public static final StructureSpawner IN_PLACE = $$02 -> Optional.ofNullable($$02.prepareTestStructure()).map($$0 -> $$0.startExecution(1));
        public static final StructureSpawner NOT_SET = $$0 -> Optional.empty();

        public Optional<GameTestInfo> spawnStructure(GameTestInfo var1);

        default public void onBatchStart(ServerLevel $$0) {
        }
    }

    public static class Builder {
        private final ServerLevel level;
        private final GameTestTicker testTicker = GameTestTicker.SINGLETON;
        private GameTestBatcher batcher = GameTestBatchFactory.fromGameTestInfo();
        private StructureSpawner existingStructureSpawner = StructureSpawner.IN_PLACE;
        private StructureSpawner newStructureSpawner = StructureSpawner.NOT_SET;
        private final Collection<GameTestBatch> batches;
        private boolean haltOnError = false;
        private boolean clearBetweenBatches = false;

        private Builder(Collection<GameTestBatch> $$0, ServerLevel $$1) {
            this.batches = $$0;
            this.level = $$1;
        }

        public static Builder fromBatches(Collection<GameTestBatch> $$0, ServerLevel $$1) {
            return new Builder($$0, $$1);
        }

        public static Builder fromInfo(Collection<GameTestInfo> $$0, ServerLevel $$1) {
            return Builder.fromBatches(GameTestBatchFactory.fromGameTestInfo().batch($$0), $$1);
        }

        public Builder haltOnError() {
            this.haltOnError = true;
            return this;
        }

        public Builder clearBetweenBatches() {
            this.clearBetweenBatches = true;
            return this;
        }

        public Builder newStructureSpawner(StructureSpawner $$0) {
            this.newStructureSpawner = $$0;
            return this;
        }

        public Builder existingStructureSpawner(StructureGridSpawner $$0) {
            this.existingStructureSpawner = $$0;
            return this;
        }

        public Builder batcher(GameTestBatcher $$0) {
            this.batcher = $$0;
            return this;
        }

        public GameTestRunner build() {
            return new GameTestRunner(this.batcher, this.batches, this.level, this.testTicker, this.existingStructureSpawner, this.newStructureSpawner, this.haltOnError, this.clearBetweenBatches);
        }
    }
}

