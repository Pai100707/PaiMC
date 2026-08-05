/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.pathfinder.Path;

public class YieldJobSite {
    public static BehaviorControl<Villager> create(float $$0) {
        return BehaviorBuilder.create($$1 -> $$1.group($$1.present(MemoryModuleType.POTENTIAL_JOB_SITE), $$1.absent(MemoryModuleType.JOB_SITE), $$1.present(MemoryModuleType.NEAREST_LIVING_ENTITIES), $$1.registered(MemoryModuleType.WALK_TARGET), $$1.registered(MemoryModuleType.LOOK_TARGET)).apply((Applicative)$$1, ($$2, $$3, $$4, $$5, $$6) -> ($$62, $$7, $$8) -> {
            if ($$7.isBaby()) {
                return false;
            }
            if (!$$7.getVillagerData().profession().is(VillagerProfession.NONE)) {
                return false;
            }
            BlockPos $$9 = ((GlobalPos)$$1.get($$2)).pos();
            Optional<Holder<PoiType>> $$10 = $$62.getPoiManager().getType($$9);
            if ($$10.isEmpty()) {
                return true;
            }
            ((List)$$1.get($$4)).stream().filter($$1 -> $$1 instanceof Villager && $$1 != $$7).map($$0 -> (Villager)$$0).filter(LivingEntity::isAlive).filter($$2 -> YieldJobSite.nearbyWantsJobsite((Holder)$$10.get(), $$2, $$9)).findFirst().ifPresent($$6 -> {
                $$5.erase();
                $$6.erase();
                $$2.erase();
                if ($$6.getBrain().getMemory(MemoryModuleType.JOB_SITE).isEmpty()) {
                    BehaviorUtils.setWalkAndLookTargetMemories((LivingEntity)$$6, $$9, $$0, 1);
                    $$6.getBrain().setMemory(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.of($$62.dimension(), $$9));
                    $$62.debugSynchronizers().updatePoi($$9);
                }
            });
            return true;
        }));
    }

    private static boolean nearbyWantsJobsite(Holder<PoiType> $$0, Villager $$1, BlockPos $$2) {
        boolean $$3 = $$1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
        if ($$3) {
            return false;
        }
        Optional<GlobalPos> $$4 = $$1.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        Holder<VillagerProfession> $$5 = $$1.getVillagerData().profession();
        if ($$5.value().heldJobSite().test($$0)) {
            if ($$4.isEmpty()) {
                return YieldJobSite.canReachPos($$1, $$2, $$0.value());
            }
            return $$4.get().pos().equals($$2);
        }
        return false;
    }

    private static boolean canReachPos(PathfinderMob $$0, BlockPos $$1, PoiType $$2) {
        Path $$3 = $$0.getNavigation().createPath($$1, $$2.validRange());
        return $$3 != null && $$3.canReach();
    }
}

