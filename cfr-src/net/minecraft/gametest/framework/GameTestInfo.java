/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.gametest.framework.GameTestTimeoutException;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.UnknownGameTestException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.LevelTicks;
import org.jspecify.annotations.Nullable;

public class GameTestInfo {
    private final Holder.Reference<GameTestInstance> test;
    private @Nullable BlockPos testBlockPos;
    private final ServerLevel level;
    private final Collection<GameTestListener> listeners = Lists.newArrayList();
    private final int timeoutTicks;
    private final Collection<GameTestSequence> sequences = Lists.newCopyOnWriteArrayList();
    private final Object2LongMap<Runnable> runAtTickTimeMap = new Object2LongOpenHashMap();
    private boolean placedStructure;
    private boolean chunksLoaded;
    private int tickCount;
    private boolean started;
    private final RetryOptions retryOptions;
    private final Stopwatch timer = Stopwatch.createUnstarted();
    private boolean done;
    private final Rotation extraRotation;
    private @Nullable GameTestException error;
    private @Nullable TestInstanceBlockEntity testInstanceBlockEntity;

    public GameTestInfo(Holder.Reference<GameTestInstance> $$0, Rotation $$1, ServerLevel $$2, RetryOptions $$3) {
        this.test = $$0;
        this.level = $$2;
        this.retryOptions = $$3;
        this.timeoutTicks = $$0.value().maxTicks();
        this.extraRotation = $$1;
    }

    public void setTestBlockPos(@Nullable BlockPos $$0) {
        this.testBlockPos = $$0;
    }

    public GameTestInfo startExecution(int $$0) {
        this.tickCount = -(this.test.value().setupTicks() + $$0 + 1);
        return this;
    }

    public void placeStructure() {
        if (this.placedStructure) {
            return;
        }
        TestInstanceBlockEntity $$02 = this.getTestInstanceBlockEntity();
        if (!$$02.placeStructure()) {
            this.fail(Component.translatable("test.error.structure.failure", $$02.getTestName().getString()));
        }
        this.placedStructure = true;
        $$02.encaseStructure();
        BoundingBox $$1 = $$02.getStructureBoundingBox();
        ((LevelTicks)this.level.getBlockTicks()).clearArea($$1);
        this.level.clearBlockEvents($$1);
        this.listeners.forEach($$0 -> $$0.testStructureLoaded(this));
    }

    public void tick(GameTestRunner $$0) {
        if (this.isDone()) {
            return;
        }
        if (!this.placedStructure) {
            this.fail(Component.translatable("test.error.ticking_without_structure"));
        }
        if (this.testInstanceBlockEntity == null) {
            this.fail(Component.translatable("test.error.missing_block_entity"));
        }
        if (this.error != null) {
            this.finish();
        }
        if (!this.chunksLoaded) {
            if (!this.testInstanceBlockEntity.getStructureBoundingBox().intersectingChunks().allMatch(this.level::areEntitiesActuallyLoadedAndTicking)) {
                return;
            }
        }
        this.chunksLoaded = true;
        this.tickInternal();
        if (this.isDone()) {
            if (this.error != null) {
                this.listeners.forEach($$1 -> $$1.testFailed(this, $$0));
            } else {
                this.listeners.forEach($$1 -> $$1.testPassed(this, $$0));
            }
        }
    }

    private void tickInternal() {
        ++this.tickCount;
        if (this.tickCount < 0) {
            return;
        }
        if (!this.started) {
            this.startTest();
        }
        ObjectIterator $$02 = this.runAtTickTimeMap.object2LongEntrySet().iterator();
        while ($$02.hasNext()) {
            Object2LongMap.Entry $$1 = (Object2LongMap.Entry)$$02.next();
            if ($$1.getLongValue() > (long)this.tickCount) continue;
            try {
                ((Runnable)$$1.getKey()).run();
            }
            catch (GameTestException $$2) {
                this.fail($$2);
            }
            catch (Exception $$3) {
                this.fail(new UnknownGameTestException($$3));
            }
            $$02.remove();
        }
        if (this.tickCount > this.timeoutTicks) {
            if (this.sequences.isEmpty()) {
                this.fail(new GameTestTimeoutException(Component.translatable("test.error.timeout.no_result", this.test.value().maxTicks())));
            } else {
                this.sequences.forEach($$0 -> $$0.tickAndFailIfNotComplete(this.tickCount));
                if (this.error == null) {
                    this.fail(new GameTestTimeoutException(Component.translatable("test.error.timeout.no_sequences_finished", this.test.value().maxTicks())));
                }
            }
        } else {
            this.sequences.forEach($$0 -> $$0.tickAndContinue(this.tickCount));
        }
    }

    private void startTest() {
        if (this.started) {
            return;
        }
        this.started = true;
        this.timer.start();
        this.getTestInstanceBlockEntity().setRunning();
        try {
            this.test.value().run(new GameTestHelper(this));
        }
        catch (GameTestException $$0) {
            this.fail($$0);
        }
        catch (Exception $$1) {
            this.fail(new UnknownGameTestException($$1));
        }
    }

    public void setRunAtTickTime(long $$0, Runnable $$1) {
        this.runAtTickTimeMap.put((Object)$$1, $$0);
    }

