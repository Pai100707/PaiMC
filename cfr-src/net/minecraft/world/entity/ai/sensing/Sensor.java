/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.sensing;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class Sensor<E extends LivingEntity> {
    private static final RandomSource RANDOM = RandomSource.createThreadSafe();
    private static final int DEFAULT_SCAN_RATE = 20;
    private static final int DEFAULT_TARGETING_RANGE = 16;
    private static final TargetingConditions TARGET_CONDITIONS = TargetingConditions.forNonCombat().range(16.0);
    private static final TargetingConditions TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forNonCombat().range(16.0).ignoreInvisibilityTesting();
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS = TargetingConditions.forCombat().range(16.0);
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forCombat().range(16.0).ignoreInvisibilityTesting();
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0).ignoreLineOfSight();
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0).ignoreLineOfSight().ignoreInvisibilityTesting();
    private final int scanRate;
    private long timeToTick;

    public Sensor(int $$0) {
        this.scanRate = $$0;
        this.timeToTick = RANDOM.nextInt($$0);
    }

    public Sensor() {
        this(20);
    }

    public final void tick(ServerLevel $$0, E $$1) {
        if (--this.timeToTick <= 0L) {
            this.timeToTick = this.scanRate;
            this.updateTargetingConditionRanges($$1);
            this.doTick($$0, $$1);
        }
    }

    private void updateTargetingConditionRanges(E $$0) {
        double $$1 = ((LivingEntity)$$0).getAttributeValue(Attributes.FOLLOW_RANGE);
        TARGET_CONDITIONS.range($$1);
        TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.range($$1);
        ATTACK_TARGET_CONDITIONS.range($$1);
        ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.range($$1);
        ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.range($$1);
        ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.range($$1);
    }

    protected abstract void doTick(ServerLevel var1, E var2);

    public abstract Set<MemoryModuleType<?>> requires();

    public static boolean isEntityTargetable(ServerLevel $$0, LivingEntity $$1, LivingEntity $$2) {
        if ($$1.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, $$2)) {
            return TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test($$0, $$1, $$2);
        }
        return TARGET_CONDITIONS.test($$0, $$1, $$2);
    }

    public static boolean isEntityAttackable(ServerLevel $$0, LivingEntity $$1, LivingEntity $$2) {
        if ($$1.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, $$2)) {
            return ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test($$0, $$1, $$2);
        }
        return ATTACK_TARGET_CONDITIONS.test($$0, $$1, $$2);
    }

    public static BiPredicate<ServerLevel, LivingEntity> wasEntityAttackableLastNTicks(LivingEntity $$0, int $$12) {
        return Sensor.rememberPositives($$12, ($$1, $$2) -> Sensor.isEntityAttackable($$1, $$0, $$2));
    }

    public static boolean isEntityAttackableIgnoringLineOfSight(ServerLevel $$0, LivingEntity $$1, LivingEntity $$2) {
        if ($$1.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, $$2)) {
            return ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.test($$0, $$1, $$2);
        }
        return ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.test($$0, $$1, $$2);
    }

    static <T, U> BiPredicate<T, U> rememberPositives(int $$0, BiPredicate<T, U> $$1) {
        AtomicInteger $$2 = new AtomicInteger(0);
        return ($$3, $$4) -> {
            if ($$1.test($$3, $$4)) {
                $$2.set($$0);
                return true;
            }
            return $$2.decrementAndGet() >= 0;
        };
    }
}

