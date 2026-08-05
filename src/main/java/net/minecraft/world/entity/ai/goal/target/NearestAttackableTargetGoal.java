package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class NearestAttackableTargetGoal<T extends net.minecraft.world.entity.LivingEntity> extends TargetGoal {
   private static final int DEFAULT_RANDOM_INTERVAL = 10;
   protected final Class<T> targetType;
   protected final int randomInterval;
   
   protected net.minecraft.world.entity.LivingEntity target;
   protected TargetingConditions targetConditions;

   public NearestAttackableTargetGoal(net.minecraft.world.entity.Mob $$0, Class<T> $$1, boolean $$2) {
      this($$0, $$1, 10, $$2, false, null);
   }

   public NearestAttackableTargetGoal(net.minecraft.world.entity.Mob $$0, Class<T> $$1, boolean $$2, TargetingConditions.Selector $$3) {
      this($$0, $$1, 10, $$2, false, $$3);
   }

   public NearestAttackableTargetGoal(net.minecraft.world.entity.Mob $$0, Class<T> $$1, boolean $$2, boolean $$3) {
      this($$0, $$1, 10, $$2, $$3, null);
   }

   public NearestAttackableTargetGoal(
      net.minecraft.world.entity.Mob $$0, Class<T> $$1, int $$2, boolean $$3, boolean $$4, TargetingConditions.Selector $$5
   ) {
      super($$0, $$3, $$4);
      this.targetType = $$1;
      this.randomInterval = reducedTickDelay($$2);
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
      this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector($$5);
   }

   @Override
   public boolean canUse() {
      if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
         return false;
      } else {
         this.findTarget();
         return this.target != null;
      }
   }

   protected AABB getTargetSearchArea(double $$0) {
      return this.mob.getBoundingBox().inflate($$0, $$0, $$0);
   }

   protected void findTarget() {
      ServerLevel $$0 = getServerLevel(this.mob);
      if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
         this.target = $$0.getNearestEntity(
            this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), $$0x -> true),
            this.getTargetConditions(),
            this.mob,
            this.mob.getX(),
            this.mob.getEyeY(),
            this.mob.getZ()
         );
      } else {
         this.target = $$0.getNearestPlayer(this.getTargetConditions(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
      }
   }

   @Override
   public void start() {
      this.mob.setTarget(this.target);
      super.start();
   }

   public void setTarget(net.minecraft.world.entity.LivingEntity $$0) {
      this.target = $$0;
   }

   private TargetingConditions getTargetConditions() {
      return this.targetConditions.range(this.getFollowDistance());
   }
}
