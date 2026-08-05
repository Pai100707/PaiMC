/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.pathfinder.Path;

public class VillagerMakeLove
extends Behavior<Villager> {
    private long birthTimestamp;

    public VillagerMakeLove() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, (Object)((Object)MemoryStatus.VALUE_PRESENT)), 350, 350);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
        return this.isBreedingPossible($$1);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
        return $$2 <= this.birthTimestamp && this.isBreedingPossible($$1);
    }

    @Override
    protected void start(ServerLevel $$0, Villager $$1, long $$2) {
        AgeableMob $$3 = $$1.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
        BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, 0.5f, 2);
        $$0.broadcastEntityEvent($$3, (byte)18);
        $$0.broadcastEntityEvent($$1, (byte)18);
        int $$4 = 275 + $$1.getRandom().nextInt(50);
        this.birthTimestamp = $$2 + (long)$$4;
    }

    @Override
    protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
        Villager $$3 = (Villager)$$1.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
        if ($$1.distanceToSqr($$3) > 5.0) {
            return;
        }
        BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, 0.5f, 2);
        if ($$2 >= this.birthTimestamp) {
            $$1.eatAndDigestFood();
            $$3.eatAndDigestFood();
            this.tryToGiveBirth($$0, $$1, $$3);
        } else if ($$1.getRandom().nextInt(35) == 0) {
            $$0.broadcastEntityEvent($$3, (byte)12);
            $$0.broadcastEntityEvent($$1, (byte)12);
        }
    }

    private void tryToGiveBirth(ServerLevel $$0, Villager $$1, Villager $$2) {
        Optional<BlockPos> $$3 = this.takeVacantBed($$0, $$1);
        if ($$3.isEmpty()) {
            $$0.broadcastEntityEvent($$2, (byte)13);
            $$0.broadcastEntityEvent($$1, (byte)13);
        } else {
            Optional<Villager> $$4 = this.breed($$0, $$1, $$2);
            if ($$4.isPresent()) {
                this.giveBedToChild($$0, $$4.get(), $$3.get());
            } else {
                $$0.getPoiManager().release($$3.get());
                $$0.debugSynchronizers().updatePoi($$3.get());
            }
        }
    }

    @Override
    protected void stop(ServerLevel $$0, Villager $$1, long $$2) {
        $$1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
    }

    private boolean isBreedingPossible(Villager $$02) {
        Brain<Villager> $$1 = $$02.getBrain();
        Optional<AgeableMob> $$2 = $$1.getMemory(MemoryModuleType.BREED_TARGET).filter($$0 -> $$0.getType() == EntityType.VILLAGER);
        if ($$2.isEmpty()) {
            return false;
        }
        return BehaviorUtils.targetIsValid($$1, MemoryModuleType.BREED_TARGET, EntityType.VILLAGER) && $$02.canBreed() && $$2.get().canBreed();
    }

    private Optional<BlockPos> takeVacantBed(ServerLevel $$02, Villager $$12) {
        return $$02.getPoiManager().take($$0 -> $$0.is(PoiTypes.HOME), ($$1, $$2) -> this.canReach($$12, (BlockPos)$$2, (Holder<PoiType>)$$1), $$12.blockPosition(), 48);
    }

    private boolean canReach(Villager $$0, BlockPos $$1, Holder<PoiType> $$2) {
        Path $$3 = $$0.getNavigation().createPath($$1, $$2.value().validRange());
        return $$3 != null && $$3.canReach();
    }

    private Optional<Villager> breed(ServerLevel $$0, Villager $$1, Villager $$2) {
        Villager $$3 = $$1.getBreedOffspring($$0, $$2);
        if ($$3 == null) {
            return Optional.empty();
        }
        $$1.setAge(6000);
        $$2.setAge(6000);
        $$3.setAge(-24000);
        $$3.snapTo($$1.getX(), $$1.getY(), $$1.getZ(), 0.0f, 0.0f);
        $$0.addFreshEntityWithPassengers($$3);
        $$0.broadcastEntityEvent($$3, (byte)12);
        return Optional.of($$3);
    }

    private void giveBedToChild(ServerLevel $$0, Villager $$1, BlockPos $$2) {
        GlobalPos $$3 = GlobalPos.of($$0.dimension(), $$2);
        $$1.getBrain().setMemory(MemoryModuleType.HOME, $$3);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

