/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.navigation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.ServerDebugSubscribers;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class PathNavigation {
    private static final int MAX_TIME_RECOMPUTE = 20;
    private static final int STUCK_CHECK_INTERVAL = 100;
    private static final float STUCK_THRESHOLD_DISTANCE_FACTOR = 0.25f;
    protected final Mob mob;
    protected final Level level;
    protected @Nullable Path path;
    protected double speedModifier;
    protected int tick;
    protected int lastStuckCheck;
    protected Vec3 lastStuckCheckPos = Vec3.ZERO;
    protected Vec3i timeoutCachedNode = Vec3i.ZERO;
    protected long timeoutTimer;
    protected long lastTimeoutCheck;
    protected double timeoutLimit;
    protected float maxDistanceToWaypoint = 0.5f;
    protected boolean hasDelayedRecomputation;
    protected long timeLastRecompute;
    protected NodeEvaluator nodeEvaluator;
    private @Nullable BlockPos targetPos;
    private int reachRange;
    private float maxVisitedNodesMultiplier = 1.0f;
    private final PathFinder pathFinder;
    private boolean isStuck;
    private float requiredPathLength = 16.0f;

    public PathNavigation(Mob $$0, Level $$1) {
        this.mob = $$0;
        this.level = $$1;
        this.pathFinder = this.createPathFinder(Mth.floor($$0.getAttributeBaseValue(Attributes.FOLLOW_RANGE) * 16.0));
        if ($$1 instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)$$1;
            ServerDebugSubscribers $$3 = $$2.getServer().debugSubscribers();
            this.pathFinder.setCaptureDebug(() -> $$3.hasAnySubscriberFor(DebugSubscriptions.ENTITY_PATHS));
        }
    }

    public void updatePathfinderMaxVisitedNodes() {
        int $$0 = Mth.floor(this.getMaxPathLength() * 16.0f);
        this.pathFinder.setMaxVisitedNodes($$0);
    }

    public void setRequiredPathLength(float $$0) {
        this.requiredPathLength = $$0;
        this.updatePathfinderMaxVisitedNodes();
    }

    private float getMaxPathLength() {
        return Math.max((float)this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), this.requiredPathLength);
    }

    public void resetMaxVisitedNodesMultiplier() {
        this.maxVisitedNodesMultiplier = 1.0f;
    }

    public void setMaxVisitedNodesMultiplier(float $$0) {
        this.maxVisitedNodesMultiplier = $$0;
    }

    public @Nullable BlockPos getTargetPos() {
        return this.targetPos;
    }

    protected abstract PathFinder createPathFinder(int var1);

    public void setSpeedModifier(double $$0) {
        this.speedModifier = $$0;
    }

    public void recomputePath() {
        if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
            if (this.targetPos != null) {
                this.path = null;
                this.path = this.createPath(this.targetPos, this.reachRange);
                this.timeLastRecompute = this.level.getGameTime();
                this.hasDelayedRecomputation = false;
            }
        } else {
            this.hasDelayedRecomputation = true;
        }
    }

    public final @Nullable Path createPath(double $$0, double $$1, double $$2, int $$3) {
        return this.createPath(BlockPos.containing($$0, $$1, $$2), $$3);
    }

    public @Nullable Path createPath(Stream<BlockPos> $$0, int $$1) {
        return this.createPath($$0.collect(Collectors.toSet()), 8, false, $$1);
    }

    public @Nullable Path createPath(Set<BlockPos> $$0, int $$1) {
        return this.createPath($$0, 8, false, $$1);
    }

    public @Nullable Path createPath(BlockPos $$0, int $$1) {
        return this.createPath((Set<BlockPos>)ImmutableSet.of((Object)$$0), 8, false, $$1);
    }

    public @Nullable Path createPath(BlockPos $$0, int $$1, int $$2) {
        return this.createPath((Set<BlockPos>)ImmutableSet.of((Object)$$0), 8, false, $$1, $$2);
    }

    public @Nullable Path createPath(Entity $$0, int $$1) {
        return this.createPath((Set<BlockPos>)ImmutableSet.of((Object)$$0.blockPosition()), 16, true, $$1);
    }

    protected @Nullable Path createPath(Set<BlockPos> $$0, int $$1, boolean $$2, int $$3) {
        return this.createPath($$0, $$1, $$2, $$3, this.getMaxPathLength());
    }

    protected @Nullable Path createPath(Set<BlockPos> $$0, int $$1, boolean $$2, int $$3, float $$4) {
        if ($$0.isEmpty()) {
            return null;
        }
        if (this.mob.getY() < (double)this.level.getMinY()) {
            return null;
        }
        if (!this.canUpdatePath()) {
            return null;
        }
        if (this.path != null && !this.path.isDone() && $$0.contains(this.targetPos)) {
            return this.path;
        }
        ProfilerFiller $$5 = Profiler.get();
        $$5.push("pathfind");
        BlockPos $$6 = $$2 ? this.mob.blockPosition().above() : this.mob.blockPosition();
        int $$7 = (int)($$4 + (float)$$1);
        PathNavigationRegion $$8 = new PathNavigationRegion(this.level, $$6.offset(-$$7, -$$7, -$$7), $$6.offset($$7, $$7, $$7));
        Path $$9 = this.pathFinder.findPath($$8, this.mob, $$0, $$4, $$3, this.maxVisitedNodesMultiplier);
        $$5.pop();
        if ($$9 != null && $$9.getTarget() != null) {
            this.targetPos = $$9.getTarget();
            this.reachRange = $$3;
            this.resetStuckTimeout();
        }
        return $$9;
    }

    public boolean moveTo(double $$0, double $$1, double $$2, double $$3) {
        return this.moveTo(this.createPath($$0, $$1, $$2, 1), $$3);
    }

    public boolean moveTo(double $$0, double $$1, double $$2, int $$3, double $$4) {
        return this.moveTo(this.createPath($$0, $$1, $$2, $$3), $$4);
    }

    public boolean moveTo(Entity $$0, double $$1) {
        Path $$2 = this.createPath($$0, 1);
        return $$2 != null && this.moveTo($$2, $$1);
    }

    public boolean moveTo(@Nullable Path $$0, double $$1) {
        if ($$0 == null) {
            this.path = null;
            return false;
        }
        if (!$$0.sameAs(this.path)) {
            this.path = $$0;
        }
        if (this.isDone()) {
            return false;
        }
        this.trimPath();
        if (this.path.getNodeCount() <= 0) {
            return false;
        }
        this.speedModifier = $$1;
        Vec3 $$2 = this.getTempMobPos();
        this.lastStuckCheck = this.tick;
        this.lastStuckCheckPos = $$2;
        return true;
    }

    public @Nullable Path getPath() {
        return this.path;
    }

    public void tick() {
        ++this.tick;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }
        if (this.isDone()) {
            return;
        }
        if (this.canUpdatePath()) {
            this.followThePath();
        } else if (this.path != null && !this.path.isDone()) {
            Vec3 $$0 = this.getTempMobPos();
            Vec3 $$1 = this.path.getNextEntityPos(this.mob);
            if ($$0.y > $$1.y && !this.mob.onGround() && Mth.floor($$0.x) == Mth.floor($$1.x) && Mth.floor($$0.z) == Mth.floor($$1.z)) {
                this.path.advance();
            }
        }
        if (this.isDone()) {
            return;
        }
        Vec3 $$2 = this.path.getNextEntityPos(this.mob);
        this.mob.getMoveControl().setWantedPosition($$2.x, this.getGroundY($$2), $$2.z, this.speedModifier);
    }

    protected double getGroundY(Vec3 $$0) {
        BlockPos $$1 = BlockPos.containing($$0);
        return this.level.getBlockState($$1.below()).isAir() ? $$0.y : WalkNodeEvaluator.getFloorLevel(this.level, $$1);
    }

    protected void followThePath() {
        boolean $$5;
        Vec3 $$0 = this.getTempMobPos();
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75f ? this.mob.getBbWidth() / 2.0f : 0.75f - this.mob.getBbWidth() / 2.0f;
        BlockPos $$1 = this.path.getNextNodePos();
        double $$2 = Math.abs(this.mob.getX() - ((double)$$1.getX() + 0.5));
        double $$3 = Math.abs(this.mob.getY() - (double)$$1.getY());
        double $$4 = Math.abs(this.mob.getZ() - ((double)$$1.getZ() + 0.5));
        boolean bl = $$5 = $$2 < (double)this.maxDistanceToWaypoint && $$4 < (double)this.maxDistanceToWaypoint && $$3 < 1.0;
        if ($$5 || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection($$0)) {
            this.path.advance();
        }
        this.doStuckDetection($$0);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 $$0) {
        boolean $$8;
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        }
        Vec3 $$1 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
        if (!$$0.closerThan($$1, 2.0)) {
            return false;
        }
        if (this.canMoveDirectly($$0, this.path.getNextEntityPos(this.mob))) {
            return true;
        }
        Vec3 $$2 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
        Vec3 $$3 = $$1.subtract($$0);
        Vec3 $$4 = $$2.subtract($$0);
        double $$5 = $$3.lengthSqr();
        double $$6 = $$4.lengthSqr();
        boolean $$7 = $$6 < $$5;
        boolean bl = $$8 = $$5 < 0.5;
        if ($$7 || $$8) {
            Vec3 $$9 = $$3.normalize();
            Vec3 $$10 = $$4.normalize();
            return $$10.dot($$9) < 0.0;
        }
        return false;
    }

    protected void doStuckDetection(Vec3 $$0) {
        if (this.tick - this.lastStuckCheck > 100) {
            float $$1 = this.mob.getSpeed() >= 1.0f ? this.mob.getSpeed() : this.mob.getSpeed() * this.mob.getSpeed();
            float $$2 = $$1 * 100.0f * 0.25f;
            if ($$0.distanceToSqr(this.lastStuckCheckPos) < (double)($$2 * $$2)) {
                this.isStuck = true;
                this.stop();
            } else {
                this.isStuck = false;
            }
            this.lastStuckCheck = this.tick;
            this.lastStuckCheckPos = $$0;
        }
        if (this.path != null && !this.path.isDone()) {
            BlockPos $$3 = this.path.getNextNodePos();
            long $$4 = this.level.getGameTime();
            if ($$3.equals(this.timeoutCachedNode)) {
                this.timeoutTimer += $$4 - this.lastTimeoutCheck;
            } else {
                this.timeoutCachedNode = $$3;
                double $$5 = $$0.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode));
                double d = this.timeoutLimit = this.mob.getSpeed() > 0.0f ? $$5 / (double)this.mob.getSpeed() * 20.0 : 0.0;
            }
            if (this.timeoutLimit > 0.0 && (double)this.timeoutTimer > this.timeoutLimit * 3.0) {
                this.timeoutPath();
            }
            this.lastTimeoutCheck = $$4;
        }
    }

    private void timeoutPath() {
        this.resetStuckTimeout();
        this.stop();
    }

    private void resetStuckTimeout() {
        this.timeoutCachedNode = Vec3i.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0;
        this.isStuck = false;
    }

    public boolean isDone() {
        return this.path == null || this.path.isDone();
    }

    public boolean isInProgress() {
        return !this.isDone();
    }

    public void stop() {
        this.path = null;
    }

    protected abstract Vec3 getTempMobPos();

    protected abstract boolean canUpdatePath();

    protected void trimPath() {
        if (this.path == null) {
            return;
        }
        for (int $$0 = 0; $$0 < this.path.getNodeCount(); ++$$0) {
            Node $$1 = this.path.getNode($$0);
            Node $$2 = $$0 + 1 < this.path.getNodeCount() ? this.path.getNode($$0 + 1) : null;
            BlockState $$3 = this.level.getBlockState(new BlockPos($$1.x, $$1.y, $$1.z));
            if (!$$3.is(BlockTags.CAULDRONS)) continue;
            this.path.replaceNode($$0, $$1.cloneAndMove($$1.x, $$1.y + 1, $$1.z));
            if ($$2 == null || $$1.y < $$2.y) continue;
            this.path.replaceNode($$0 + 1, $$1.cloneAndMove($$2.x, $$1.y + 1, $$2.z));
        }
    }

    protected boolean canMoveDirectly(Vec3 $$0, Vec3 $$1) {
        return false;
    }

    public boolean canCutCorner(PathType $$0) {
        return $$0 != PathType.DANGER_FIRE && $$0 != PathType.DANGER_OTHER && $$0 != PathType.WALKABLE_DOOR;
    }

    protected static boolean isClearForMovementBetween(Mob $$0, Vec3 $$1, Vec3 $$2, boolean $$3) {
        Vec3 $$4 = new Vec3($$2.x, $$2.y + (double)$$0.getBbHeight() * 0.5, $$2.z);
        return $$0.level().clip(new ClipContext($$1, $$4, ClipContext.Block.COLLIDER, $$3 ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, $$0)).getType() == HitResult.Type.MISS;
    }

    public boolean isStableDestination(BlockPos $$0) {
        BlockPos $$1 = $$0.below();
        return this.level.getBlockState($$1).isSolidRender();
    }

    public NodeEvaluator getNodeEvaluator() {
        return this.nodeEvaluator;
    }

    public void setCanFloat(boolean $$0) {
        this.nodeEvaluator.setCanFloat($$0);
    }

    public boolean canFloat() {
        return this.nodeEvaluator.canFloat();
    }

    public boolean shouldRecomputePath(BlockPos $$0) {
        if (this.hasDelayedRecomputation) {
            return false;
        }
        if (this.path == null || this.path.isDone() || this.path.getNodeCount() == 0) {
            return false;
        }
        Node $$1 = this.path.getEndNode();
        Vec3 $$2 = new Vec3(((double)$$1.x + this.mob.getX()) / 2.0, ((double)$$1.y + this.mob.getY()) / 2.0, ((double)$$1.z + this.mob.getZ()) / 2.0);
        return $$0.closerToCenterThan($$2, this.path.getNodeCount() - this.path.getNextNodeIndex());
    }

    public float getMaxDistanceToWaypoint() {
        return this.maxDistanceToWaypoint;
    }

    public boolean isStuck() {
        return this.isStuck;
    }

    public abstract boolean canNavigateGround();

    public void setCanOpenDoors(boolean $$0) {
        this.nodeEvaluator.setCanOpenDoors($$0);
    }
}

