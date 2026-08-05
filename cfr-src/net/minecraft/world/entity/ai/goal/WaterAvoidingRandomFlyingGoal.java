/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WaterAvoidingRandomFlyingGoal
extends WaterAvoidingRandomStrollGoal {
    public WaterAvoidingRandomFlyingGoal(PathfinderMob $$0, double $$1) {
        super($$0, $$1);
    }

    @Override
    protected @Nullable Vec3 getPosition() {
        Vec3 $$0 = this.mob.getViewVector(0.0f);
        int $$1 = 8;
        Vec3 $$2 = HoverRandomPos.getPos(this.mob, 8, 7, $$0.x, $$0.z, 1.5707964f, 3, 1);
        if ($$2 != null) {
            return $$2;
        }
        return AirAndWaterRandomPos.getPos(this.mob, 8, 4, -2, $$0.x, $$0.z, 1.5707963705062866);
    }
}

