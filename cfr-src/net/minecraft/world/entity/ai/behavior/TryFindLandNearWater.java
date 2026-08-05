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
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLandNearWater {
    public static BehaviorControl<PathfinderMob> create(int $$0, float $$1) {
        MutableLong $$2 = new MutableLong(0L);
        return BehaviorBuilder.create($$32 -> $$32.group($$32.absent(MemoryModuleType.ATTACK_TARGET), $$32.absent(MemoryModuleType.WALK_TARGET), $$32.registered(MemoryModuleType.LOOK_TARGET)).apply((Applicative)$$32, ($$3, $$4, $$52) -> ($$5, $$6, $$7) -> {
            if ($$5.getFluidState($$6.blockPosition()).is(FluidTags.WATER)) {
                return false;
            }
            if ($$7 < $$2.longValue()) {
                $$2.setValue($$7 + 40L);
                return true;
            }
            CollisionContext $$8 = CollisionContext.of($$6);
            BlockPos $$9 = $$6.blockPosition();
            BlockPos.MutableBlockPos $$10 = new BlockPos.MutableBlockPos();
            block0: for (BlockPos $$11 : BlockPos.withinManhattan($$9, $$0, $$0, $$0)) {
                if ($$11.getX() == $$9.getX() && $$11.getZ() == $$9.getZ() || !$$5.getBlockState($$11).getCollisionShape($$5, $$11, $$8).isEmpty() || $$5.getBlockState($$10.setWithOffset((Vec3i)$$11, Direction.DOWN)).getCollisionShape($$5, $$11, $$8).isEmpty()) continue;
                for (Direction $$12 : Direction.Plane.HORIZONTAL) {
                    $$10.setWithOffset((Vec3i)$$11, $$12);
                    if (!$$5.getBlockState($$10).isAir() || !$$5.getBlockState($$10.move(Direction.DOWN)).is(Blocks.WATER)) continue;
                    $$52.set(new BlockPosTracker($$11));
                    $$4.set(new WalkTarget(new BlockPosTracker($$11), $$1, 0));
                    break block0;
                }
            }
            $$2.setValue($$7 + 40L);
            return true;
        }));
    }
}

