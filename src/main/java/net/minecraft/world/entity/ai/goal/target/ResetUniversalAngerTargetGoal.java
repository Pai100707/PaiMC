package net.minecraft.world.entity.ai.goal.target;

import java.util.List;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;

public class ResetUniversalAngerTargetGoal<T extends net.minecraft.world.entity.Mob & net.minecraft.world.entity.NeutralMob> extends Goal {
   private static final int ALERT_RANGE_Y = 10;
   private final T mob;
   private final boolean alertOthersOfSameType;
   private int lastHurtByPlayerTimestamp;

   public ResetUniversalAngerTargetGoal(T $$0, boolean $$1) {
      this.mob = $$0;
      this.alertOthersOfSameType = $$1;
   }

   @Override
   public boolean canUse() {
      return (Boolean)getServerLevel(this.mob).getGameRules().get(GameRules.UNIVERSAL_ANGER) && this.wasHurtByPlayer();
   }

   private boolean wasHurtByPlayer() {
      return this.mob.getLastHurtByMob() != null
         && this.mob.getLastHurtByMob().getType() == net.minecraft.world.entity.EntityType.PLAYER
         && this.mob.getLastHurtByMobTimestamp() > this.lastHurtByPlayerTimestamp;
   }

   @Override
   public void start() {
      this.lastHurtByPlayerTimestamp = this.mob.getLastHurtByMobTimestamp();
      this.mob.forgetCurrentTargetAndRefreshUniversalAnger();
      if (this.alertOthersOfSameType) {
         this.getNearbyMobsOfSameType()
            .stream()
            .filter($$0 -> $$0 != this.mob)
            .map($$0 -> (net.minecraft.world.entity.NeutralMob)$$0)
            .forEach(net.minecraft.world.entity.NeutralMob::forgetCurrentTargetAndRefreshUniversalAnger);
      }

      super.start();
   }

   private List<? extends net.minecraft.world.entity.Mob> getNearbyMobsOfSameType() {
      double $$0 = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
      AABB $$1 = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate($$0, 10.0, $$0);
      return this.mob.level().getEntitiesOfClass(this.mob.getClass(), $$1, net.minecraft.world.entity.EntitySelector.NO_SPECTATORS);
   }
}
