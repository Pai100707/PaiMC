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
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class HoglinSpecificSensor
extends Sensor<Hoglin> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, (Object[])new MemoryModuleType[0]);
    }

    @Override
    protected void doTick(ServerLevel $$02, Hoglin $$1) {
        Brain<Hoglin> $$2 = $$1.getBrain();
        $$2.setMemory(MemoryModuleType.NEAREST_REPELLENT, this.findNearestRepellent($$02, $$1));
        Optional<Object> $$3 = Optional.empty();
        int $$4 = 0;
        ArrayList $$5 = Lists.newArrayList();
        NearestVisibleLivingEntities $$6 = $$2.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        for (LivingEntity $$7 : $$6.findAll($$0 -> !$$0.isBaby() && ($$0 instanceof Piglin || $$0 instanceof Hoglin))) {
            if ($$7 instanceof Piglin) {
                Piglin $$8 = (Piglin)$$7;
                ++$$4;
                if ($$3.isEmpty()) {
                    $$3 = Optional.of($$8);
                }
            }
            if (!($$7 instanceof Hoglin)) continue;
            Hoglin $$9 = (Hoglin)$$7;
            $$5.add($$9);
        }
        $$2.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, $$3);
        $$2.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, $$5);
        $$2.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, $$4);
        $$2.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, $$5.size());
    }

    private Optional<BlockPos> findNearestRepellent(ServerLevel $$0, Hoglin $$12) {
        return BlockPos.findClosestMatch($$12.blockPosition(), 8, 4, $$1 -> $$0.getBlockState((BlockPos)$$1).is(BlockTags.HOGLIN_REPELLENTS));
    }
}

