package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.Path;

public class ShootTongue extends Behavior<Frog> {
   public static final int TIME_OUT_DURATION = 100;
   public static final int CATCH_ANIMATION_DURATION = 6;
   public static final int TONGUE_ANIMATION_DURATION = 10;
   private static final float EATING_DISTANCE = 1.75F;
   private static final float EATING_MOVEMENT_FACTOR = 0.75F;
   public static final int UNREACHABLE_TONGUE_TARGETS_COOLDOWN_DURATION = 100;
   public static final int MAX_UNREACHBLE_TONGUE_TARGETS_IN_MEMORY = 5;
   private int eatAnimationTimer;
   private int calculatePathCounter;
   private final SoundEvent tongueSound;
   private final SoundEvent eatSound;
   private ShootTongue.State state = ShootTongue.State.DONE;

   public ShootTongue(SoundEvent $$0, SoundEvent $$1) {
      super(
         ImmutableMap.of(
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.LOOK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.ATTACK_TARGET,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.IS_PANICKING,
            MemoryStatus.VALUE_ABSENT
         ),
         100
      );
      this.tongueSound = $$0;
      this.eatSound = $$1;
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Frog $$1) {
      net.minecraft.world.entity.LivingEntity $$2 = $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
      boolean $$3 = this.canPathfindToTarget($$1, $$2);
      if (!$$3) {
         $$1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
         this.addUnreachableTargetToMemory($$1, $$2);
      }

      return $$3 && $$1.getPose() != net.minecraft.world.entity.Pose.CROAKING && Frog.canEat($$2);
   }

   protected boolean canStillUse(ServerLevel $$0, Frog $$1, long $$2) {
      return $$1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
         && this.state != ShootTongue.State.DONE
         && !$$1.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
   }

   protected void start(ServerLevel $$0, Frog $$1, long $$2) {
      net.minecraft.world.entity.LivingEntity $$3 = $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
      BehaviorUtils.lookAtEntity($$1, $$3);
      $$1.setTongueTarget($$3);
      $$1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget($$3.position(), 2.0F, 0));
      this.calculatePathCounter = 10;
      this.state = ShootTongue.State.MOVE_TO_TARGET;
   }

   protected void stop(ServerLevel $$0, Frog $$1, long $$2) {
      $$1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
      $$1.eraseTongueTarget();
      $$1.setPose(net.minecraft.world.entity.Pose.STANDING);
   }

   private void eatEntity(ServerLevel $$0, Frog $$1) {
      $$0.playSound(null, $$1, this.eatSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
      Optional<net.minecraft.world.entity.Entity> $$2 = $$1.getTongueTarget();
      if ($$2.isPresent()) {
         net.minecraft.world.entity.Entity $$3 = $$2.get();
         if ($$3.isAlive()) {
            $$1.doHurtTarget($$0, $$3);
            if (!$$3.isAlive()) {
               $$3.remove(net.minecraft.world.entity.Entity.RemovalReason.KILLED);
            }
         }
      }
   }

   protected void tick(ServerLevel $$0, Frog $$1, long $$2) {
      net.minecraft.world.entity.LivingEntity $$3 = $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
      $$1.setTongueTarget($$3);
      switch (this.state) {
         case MOVE_TO_TARGET:
            if ($$3.distanceTo($$1) < 1.75F) {
               $$0.playSound(null, $$1, this.tongueSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
               $$1.setPose(net.minecraft.world.entity.Pose.USING_TONGUE);
               $$3.setDeltaMovement($$3.position().vectorTo($$1.position()).normalize().scale(0.75));
               this.eatAnimationTimer = 0;
               this.state = ShootTongue.State.CATCH_ANIMATION;
            } else if (this.calculatePathCounter <= 0) {
               $$1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget($$3.position(), 2.0F, 0));
               this.calculatePathCounter = 10;
            } else {
               this.calculatePathCounter--;
            }
            break;
         case CATCH_ANIMATION:
            if (this.eatAnimationTimer++ >= 6) {
               this.state = ShootTongue.State.EAT_ANIMATION;
               this.eatEntity($$0, $$1);
            }
            break;
         case EAT_ANIMATION:
            if (this.eatAnimationTimer >= 10) {
               this.state = ShootTongue.State.DONE;
            } else {
               this.eatAnimationTimer++;
            }
         case DONE:
      }
   }

   private boolean canPathfindToTarget(Frog $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Path $$2 = $$0.getNavigation().createPath($$1, 0);
      return $$2 != null && $$2.getDistToTarget() < 1.75F;
   }

   private void addUnreachableTargetToMemory(Frog $$0, net.minecraft.world.entity.LivingEntity $$1) {
      List<UUID> $$2 = $$0.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
      boolean $$3 = !$$2.contains($$1.getUUID());
      if ($$2.size() == 5 && $$3) {
         $$2.remove(0);
      }

      if ($$3) {
         $$2.add($$1.getUUID());
      }

      $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS, $$2, 100L);
   }

   static enum State {
      MOVE_TO_TARGET,
      CATCH_ANIMATION,
      EAT_ANIMATION,
      DONE;
   }
}
