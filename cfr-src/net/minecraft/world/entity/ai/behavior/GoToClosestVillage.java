/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.Vec3;

public class GoToClosestVillage {
    public static BehaviorControl<Villager> create(float $$0, int $$1) {
        return BehaviorBuilder.create($$22 -> $$22.group($$22.absent(MemoryModuleType.WALK_TARGET)).apply((Applicative)$$22, $$2 -> ($$3, $$4, $$5) -> {
            if ($$3.isVillage($$4.blockPosition())) {
                return false;
            }
            PoiManager $$6 = $$3.getPoiManager();
            int $$7 = $$6.sectionsToVillage(SectionPos.of($$4.blockPosition()));
            Vec3 $$8 = null;
            for (int $$9 = 0; $$9 < 5; ++$$9) {
                Vec3 $$10 = LandRandomPos.getPos($$4, 15, 7, $$1 -> -$$6.sectionsToVillage(SectionPos.of($$1)));
                if ($$10 == null) continue;
                int $$11 = $$6.sectionsToVillage(SectionPos.of(BlockPos.containing($$10)));
                if ($$11 < $$7) {
                    $$8 = $$10;
                    break;
                }
                if ($$11 != $$7) continue;
                $$8 = $$10;
            }
            if ($$8 != null) {
                $$2.set(new WalkTarget($$8, $$0, $$1));
            }
            return true;
        }));
    }
}

