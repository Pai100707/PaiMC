/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.sensing;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class MobSensor<T extends LivingEntity>
extends Sensor<T> {
    private final BiPredicate<T, LivingEntity> mobTest;
    private final Predicate<T> readyTest;
    private final MemoryModuleType<Boolean> toSet;
    private final int memoryTimeToLive;

    public MobSensor(int $$0, BiPredicate<T, LivingEntity> $$1, Predicate<T> $$2, MemoryModuleType<Boolean> $$3, int $$4) {
        super($$0);
        this.mobTest = $$1;
        this.readyTest = $$2;
        this.toSet = $$3;
        this.memoryTimeToLive = $$4;
    }

    @Override
    protected void doTick(ServerLevel $$0, T $$1) {
        if (!this.readyTest.test($$1)) {
            this.clearMemory($$1);
        } else {
            this.checkForMobsNearby($$1);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(MemoryModuleType.NEAREST_LIVING_ENTITIES);
    }

    public void checkForMobsNearby(T $$0) {
        Optional<List<LivingEntity>> $$12 = ((LivingEntity)$$0).getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
        if ($$12.isEmpty()) {
            return;
        }
        boolean $$2 = $$12.get().stream().anyMatch($$1 -> this.mobTest.test((LivingEntity)$$0, (LivingEntity)$$1));
        if ($$2) {
            this.mobDetected($$0);
        }
    }

    public void mobDetected(T $$0) {
        ((LivingEntity)$$0).getBrain().setMemoryWithExpiry(this.toSet, true, this.memoryTimeToLive);
    }

    public void clearMemory(T $$0) {
        ((LivingEntity)$$0).getBrain().eraseMemory(this.toSet);
    }
}

