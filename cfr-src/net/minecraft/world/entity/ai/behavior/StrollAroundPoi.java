/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.apache.commons.lang3.mutable.MutableLong
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollAroundPoi {
    private static final int MIN_TIME_BETWEEN_STROLLS = 180;
    private static final int STROLL_MAX_XZ_DIST = 8;
    private static final int STROLL_MAX_Y_DIST = 6;

    public static OneShot<PathfinderMob> create(MemoryModuleType<GlobalPos> $$0, float $$1, int $$2) {
        MutableLong $$3 = new MutableLong(0L);
        return BehaviorBuilder.create($$42 -> $$42.group($$42.registered(MemoryModuleType.WALK_TARGET), $$42.present($$0)).apply((Applicative)$$42, ($$4, $$5) -> ($$6, $$7, $$8) -> {
            GlobalPos $$9 = (GlobalPos)$$42.get($$5);
            if ($$6.dimension() != $$9.dimension() || !$$9.pos().closerToCenterThan($$7.position(), $$2)) {
                return false;
            }
            if ($$8 <= $$3.longValue()) {
                return true;
            }
            Optional<Vec3> $$10 = Optional.ofNullable(LandRandomPos.getPos($$7, 8, 6));
            $$4.setOrErase($$10.map($$1 -> new WalkTarget((Vec3)$$1, $$1, 1)));
            $$3.setValue($$8 + 180L);
            return true;
        }));
    }
}

