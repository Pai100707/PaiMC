/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MoveToSkySeeingSpot {
    public static OneShot<LivingEntity> create(float $$0) {
        return BehaviorBuilder.create($$12 -> $$12.group($$12.absent(MemoryModuleType.WALK_TARGET)).apply((Applicative)$$12, $$1 -> ($$22, $$3, $$4) -> {
            if ($$22.canSeeSky($$3.blockPosition())) {
                return false;
            }
            Optional<Vec3> $$5 = Optional.ofNullable(MoveToSkySeeingSpot.getOutdoorPosition($$22, $$3));
            $$5.ifPresent($$2 -> $$1.set(new WalkTarget((Vec3)$$2, $$0, 0)));
            return true;
        }));
    }

    private static @Nullable Vec3 getOutdoorPosition(ServerLevel $$0, LivingEntity $$1) {
        RandomSource $$2 = $$1.getRandom();
        BlockPos $$3 = $$1.blockPosition();
        for (int $$4 = 0; $$4 < 10; ++$$4) {
            BlockPos $$5 = $$3.offset($$2.nextInt(20) - 10, $$2.nextInt(6) - 3, $$2.nextInt(20) - 10);
            if (!MoveToSkySeeingSpot.hasNoBlocksAbove($$0, $$1, $$5)) continue;
            return Vec3.atBottomCenterOf($$5);
        }
        return null;
    }

    public static boolean hasNoBlocksAbove(ServerLevel $$0, LivingEntity $$1, BlockPos $$2) {
        return $$0.canSeeSky($$2) && (double)$$0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, $$2).getY() <= $$1.getY();
    }
}

