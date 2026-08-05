/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MoveBackToVillageGoal
extends RandomStrollGoal {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    public MoveBackToVillageGoal(PathfinderMob $$0, double $$1, boolean $$2) {
        super($$0, $$1, 10, $$2);
    }

    @Override
    public boolean canUse() {
        BlockPos $$1;
        ServerLevel $$0 = (ServerLevel)this.mob.level();
        if ($$0.isVillage($$1 = this.mob.blockPosition())) {
            return false;
        }
        return super.canUse();
    }

    @Override
    protected @Nullable Vec3 getPosition() {
        BlockPos $$1;
        SectionPos $$2;
        ServerLevel $$0 = (ServerLevel)this.mob.level();
        SectionPos $$3 = BehaviorUtils.findSectionClosestToVillage($$0, $$2 = SectionPos.of($$1 = this.mob.blockPosition()), 2);
        if ($$3 != $$2) {
            return DefaultRandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf($$3.center()), 1.5707963705062866);
        }
        return null;
    }
}

