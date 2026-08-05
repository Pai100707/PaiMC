package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;

public class HurtByTargetGoal extends TargetGoal {
   private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
   private static final int ALERT_RANGE_Y = 10;
   private boolean alertSameType;
   private int timestamp;
   private final Class<?>[] toIgnoreDamage;
   
   private Class<?>[] toIgnoreAlert;

   public HurtByTargetGoal(net.minecraft.world.entity.PathfinderMob $$0, Class<?>... $$1) {
      super($$0, true);
      this.toIgnoreDamage = $$1;
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   @Override
   public boolean canUse() {
      int $$0 = this.mob.getLastHurtByMobTimestamp();
      net.minecraft.world.entity.LivingEntity $$1 = this.mob.getLastHurtByMob();
      if ($$0 != this.timestamp && $$1 != null) {
         if ($$1.getType() == net.minecraft.world.entity.EntityType.PLAYER && (Boolean)getServerLevel(this.mob).getGameRules().get(GameRules.UNIVERSAL_ANGER)) {
            return false;
         } else {
            for (Class<?> $$2 : this.toIgnoreDamage) {
               if ($$2.isAssignableFrom($$1.getClass())) {
                  return false;
               }
            }

            return this.canAttack($$1, HURT_BY_TARGETING);
         }
      } else {
         return false;
      }
   }

   public HurtByTargetGoal setAlertOthers(Class<?>... $$0) {
      this.alertSameType = true;
      this.toIgnoreAlert = $$0;
      return this;
   }

   @Override
   public void start() {
      this.mob.setTarget(this.mob.getLastHurtByMob());
      this.targetMob = this.mob.getTarget();
      this.timestamp = this.mob.getLastHurtByMobTimestamp();
      this.unseenMemoryTicks = 300;
      if (this.alertSameType) {
         this.alertOthers();
      }

      super.start();
   }

   protected void alertOthers() {
      double $$0 = this.getFollowDistance();
      AABB $$1 = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate($$0, 10.0, $$0);
      List<? extends net.minecraft.world.entity.Mob> $$2 = this.mob
         .level()
         .getEntitiesOfClass(this.mob.getClass(), $$1, net.minecraft.world.entity.EntitySelector.NO_SPECTATORS);
      Iterator var5 = $$2.iterator();

      while (true) {
         net.minecraft.world.entity.Mob $$3;
         while (true) {
            if (!var5.hasNext()) {
               return;
            }

            $$3 = (net.minecraft.world.entity.Mob)var5.next();
            if (this.mob != $$3
               && $$3.getTarget() == null
               && (
                  !(this.mob instanceof net.minecraft.world.entity.TamableAnimal)
                     || ((net.minecraft.world.entity.TamableAnimal)this.mob).getOwner() == ((net.minecraft.world.entity.TamableAnimal)$$3).getOwner()
               )
               && !$$3.isAlliedTo(this.mob.getLastHurtByMob())) {
               if (this.toIgnoreAlert == null) {
                  break;
               }

               boolean $$4 = false;

               for (Class<?> $$5 : this.toIgnoreAlert) {
                  if ($$3.getClass() == $$5) {
                     $$4 = true;
                     break;
                  }
               }

               if (!$$4) {
                  break;
               }
            }
         }

         this.alertOther($$3, this.mob.getLastHurtByMob());
      }
   }

   protected void alertOther(net.minecraft.world.entity.Mob $$0, net.minecraft.world.entity.LivingEntity $$1) {
      $$0.setTarget($$1);
   }
}
