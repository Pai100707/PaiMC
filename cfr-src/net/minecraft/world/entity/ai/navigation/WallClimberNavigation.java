/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jspecify.annotations.Nullable;

public class WallClimberNavigation
extends GroundPathNavigation {
    private @Nullable BlockPos pathToPosition;

    public WallClimberNavigation(Mob $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public Path createPath(BlockPos $$0, int $$1) {
        this.pathToPosition = $$0;
        return super.createPath($$0, $$1);
    }

    @Override
    public Path createPath(Entity $$0, int $$1) {
        this.pathToPosition = $$0.blockPosition();
        return super.createPath($$0, $$1);
    }

    @Override
    public boolean moveTo(Entity $$0, double $$1) {
        Path $$2 = this.createPath($$0, 0);
        if ($$2 != null) {
            return this.moveTo($$2, $$1);
        }
        this.pathToPosition = $$0.blockPosition();
        this.speedModifier = $$1;
        return true;
    }

    @Override
    public void tick() {
        if (this.isDone()) {
            if (this.pathToPosition != null) {
                if (this.pathToPosition.closerToCenterThan(this.mob.position(), this.mob.getBbWidth()) || this.mob.getY() > (double)this.pathToPosition.getY() && BlockPos.containing(this.pathToPosition.getX(), this.mob.getY(), this.pathToPosition.getZ()).closerToCenterThan(this.mob.position(), this.mob.getBbWidth())) {
                    this.pathToPosition = null;
                } else {
                    this.mob.getMoveControl().setWantedPosition(this.pathToPosition.getX(), this.pathToPosition.getY(), this.pathToPosition.getZ(), this.speedModifier);
                }
            }
            return;
        }
        super.tick();
    }
}

