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
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindWater {
    public static BehaviorControl<PathfinderMob> create(int $$0, float $$1) {
        MutableLong $$2 = new MutableLong(0L);
        return BehaviorBuilder.create($$32 -> $$32.group($$32.absent(MemoryModuleType.ATTACK_TARGET), $$32.absent(MemoryModuleType.WALK_TARGET), $$32.registered(MemoryModuleType.LOOK_TARGET)).apply((Applicative)$$32, ($$3, $$4, $$52) -> ($$5, $$6, $$7) -> {
            if ($$5.getFluidState($$6.blockPosition()).is(FluidTags.WATER)) {
                return false;
            }
            if ($$7 < $$2.longValue()) {
                $$2.setValue($$7 + 20L + 2L);
                return true;
            }
            BlockPos $$8 = null;
            BlockPos $$9 = null;
            BlockPos $$10 = $$6.blockPosition();
            Iterable<BlockPos> $$11 = BlockPos.withinManhattan($$10, $$0, $$0, $$0);
            for (BlockPos $$12 : $$11) {
                if ($$12.getX() == $$10.getX() && $$12.getZ() == $$10.getZ()) continue;
                BlockState $$13 = $$6.level().getBlockState($$12.above());
                BlockState $$14 = $$6.level().getBlockState($$12);
                if (!$$14.is(Blocks.WATER)) continue;
                if ($$13.isAir()) {
                    $$8 = $$12.immutable();
                    break;
                }
                if ($$9 != null || $$12.closerToCenterThan($$6.position(), 1.5)) continue;
                $$9 = $$12.immutable();
            }
            if ($$8 == null) {
                $$8 = $$9;
            }
            if ($$8 != null) {
                $$52.set(new BlockPosTracker($$8));
                $$4.set(new WalkTarget(new BlockPosTracker($$8), $$1, 0));
            }
            $$2.setValue($$7 + 40L);
            return true;
        }));
    }
}

