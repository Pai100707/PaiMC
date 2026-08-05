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
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TemptingSensor
extends Sensor<PathfinderMob> {
    private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().ignoreLineOfSight();
    private final BiPredicate<PathfinderMob, ItemStack> temptations;

    public TemptingSensor(Predicate<ItemStack> $$0) {
        this((PathfinderMob $$1, ItemStack $$2) -> $$0.test((ItemStack)$$2));
    }

    public static TemptingSensor forAnimal() {
        return new TemptingSensor(($$0, $$1) -> {
            if ($$0 instanceof Animal) {
                Animal $$2 = (Animal)$$0;
                return $$2.isFood((ItemStack)$$1);
            }
            return false;
        });
    }

    private TemptingSensor(BiPredicate<PathfinderMob, ItemStack> $$0) {
        this.temptations = $$0;
    }

    @Override
    protected void doTick(ServerLevel $$0, PathfinderMob $$12) {
        Brain<?> $$2 = $$12.getBrain();
        TargetingConditions $$32 = TEMPT_TARGETING.copy().range((float)$$12.getAttributeValue(Attributes.TEMPT_RANGE));
        List $$4 = $$0.players().stream().filter(EntitySelector.NO_SPECTATORS).filter($$3 -> $$32.test($$0, $$12, (LivingEntity)$$3)).filter($$1 -> this.playerHoldingTemptation($$12, (Player)$$1)).filter($$1 -> !$$12.hasPassenger((Entity)$$1)).sorted(Comparator.comparingDouble($$12::distanceToSqr)).collect(Collectors.toList());
        if (!$$4.isEmpty()) {
            Player $$5 = (Player)$$4.get(0);
            $$2.setMemory(MemoryModuleType.TEMPTING_PLAYER, $$5);
        } else {
            $$2.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        }
    }

    private boolean playerHoldingTemptation(PathfinderMob $$0, Player $$1) {
        return this.isTemptation($$0, $$1.getMainHandItem()) || this.isTemptation($$0, $$1.getOffhandItem());
    }

    private boolean isTemptation(PathfinderMob $$0, ItemStack $$1) {
        return this.temptations.test($$0, $$1);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
    }
}

