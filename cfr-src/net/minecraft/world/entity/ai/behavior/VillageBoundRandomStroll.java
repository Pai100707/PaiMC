/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class VillageBoundRandomStroll {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    public static OneShot<PathfinderMob> create(float $$0) {
        return VillageBoundRandomStroll.create($$0, 10, 7);
    }

    public static OneShot<PathfinderMob> create(float $$0, int $$1, int $$2) {
        return BehaviorBuilder.create($$32 -> $$32.group($$32.absent(MemoryModuleType.WALK_TARGET)).apply((Applicative)$$32, $$3 -> ($$4, $$5, $$6) -> {
            Vec3 $$122;
            BlockPos $$7 = $$5.blockPosition();
            if ($$4.isVillage($$7)) {
                Vec3 $$8 = LandRandomPos.getPos($$5, $$1, $$2);
            } else {
                SectionPos $$9 = SectionPos.of($$7);
                SectionPos $$10 = BehaviorUtils.findSectionClosestToVillage($$4, $$9, 2);
                if ($$10 != $$9) {
                    Vec3 $$11 = DefaultRandomPos.getPosTowards($$5, $$1, $$2, Vec3.atBottomCenterOf($$10.center()), 1.5707963705062866);
                } else {
                    $$122 = LandRandomPos.getPos($$5, $$1, $$2);
                }
            }
            $$3.setOrErase(Optional.ofNullable($$122).map($$1 -> new WalkTarget((Vec3)$$1, $$0, 0)));
            return true;
        }));
    }
}

