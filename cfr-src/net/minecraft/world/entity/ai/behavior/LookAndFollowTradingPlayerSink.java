/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

public class LookAndFollowTradingPlayerSink
extends Behavior<Villager> {
    private final float speedModifier;

    public LookAndFollowTradingPlayerSink(float $$0) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED)), Integer.MAX_VALUE);
        this.speedModifier = $$0;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
        Player $$2 = $$1.getTradingPlayer();
        return $$1.isAlive() && $$2 != null && !$$1.isInWater() && !$$1.hurtMarked && $$1.distanceToSqr($$2) <= 16.0;
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
        return this.checkExtraStartConditions($$0, $$1);
    }

    @Override
    protected void start(ServerLevel $$0, Villager $$1, long $$2) {
        this.followPlayer($$1);
    }

    @Override
    protected void stop(ServerLevel $$0, Villager $$1, long $$2) {
        Brain<Villager> $$3 = $$1.getBrain();
        $$3.eraseMemory(MemoryModuleType.WALK_TARGET);
        $$3.eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
        this.followPlayer($$1);
    }

    @Override
    protected boolean timedOut(long $$0) {
        return false;
    }

    private void followPlayer(Villager $$0) {
        Brain<Villager> $$1 = $$0.getBrain();
        $$1.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker($$0.getTradingPlayer(), false), this.speedModifier, 2));
        $$1.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$0.getTradingPlayer(), true));
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

