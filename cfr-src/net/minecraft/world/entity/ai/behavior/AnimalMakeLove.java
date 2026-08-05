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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

public class AnimalMakeLove
extends Behavior<Animal> {
    private static final int BREED_RANGE = 3;
    private static final int MIN_DURATION = 60;
    private static final int MAX_DURATION = 110;
    private final EntityType<? extends Animal> partnerType;
    private final float speedModifier;
    private final int closeEnoughDistance;
    private static final int DEFAULT_CLOSE_ENOUGH_DISTANCE = 2;
    private long spawnChildAtTime;

    public AnimalMakeLove(EntityType<? extends Animal> $$0) {
        this($$0, 1.0f, 2);
    }

    public AnimalMakeLove(EntityType<? extends Animal> $$0, float $$1, int $$2) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.IS_PANICKING, (Object)((Object)MemoryStatus.VALUE_ABSENT)), 110);
        this.partnerType = $$0;
        this.speedModifier = $$1;
        this.closeEnoughDistance = $$2;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Animal $$1) {
        return $$1.isInLove() && this.findValidBreedPartner($$1).isPresent();
    }

    @Override
    protected void start(ServerLevel $$0, Animal $$1, long $$2) {
        Animal $$3 = this.findValidBreedPartner($$1).get();
        $$1.getBrain().setMemory(MemoryModuleType.BREED_TARGET, $$3);
        $$3.getBrain().setMemory(MemoryModuleType.BREED_TARGET, $$1);
        BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, this.speedModifier, this.closeEnoughDistance);
        int $$4 = 60 + $$1.getRandom().nextInt(50);
        this.spawnChildAtTime = $$2 + (long)$$4;
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Animal $$1, long $$2) {
        if (!this.hasBreedTargetOfRightType($$1)) {
            return false;
        }
        Animal $$3 = this.getBreedTarget($$1);
        return $$3.isAlive() && $$1.canMate($$3) && BehaviorUtils.entityIsVisible($$1.getBrain(), $$3) && $$2 <= this.spawnChildAtTime && !$$1.isPanicking() && !$$3.isPanicking();
    }

    @Override
    protected void tick(ServerLevel $$0, Animal $$1, long $$2) {
        Animal $$3 = this.getBreedTarget($$1);
        BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, this.speedModifier, this.closeEnoughDistance);
        if (!$$1.closerThan($$3, 3.0)) {
            return;
        }
        if ($$2 >= this.spawnChildAtTime) {
            $$1.spawnChildFromBreeding($$0, $$3);
            $$1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
            $$3.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
        }
    }

    @Override
    protected void stop(ServerLevel $$0, Animal $$1, long $$2) {
        $$1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
        $$1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        $$1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        this.spawnChildAtTime = 0L;
    }

    private Animal getBreedTarget(Animal $$0) {
        return (Animal)$$0.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
    }

    private boolean hasBreedTargetOfRightType(Animal $$0) {
        Brain<AgeableMob> $$1 = $$0.getBrain();
        return $$1.hasMemoryValue(MemoryModuleType.BREED_TARGET) && $$1.getMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.partnerType;
    }

    private Optional<? extends Animal> findValidBreedPartner(Animal $$0) {
        return $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findClosest($$1 -> {
            Animal $$2;
            return $$1.getType() == this.partnerType && $$1 instanceof Animal && $$0.canMate($$2 = (Animal)$$1) && !$$2.isPanicking();
        }).map(Animal.class::cast);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Animal)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Animal)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Animal)livingEntity, l);
    }
}

