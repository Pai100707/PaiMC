/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFrom {
    public static BehaviorControl<PathfinderMob> pos(MemoryModuleType<BlockPos> $$0, float $$1, int $$2, boolean $$3) {
        return SetWalkTargetAwayFrom.create($$0, $$1, $$2, $$3, Vec3::atBottomCenterOf);
    }

    public static OneShot<PathfinderMob> entity(MemoryModuleType<? extends Entity> $$0, float $$1, int $$2, boolean $$3) {
        return SetWalkTargetAwayFrom.create($$0, $$1, $$2, $$3, Entity::position);
    }

    private static <T> OneShot<PathfinderMob> create(MemoryModuleType<T> $$0, float $$1, int $$2, boolean $$3, Function<T, Vec3> $$4) {
        return BehaviorBuilder.create($$52 -> $$52.group($$52.registered(MemoryModuleType.WALK_TARGET), $$52.present($$0)).apply((Applicative)$$52, ($$5, $$6) -> ($$7, $$8, $$9) -> {
            Vec3 $$14;
            Vec3 $$13;
            Vec3 $$12;
            Optional $$10 = $$52.tryGet($$5);
            if ($$10.isPresent() && !$$3) {
                return false;
            }
            Vec3 $$11 = $$8.position();
            if (!$$11.closerThan($$12 = (Vec3)$$4.apply($$52.get($$6)), $$2)) {
                return false;
            }
            if ($$10.isPresent() && ((WalkTarget)$$10.get()).getSpeedModifier() == $$1 && ($$13 = ((WalkTarget)$$10.get()).getTarget().currentPosition().subtract($$11)).dot($$14 = $$12.subtract($$11)) < 0.0) {
                return false;
            }
            for (int $$15 = 0; $$15 < 10; ++$$15) {
                Vec3 $$16 = LandRandomPos.getPosAway($$8, 16, 7, $$12);
                if ($$16 == null) continue;
                $$5.set(new WalkTarget($$16, $$1, 0));
                break;
            }
            return true;
        }));
    }
}

