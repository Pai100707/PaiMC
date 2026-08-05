/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

@Deprecated
public class SetEntityLookTargetSometimes {
    public static BehaviorControl<LivingEntity> create(float $$02, UniformInt $$1) {
        return SetEntityLookTargetSometimes.create($$02, $$1, (LivingEntity $$0) -> true);
    }

    public static BehaviorControl<LivingEntity> create(EntityType<?> $$0, float $$12, UniformInt $$2) {
        return SetEntityLookTargetSometimes.create($$12, $$2, (LivingEntity $$1) -> $$0.equals($$1.getType()));
    }

    private static BehaviorControl<LivingEntity> create(float $$0, UniformInt $$1, Predicate<LivingEntity> $$2) {
        float $$32 = $$0 * $$0;
        Ticker $$4 = new Ticker($$1);
        return BehaviorBuilder.create($$3 -> $$3.group($$3.absent(MemoryModuleType.LOOK_TARGET), $$3.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)$$3, ($$4, $$5) -> ($$6, $$7, $$8) -> {
            Optional<LivingEntity> $$9 = ((NearestVisibleLivingEntities)$$3.get($$5)).findClosest($$2.and($$2 -> $$2.distanceToSqr($$7) <= (double)$$32));
            if ($$9.isEmpty()) {
                return false;
            }
            if (!$$4.tickDownAndCheck($$6.random)) {
                return false;
            }
            $$4.set(new EntityTracker($$9.get(), true));
            return true;
        }));
    }

    public static final class Ticker {
        private final UniformInt interval;
        private int ticksUntilNextStart;

        public Ticker(UniformInt $$0) {
            if ($$0.getMinValue() <= 1) {
                throw new IllegalArgumentException();
            }
            this.interval = $$0;
        }

        public boolean tickDownAndCheck(RandomSource $$0) {
            if (this.ticksUntilNextStart == 0) {
                this.ticksUntilNextStart = this.interval.sample($$0) - 1;
                return false;
            }
            return --this.ticksUntilNextStart == 0;
        }
    }
}

