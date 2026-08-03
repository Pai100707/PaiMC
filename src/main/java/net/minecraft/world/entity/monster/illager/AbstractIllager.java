package net.minecraft.world.entity.monster.illager;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

public abstract class AbstractIllager extends Raider {
   protected AbstractIllager(net.minecraft.world.entity.EntityType<? extends AbstractIllager> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
   }

   public AbstractIllager.IllagerArmPose getArmPose() {
      return AbstractIllager.IllagerArmPose.CROSSED;
   }

   @Override
   public boolean canAttack(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0 instanceof AbstractVillager && $$0.isBaby() ? false : super.canAttack($$0);
   }

   @Override
   protected boolean considersEntityAsAlly(net.minecraft.world.entity.Entity $$0) {
      if (super.considersEntityAsAlly($$0)) {
         return true;
      } else {
         return !$$0.getType().is(EntityTypeTags.ILLAGER_FRIENDS) ? false : this.getTeam() == null && $$0.getTeam() == null;
      }
   }

   public static enum IllagerArmPose {
      CROSSED,
      ATTACKING,
      SPELLCASTING,
      BOW_AND_ARROW,
      CROSSBOW_HOLD,
      CROSSBOW_CHARGE,
      CELEBRATING,
      NEUTRAL;
   }

   protected class RaiderOpenDoorGoal extends OpenDoorGoal {
      public RaiderOpenDoorGoal(final Raider $$1) {
         super($$1, false);
      }

      @Override
      public boolean canUse() {
         return super.canUse() && AbstractIllager.this.hasActiveRaid();
      }
   }
}
