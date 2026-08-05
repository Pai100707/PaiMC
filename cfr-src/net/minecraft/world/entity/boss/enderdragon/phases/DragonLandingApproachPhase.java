/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DragonLandingApproachPhase
extends AbstractDragonPhaseInstance {
    private static final TargetingConditions NEAR_EGG_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
    private @Nullable Path currentPath;
    private @Nullable Vec3 targetLocation;

    public DragonLandingApproachPhase(EnderDragon $$0) {
        super($$0);
    }

    public EnderDragonPhase<DragonLandingApproachPhase> getPhase() {
        return EnderDragonPhase.LANDING_APPROACH;
    }

    @Override
    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Override
    public void doServerTick(ServerLevel $$0) {
        double $$1;
        double d = $$1 = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if ($$1 < 100.0 || $$1 > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            this.findNewTarget($$0);
        }
    }

    @Override
    public @Nullable Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget(ServerLevel $$0) {
        if (this.currentPath == null || this.currentPath.isDone()) {
            int $$6;
            int $$1 = this.dragon.findClosestNode();
            BlockPos $$2 = $$0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.dragon.getFightOrigin()));
            Player $$3 = $$0.getNearestPlayer(NEAR_EGG_TARGETING, this.dragon, (double)$$2.getX(), (double)$$2.getY(), $$2.getZ());
            if ($$3 != null) {
                Vec3 $$4 = new Vec3($$3.getX(), 0.0, $$3.getZ()).normalize();
                int $$5 = this.dragon.findClosestNode(-$$4.x * 40.0, 105.0, -$$4.z * 40.0);
            } else {
                $$6 = this.dragon.findClosestNode(40.0, $$2.getY(), 0.0);
            }
            Node $$7 = new Node($$2.getX(), $$2.getY(), $$2.getZ());
            this.currentPath = this.dragon.findPath($$1, $$6, $$7);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }
        this.navigateToNextPathNode();
        if (this.currentPath != null && this.currentPath.isDone()) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING);
        }
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            double $$3;
            BlockPos $$0 = this.currentPath.getNextNodePos();
            this.currentPath.advance();
            double $$1 = $$0.getX();
            double $$2 = $$0.getZ();
            while (($$3 = (double)((float)$$0.getY() + this.dragon.getRandom().nextFloat() * 20.0f)) < (double)$$0.getY()) {
            }
            this.targetLocation = new Vec3($$1, $$3, $$2);
        }
    }
}

