/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Iterables
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenEntitySensor
extends NearestLivingEntitySensor<Warden> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.copyOf((Iterable)Iterables.concat(super.requires(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
    }

    @Override
    protected void doTick(ServerLevel $$02, Warden $$12) {
        super.doTick($$02, $$12);
        WardenEntitySensor.getClosest($$12, $$0 -> $$0.getType() == EntityType.PLAYER).or(() -> WardenEntitySensor.getClosest($$12, $$0 -> $$0.getType() != EntityType.PLAYER)).ifPresentOrElse($$1 -> $$12.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, $$1), () -> $$12.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE));
    }

    private static Optional<LivingEntity> getClosest(Warden $$0, Predicate<LivingEntity> $$1) {
        return $$0.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).stream().flatMap(Collection::stream).filter($$0::canTargetEntity).filter($$1).findFirst();
    }
}

