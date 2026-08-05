/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.breeze.LongJump;
import net.minecraft.world.entity.monster.breeze.Shoot;
import net.minecraft.world.entity.monster.breeze.ShootWhenStuck;
import net.minecraft.world.entity.monster.breeze.Slide;
import net.minecraft.world.entity.schedule.Activity;

public class BreezeAi {
    public static final float SPEED_MULTIPLIER_WHEN_SLIDING = 0.6f;
    public static final float JUMP_CIRCLE_INNER_RADIUS = 4.0f;
    public static final float JUMP_CIRCLE_MIDDLE_RADIUS = 8.0f;
    public static final float JUMP_CIRCLE_OUTER_RADIUS = 24.0f;
    static final List<SensorType<? extends Sensor<? super Breeze>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS, SensorType.BREEZE_ATTACK_ENTITY_SENSOR);
    static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryModuleType.BREEZE_JUMP_INHALING, MemoryModuleType.BREEZE_SHOOT, MemoryModuleType.BREEZE_SHOOT_CHARGING, MemoryModuleType.BREEZE_SHOOT_RECOVERING, MemoryModuleType.BREEZE_SHOOT_COOLDOWN, (Object[])new MemoryModuleType[]{MemoryModuleType.BREEZE_JUMP_TARGET, MemoryModuleType.BREEZE_LEAVING_WATER, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.PATH});
    private static final int TICKS_TO_REMEMBER_SEEN_TARGET = 100;

    protected static Brain<?> makeBrain(Breeze $$0, Brain<Breeze> $$1) {
        BreezeAi.initCoreActivity($$1);
        BreezeAi.initIdleActivity($$1);
        BreezeAi.initFightActivity($$0, $$1);
        $$1.setCoreActivities(Set.of(Activity.CORE));
        $$1.setDefaultActivity(Activity.FIGHT);
        $$1.useDefaultActivity();
        return $$1;
    }

    private static void initCoreActivity(Brain<Breeze> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Breeze>>)ImmutableList.of(new Swim(0.8f), (Object)new LookAtTargetSink(45, 90)));
    }

    private static void initIdleActivity(Brain<Breeze> $$02) {
        $$02.addActivity(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<Breeze>>>)ImmutableList.of((Object)Pair.of((Object)0, StartAttacking.create(($$0, $$1) -> $$1.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))), (Object)Pair.of((Object)1, StartAttacking.create(($$0, $$1) -> $$1.getHurtBy())), (Object)Pair.of((Object)2, (Object)new SlideToTargetSink(20, 40)), (Object)Pair.of((Object)3, new RunOne(ImmutableList.of((Object)Pair.of((Object)new DoNothing(20, 100), (Object)1), (Object)Pair.of(RandomStroll.stroll(0.6f), (Object)2))))));
    }

    private static void initFightActivity(Breeze $$0, Brain<Breeze> $$1) {
        $$1.addActivityWithConditions(Activity.FIGHT, (ImmutableList<Pair<Integer, BehaviorControl<Breeze>>>)ImmutableList.of((Object)Pair.of((Object)0, StopAttackingIfTargetInvalid.create(Sensor.wasEntityAttackableLastNTicks($$0, 100).negate()::test)), (Object)Pair.of((Object)1, (Object)new Shoot()), (Object)Pair.of((Object)2, (Object)new LongJump()), (Object)Pair.of((Object)3, (Object)new ShootWhenStuck()), (Object)Pair.of((Object)4, (Object)new Slide())), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)), (Object)Pair.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT))));
    }

    static void updateActivity(Breeze $$0) {
        $$0.getBrain().setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.FIGHT, (Object)Activity.IDLE));
    }

    public static class SlideToTargetSink
    extends MoveToTargetSink {
        @VisibleForTesting
        public SlideToTargetSink(int $$0, int $$1) {
            super($$0, $$1);
        }

        @Override
        protected void start(ServerLevel $$0, Mob $$1, long $$2) {
            super.start($$0, $$1, $$2);
            $$1.playSound(SoundEvents.BREEZE_SLIDE);
            $$1.setPose(Pose.SLIDING);
        }

        @Override
        protected void stop(ServerLevel $$0, Mob $$1, long $$2) {
            super.stop($$0, $$1, $$2);
            $$1.setPose(Pose.STANDING);
            if ($$1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 60L);
            }
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Mob)livingEntity, l);
        }
    }
}

