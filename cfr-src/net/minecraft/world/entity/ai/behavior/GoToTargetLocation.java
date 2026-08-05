/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GoToTargetLocation {
    private static BlockPos getNearbyPos(Mob $$0, BlockPos $$1) {
        RandomSource $$2 = $$0.level().random;
        return $$1.offset(GoToTargetLocation.getRandomOffset($$2), 0, GoToTargetLocation.getRandomOffset($$2));
    }

    private static int getRandomOffset(RandomSource $$0) {
        return $$0.nextInt(3) - 1;
    }

    public static <E extends Mob> OneShot<E> create(MemoryModuleType<BlockPos> $$0, int $$1, float $$2) {
        return BehaviorBuilder.create($$32 -> $$32.group($$32.present($$0), $$32.absent(MemoryModuleType.ATTACK_TARGET), $$32.absent(MemoryModuleType.WALK_TARGET), $$32.registered(MemoryModuleType.LOOK_TARGET)).apply((Applicative)$$32, ($$3, $$42, $$52, $$62) -> ($$4, $$5, $$6) -> {
            BlockPos $$7 = (BlockPos)$$32.get($$3);
            boolean $$8 = $$7.closerThan($$5.blockPosition(), $$1);
            if (!$$8) {
                BehaviorUtils.setWalkAndLookTargetMemories($$5, GoToTargetLocation.getNearbyPos($$5, $$7), $$2, $$1);
            }
            return true;
        }));
    }
}

