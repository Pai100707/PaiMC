/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;

public class SecondaryPoiSensor
extends Sensor<Villager> {
    private static final int SCAN_RATE = 40;

    public SecondaryPoiSensor() {
        super(40);
    }

    @Override
    protected void doTick(ServerLevel $$0, Villager $$1) {
        ResourceKey<Level> $$2 = $$0.dimension();
        BlockPos $$3 = $$1.blockPosition();
        ArrayList $$4 = Lists.newArrayList();
        int $$5 = 4;
        for (int $$6 = -4; $$6 <= 4; ++$$6) {
            for (int $$7 = -2; $$7 <= 2; ++$$7) {
                for (int $$8 = -4; $$8 <= 4; ++$$8) {
                    BlockPos $$9 = $$3.offset($$6, $$7, $$8);
                    if (!$$1.getVillagerData().profession().value().secondaryPoi().contains((Object)$$0.getBlockState($$9).getBlock())) continue;
                    $$4.add(GlobalPos.of($$2, $$9));
                }
            }
        }
        Brain<Villager> $$10 = $$1.getBrain();
        if (!$$4.isEmpty()) {
            $$10.setMemory(MemoryModuleType.SECONDARY_JOB_SITE, $$4);
        } else {
            $$10.eraseMemory(MemoryModuleType.SECONDARY_JOB_SITE);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.SECONDARY_JOB_SITE);
    }
}

