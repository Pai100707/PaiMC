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
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

public class PoiCompetitorScan {
    public static BehaviorControl<Villager> create() {
        return BehaviorBuilder.create($$0 -> $$0.group($$0.present(MemoryModuleType.JOB_SITE), $$0.present(MemoryModuleType.NEAREST_LIVING_ENTITIES)).apply((Applicative)$$0, ($$1, $$2) -> ($$3, $$42, $$5) -> {
            GlobalPos $$6 = (GlobalPos)$$0.get($$1);
            $$3.getPoiManager().getType($$6.pos()).ifPresent($$4 -> ((List)$$0.get($$2)).stream().filter($$1 -> $$1 instanceof Villager && $$1 != $$42).map($$0 -> (Villager)$$0).filter(LivingEntity::isAlive).filter($$2 -> PoiCompetitorScan.competesForSameJobsite($$6, $$4, $$2)).reduce((Villager)$$42, PoiCompetitorScan::selectWinner));
            return true;
        }));
    }

    private static Villager selectWinner(Villager $$0, Villager $$1) {
        Villager $$5;
        Villager $$4;
        if ($$0.getVillagerXp() > $$1.getVillagerXp()) {
            Villager $$2 = $$0;
            Villager $$3 = $$1;
        } else {
            $$4 = $$1;
            $$5 = $$0;
        }
        $$5.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
        return $$4;
    }

    private static boolean competesForSameJobsite(GlobalPos $$0, Holder<PoiType> $$1, Villager $$2) {
        Optional<GlobalPos> $$3 = $$2.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        return $$3.isPresent() && $$0.equals($$3.get()) && PoiCompetitorScan.hasMatchingProfession($$1, $$2.getVillagerData().profession());
    }

    private static boolean hasMatchingProfession(Holder<PoiType> $$0, Holder<VillagerProfession> $$1) {
        return $$1.value().heldJobSite().test($$0);
    }
}

