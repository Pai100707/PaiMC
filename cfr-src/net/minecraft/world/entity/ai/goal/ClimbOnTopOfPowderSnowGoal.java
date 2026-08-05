/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;

public class ClimbOnTopOfPowderSnowGoal
extends Goal {
    private final Mob mob;
    private final Level level;

    public ClimbOnTopOfPowderSnowGoal(Mob $$0, Level $$1) {
        this.mob = $$0;
        this.level = $$1;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        boolean $$0;
        boolean bl = $$0 = this.mob.wasInPowderSnow || this.mob.isInPowderSnow;
        if (!$$0 || !this.mob.getType().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
            return false;
        }
        BlockPos $$1 = this.mob.blockPosition().above();
        BlockState $$2 = this.level.getBlockState($$1);
        return $$2.is(Blocks.POWDER_SNOW) || $$2.getCollisionShape(this.level, $$1) == Shapes.empty();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.mob.getJumpControl().jump();
    }
}

