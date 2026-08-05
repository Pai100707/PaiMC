/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;

public class PlayerSensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS);
    }

    @Override
    protected void doTick(ServerLevel $$0, LivingEntity $$12) {
        List $$22 = $$0.players().stream().filter(EntitySelector.NO_SPECTATORS).filter($$1 -> $$12.closerThan((Entity)$$1, this.getFollowDistance($$12))).sorted(Comparator.comparingDouble($$12::distanceToSqr)).collect(Collectors.toList());
        Brain<?> $$3 = $$12.getBrain();
        $$3.setMemory(MemoryModuleType.NEAREST_PLAYERS, $$22);
        List $$4 = $$22.stream().filter($$2 -> PlayerSensor.isEntityTargetable($$0, $$12, $$2)).collect(Collectors.toList());
        $$3.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, $$4.isEmpty() ? null : (Player)$$4.get(0));
        List<Player> $$5 = $$4.stream().filter($$2 -> PlayerSensor.isEntityAttackable($$0, $$12, $$2)).toList();
        $$3.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS, $$5);
        $$3.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, $$5.isEmpty() ? null : $$5.get(0));
    }

    protected double getFollowDistance(LivingEntity $$0) {
        return $$0.getAttributeValue(Attributes.FOLLOW_RANGE);
    }
}