    public Identifier id() {
        return this.test.key().identifier();
    }

    public @Nullable BlockPos getTestBlockPos() {
        return this.testBlockPos;
    }

    public BlockPos getTestOrigin() {
        return this.testInstanceBlockEntity.getStartCorner();
    }

    public AABB getStructureBounds() {
        TestInstanceBlockEntity $$0 = this.getTestInstanceBlockEntity();
        return $$0.getStructureBounds();
    }

    public TestInstanceBlockEntity getTestInstanceBlockEntity() {
        if (this.testInstanceBlockEntity == null) {
            if (this.testBlockPos == null) {
                throw new IllegalStateException("This GameTestInfo has no position");
            }
            BlockEntity blockEntity = this.level.getBlockEntity(this.testBlockPos);
            if (blockEntity instanceof TestInstanceBlockEntity) {
                TestInstanceBlockEntity $$0;
                this.testInstanceBlockEntity = $$0 = (TestInstanceBlockEntity)blockEntity;
            }
            if (this.testInstanceBlockEntity == null) {
                throw new IllegalStateException("Could not find a test instance block entity at the given coordinate " + String.valueOf(this.testBlockPos));
            }
        }
        return this.testInstanceBlockEntity;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public boolean hasSucceeded() {
        return this.done && this.error == null;
    }

    public boolean hasFailed() {
        return this.error != null;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public boolean isDone() {
        return this.done;
    }

    public long getRunTime() {
        return this.timer.elapsed(TimeUnit.MILLISECONDS);
    }

    private void finish() {
        if (!this.done) {
            this.done = true;
            if (this.timer.isRunning()) {
                this.timer.stop();
            }
        }
    }

    public void succeed() {
        if (this.error == null) {
            this.finish();
            AABB $$02 = this.getStructureBounds();
            List<Entity> $$1 = this.getLevel().getEntitiesOfClass(Entity.class, $$02.inflate(1.0), $$0 -> !($$0 instanceof Player));
            $$1.forEach($$0 -> $$0.remove(Entity.RemovalReason.DISCARDED));
        }
    }

    public void fail(Component $$0) {
        this.fail(new GameTestAssertException($$0, this.tickCount));
    }

    public void fail(GameTestException $$0) {
        this.error = $$0;
    }

    public @Nullable GameTestException getError() {
        return this.error;
    }

    public String toString() {
        return this.id().toString();
    }

    public void addListener(GameTestListener $$0) {
        this.listeners.add($$0);
    }

    public @Nullable GameTestInfo prepareTestStructure() {
        TestInstanceBlockEntity $$0 = this.createTestInstanceBlock(Objects.requireNonNull(this.testBlockPos), this.extraRotation, this.level);
        if ($$0 != null) {
            this.testInstanceBlockEntity = $$0;
            this.placeStructure();
            return this;
        }
        return null;
    }

    private @Nullable TestInstanceBlockEntity createTestInstanceBlock(BlockPos $$0, Rotation $$1, ServerLevel $$2) {
        $$2.setBlockAndUpdate($$0, Blocks.TEST_INSTANCE_BLOCK.defaultBlockState());
        BlockEntity blockEntity = $$2.getBlockEntity($$0);
        if (blockEntity instanceof TestInstanceBlockEntity) {
            TestInstanceBlockEntity $$3 = (TestInstanceBlockEntity)blockEntity;
            ResourceKey<GameTestInstance> $$4 = this.getTestHolder().key();
            Vec3i $$5 = TestInstanceBlockEntity.getStructureSize($$2, $$4).orElse(new Vec3i(1, 1, 1));
            $$3.set(new TestInstanceBlockEntity.Data(Optional.of($$4), $$5, $$1, false, TestInstanceBlockEntity.Status.CLEARED, Optional.empty()));
            return $$3;
        }
        return null;
    }

    int getTick() {
        return this.tickCount;
    }

    GameTestSequence createSequence() {
        GameTestSequence $$0 = new GameTestSequence(this);
        this.sequences.add($$0);
        return $$0;
    }

    public boolean isRequired() {
        return this.test.value().required();
    }

    public boolean isOptional() {
        return !this.test.value().required();
    }

    public Identifier getStructure() {
        return this.test.value().structure();
    }

    public Rotation getRotation() {
        return this.test.value().info().rotation().getRotated(this.extraRotation);
    }

    public GameTestInstance getTest() {
        return this.test.value();
    }

    public Holder.Reference<GameTestInstance> getTestHolder() {
        return this.test;
    }

    public int getTimeoutTicks() {
        return this.timeoutTicks;
    }

    public boolean isFlaky() {
        return this.test.value().maxAttempts() > 1;
    }

    public int maxAttempts() {
        return this.test.value().maxAttempts();
    }

    public int requiredSuccesses() {
        return this.test.value().requiredSuccesses();
    }

    public RetryOptions retryOptions() {
        return this.retryOptions;
    }

    public Stream<GameTestListener> getListeners() {
        return this.listeners.stream();
    }

    public GameTestInfo copyReset() {
        GameTestInfo $$0 = new GameTestInfo(this.test, this.extraRotation, this.level, this.retryOptions());
        if (this.testBlockPos != null) {
            $$0.setTestBlockPos(this.testBlockPos);
        }
        return $$0;
    }
}

