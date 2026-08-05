/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class InteractGoal
extends LookAtPlayerGoal {
    public InteractGoal(Mob $$0, Class<? extends LivingEntity> $$1, float $$2) {
        super($$0, $$1, $$2);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }

    public InteractGoal(Mob $$0, Class<? extends LivingEntity> $$1, float $$2, float $$3) {
        super($$0, $$1, $$2, $$3);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }
}

