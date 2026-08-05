/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public class LocateHidingPlace {
    public static OneShot<LivingEntity> create(int $$0, float $$1, int $$2) {
        return BehaviorBuilder.create($$3 -> $$3.group($$3.absent(MemoryModuleType.WALK_TARGET), $$3.registered(MemoryModuleType.HOME), $$3.registered(MemoryModuleType.HIDING_PLACE), $$3.registered(MemoryModuleType.PATH), $$3.registered(MemoryModuleType.LOOK_TARGET), $$3.registered(MemoryModuleType.BREED_TARGET), $$3.registered(MemoryModuleType.INTERACTION_TARGET)).apply((Applicative)$$3, ($$4, $$5, $$6, $$7, $$8, $$9, $$10) -> ($$11, $$12, $$13) -> {
            $$11.getPoiManager().find($$0 -> $$0.is(PoiTypes.HOME), $$0 -> true, $$12.blockPosition(), $$2 + 1, PoiManager.Occupancy.ANY).filter($$2 -> $$2.closerToCenterThan($$12.position(), $$2)).or(() -> $$11.getPoiManager().getRandom($$0 -> $$0.is(PoiTypes.HOME), $$0 -> true, PoiManager.Occupancy.ANY, $$12.blockPosition(), $$0, $$12.getRandom())).or(() -> $$3.tryGet($$5).map(GlobalPos::pos)).ifPresent($$10 -> {
                $$7.erase();
                $$8.erase();
                $$9.erase();
                $$10.erase();
                $$6.set(GlobalPos.of($$11.dimension(), $$10));
                if (!$$10.closerToCenterThan($$12.position(), $$2)) {
                    $$4.set(new WalkTarget((BlockPos)$$10, $$1, $$2));
                }
            });
            return true;
        }));
    }
}

