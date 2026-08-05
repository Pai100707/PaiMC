/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.pathfinder.Path;

public class ShootTongue
extends Behavior<Frog> {
    public static final int TIME_OUT_DURATION = 100;
    public static final int CATCH_ANIMATION_DURATION = 6;
    public static final int TONGUE_ANIMATION_DURATION = 10;
    private static final float EATING_DISTANCE = 1.75f;
    private static final float EATING_MOVEMENT_FACTOR = 0.75f;
    public static final int UNREACHABLE_TONGUE_TARGETS_COOLDOWN_DURATION = 100;
    public static final int MAX_UNREACHBLE_TONGUE_TARGETS_IN_MEMORY = 5;
    private int eatAnimationTimer;
    private int calculatePathCounter;
    private final SoundEvent tongueSound;
    private final SoundEvent eatSound;
    private State state = State.DONE;

    public ShootTongue(SoundEvent $$0, SoundEvent $$1) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.IS_PANICKING, (Object)((Object)MemoryStatus.VALUE_ABSENT)), 100);
        this.tongueSound = $$0;
        this.eatSound = $$1;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Frog $$1) {
        LivingEntity $$2 = $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        boolean $$3 = this.canPathfindToTarget($$1, $$2);
        if (!$$3) {
            $$1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            this.addUnreachableTargetToMemory($$1, $$2);
        }
        return $$3 && $$1.getPose() != Pose.CROAKING && Frog.canEat($$2);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Frog $$1, long $$2) {
        return $$1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.state != State.DONE && !$$1.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected void start(ServerLevel $$0, Frog $$1, long $$2) {
        LivingEntity $$3 = $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        BehaviorUtils.lookAtEntity($$1, $$3);
        $$1.setTongueTarget($$3);
        $$1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget($$3.position(), 2.0f, 0));
        this.calculatePathCounter = 10;
        this.state = State.MOVE_TO_TARGET;
    }

    @Override
    protected void stop(ServerLevel $$0, Frog $$1, long $$2) {
        $$1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        $$1.eraseTongueTarget();
        $$1.setPose(Pose.STANDING);
    }

    private void eatEntity(ServerLevel $$0, Frog $$1) {
        Entity $$3;
        $$0.playSound(null, $$1, this.eatSound, SoundSource.NEUTRAL, 2.0f, 1.0f);
        Optional<Entity> $$2 = $$1.getTongueTarget();
        if ($$2.isPresent() && ($$3 = $$2.get()).isAlive()) {
            $$1.doHurtTarget($$0, $$3);
            if (!$$3.isAlive()) {
                $$3.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    protected void tick(ServerLevel $$0, Frog $$1, long $$2) {
        LivingEntity $$3 = $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        $$1.setTongueTarget($$3);
        switch (this.state.ordinal()) {
            case 0: {
                if ($$3.distanceTo($$1) < 1.75f) {
                    $$0.playSound(null, $$1, this.tongueSound, SoundSource.NEUTRAL, 2.0f, 1.0f);
                    $$1.setPose(Pose.USING_TONGUE);
                    $$3.setDeltaMovement($$3.position().vectorTo($$1.position()).normalize().scale(0.75));
                    this.eatAnimationTimer = 0;
                    this.state = State.CATCH_ANIMATION;
                    break;
                }
                if (this.calculatePathCounter <= 0) {
                    $$1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget($$3.position(), 2.0f, 0));
                    this.calculatePathCounter = 10;
                    break;
                }
                --this.calculatePathCounter;
                break;
            }
            case 1: {
                if (this.eatAnimationTimer++ < 6) break;
                this.state = State.EAT_ANIMATION;
                this.eatEntity($$0, $$1);
                break;
            }
            case 2: {
                if (this.eatAnimationTimer >= 10) {
                    this.state = State.DONE;
                    break;
                }
                ++this.eatAnimationTimer;
                break;
            }
        }
    }

    private boolean canPathfindToTarget(Frog $$0, LivingEntity $$1) {
        Path $$2 = $$0.getNavigation().createPath($$1, 0);
        return $$2 != null && $$2.getDistToTarget() < 1.75f;
    }

    private void addUnreachableTargetToMemory(Frog $$0, LivingEntity $$1) {
        boolean $$3;
        List $$2 = $$0.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
        boolean bl = $$3 = !$$2.contains($$1.getUUID());
        if ($$2.size() == 5 && $$3) {
            $$2.remove(0);
        }
        if ($$3) {
            $$2.add($$1.getUUID());
        }
        $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS, $$2, 100L);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Frog)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Frog)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Frog)livingEntity, l);
    }

    static enum State {
        MOVE_TO_TARGET,
        CATCH_ANIMATION,
        EAT_ANIMATION,
        DONE;

    }
}

