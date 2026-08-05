/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.apache.commons.lang3.mutable.MutableLong
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLand {
    private static final int COOLDOWN_TICKS = 60;

    public static BehaviorControl<PathfinderMob> create(int $$0, float $$1) {
        MutableLong $$2 = new MutableLong(0L);
        return BehaviorBuilder.create($$32 -> $$32.group($$32.absent(MemoryModuleType.ATTACK_TARGET), $$32.absent(MemoryModuleType.WALK_TARGET), $$32.registered(MemoryModuleType.LOOK_TARGET)).apply((Applicative)$$32, ($$3, $$4, $$52) -> ($$5, $$6, $$7) -> {
            if (!$$5.getFluidState($$6.blockPosition()).is(FluidTags.WATER)) {
                return false;
            }
            if ($$7 < $$2.longValue()) {
                $$2.setValue($$7 + 60L);
                return true;
            }
            BlockPos $$8 = $$6.blockPosition();
            BlockPos.MutableBlockPos $$9 = new BlockPos.MutableBlockPos();
            CollisionContext $$10 = CollisionContext.of($$6);
            for (BlockPos $$11 : BlockPos.withinManhattan($$8, $$0, $$0, $$0)) {
                if ($$11.getX() == $$8.getX() && $$11.getZ() == $$8.getZ()) continue;
                BlockState $$12 = $$5.getBlockState($$11);
                BlockState $$13 = $$5.getBlockState($$9.setWithOffset((Vec3i)$$11, Direction.DOWN));
                if ($$12.is(Blocks.WATER) || !$$5.getFluidState($$11).isEmpty() || !$$12.getCollisionShape($$5, $$11, $$10).isEmpty() || !$$13.isFaceSturdy($$5, $$9, Direction.UP)) continue;
                BlockPos $$14 = $$11.immutable();
                $$52.set(new BlockPosTracker($$14));
                $$4.set(new WalkTarget(new BlockPosTracker($$14), $$1, 1));
                break;
            }
            $$2.setValue($$7 + 60L);
            return true;
        }));
    }
}

