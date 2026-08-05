/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GoAndGiveItemsToTarget<E extends LivingEntity>
extends Behavior<E> {
    private static final int CLOSE_ENOUGH_DISTANCE_TO_TARGET = 3;
    private static final int ITEM_PICKUP_COOLDOWN_AFTER_THROWING = 60;
    private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
    private final float speedModifier;

    public GoAndGiveItemsToTarget(Function<LivingEntity, Optional<PositionTracker>> $$0, float $$1, int $$2) {
        super(Map.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.REGISTERED), $$2);
        this.targetPositionGetter = $$0;
        this.speedModifier = $$1;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, E $$1) {
        return this.canThrowItemToTarget($$1);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
        return this.canThrowItemToTarget($$1);
    }

    @Override
    protected void start(ServerLevel $$0, E $$12, long $$2) {
        this.targetPositionGetter.apply((LivingEntity)$$12).ifPresent($$1 -> BehaviorUtils.setWalkAndLookTargetMemories($$12, $$1, this.speedModifier, 3));
    }

    @Override
    protected void tick(ServerLevel $$0, E $$1, long $$22) {
        ItemStack $$6;
        Optional<PositionTracker> $$3 = this.targetPositionGetter.apply((LivingEntity)$$1);
        if ($$3.isEmpty()) {
            return;
        }
        PositionTracker $$4 = $$3.get();
        double $$5 = $$4.currentPosition().distanceTo(((Entity)$$1).getEyePosition());
        if ($$5 < 3.0 && !($$6 = ((InventoryCarrier)$$1).getInventory().removeItem(0, 1)).isEmpty()) {
            GoAndGiveItemsToTarget.throwItem($$1, $$6, GoAndGiveItemsToTarget.getThrowPosition($$4));
            if ($$1 instanceof Allay) {
                Allay $$7 = (Allay)$$1;
                AllayAi.getLikedPlayer($$7).ifPresent($$2 -> this.triggerDropItemOnBlock($$4, $$6, (ServerPlayer)$$2));
            }
            ((LivingEntity)$$1).getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 60);
        }
    }

    private void triggerDropItemOnBlock(PositionTracker $$0, ItemStack $$1, ServerPlayer $$2) {
        BlockPos $$3 = $$0.currentBlockPosition().below();
        CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.trigger($$2, $$3, $$1);
    }

    private boolean canThrowItemToTarget(E $$0) {
        if (((InventoryCarrier)$$0).getInventory().isEmpty()) {
            return false;
        }
        Optional<PositionTracker> $$1 = this.targetPositionGetter.apply((LivingEntity)$$0);
        return $$1.isPresent();
    }

    private static Vec3 getThrowPosition(PositionTracker $$0) {
        return $$0.currentPosition().add(0.0, 1.0, 0.0);
    }

    public static void throwItem(LivingEntity $$0, ItemStack $$1, Vec3 $$2) {
        Vec3 $$3 = new Vec3(0.2f, 0.3f, 0.2f);
        BehaviorUtils.throwItem($$0, $$1, $$2, $$3, 0.2f);
        Level $$4 = $$0.level();
        if ($$4.getGameTime() % 7L == 0L && $$4.random.nextDouble() < 0.9) {
            float $$5 = Util.getRandom(Allay.THROW_SOUND_PITCHES, $$4.getRandom()).floatValue();
            $$4.playSound(null, $$0, SoundEvents.ALLAY_THROW, SoundSource.NEUTRAL, 1.0f, $$5);
        }
    }
}

