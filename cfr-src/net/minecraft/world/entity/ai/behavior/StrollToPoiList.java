/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.apache.commons.lang3.mutable.MutableLong
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollToPoiList {
    public static BehaviorControl<Villager> create(MemoryModuleType<List<GlobalPos>> $$0, float $$1, int $$2, int $$3, MemoryModuleType<GlobalPos> $$4) {
        MutableLong $$5 = new MutableLong(0L);
        return BehaviorBuilder.create($$62 -> $$62.group($$62.registered(MemoryModuleType.WALK_TARGET), $$62.present($$0), $$62.present($$4)).apply((Applicative)$$62, ($$5, $$6, $$7) -> ($$8, $$9, $$10) -> {
            List $$11 = (List)$$62.get($$6);
            GlobalPos $$12 = (GlobalPos)$$62.get($$7);
            if ($$11.isEmpty()) {
                return false;
            }
            GlobalPos $$13 = (GlobalPos)$$11.get($$8.getRandom().nextInt($$11.size()));
            if ($$13 == null || $$8.dimension() != $$13.dimension() || !$$12.pos().closerToCenterThan($$9.position(), $$3)) {
                return false;
            }
            if ($$10 > $$5.longValue()) {
                $$5.set(new WalkTarget($$13.pos(), $$1, $$2));
                $$5.setValue($$10 + 100L);
            }
            return true;
        }));
    }
}

