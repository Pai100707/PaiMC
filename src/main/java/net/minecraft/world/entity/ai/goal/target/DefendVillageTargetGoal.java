package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class DefendVillageTargetGoal extends TargetGoal {
   private final IronGolem golem;
   @Nullable
   private net.minecraft.world.entity.LivingEntity potentialTarget;
   private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0);

   public DefendVillageTargetGoal(IronGolem $$0) {
      super($$0, false, true);
      this.golem = $$0;
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   @Override
   public boolean canUse() {
      AABB $$0 = this.golem.getBoundingBox().inflate(10.0, 8.0, 10.0);
      ServerLevel $$1 = getServerLevel(this.golem);
      List<? extends net.minecraft.world.entity.LivingEntity> $$2 = $$1.getNearbyEntities(Villager.class, this.attackTargeting, this.golem, $$0);
      List<Player> $$3 = $$1.getNearbyPlayers(this.attackTargeting, this.golem, $$0);

      for (net.minecraft.world.entity.LivingEntity $$4 : $$2) {
         Villager $$5 = (Villager)$$4;

         for (Player $$6 : $$3) {
            int $$7 = $$5.getPlayerReputation($$6);
            if ($$7 <= -100) {
               this.potentialTarget = $$6;
            }
         }
      }

      return this.potentialTarget == null ? false : !(this.potentialTarget instanceof Player $$8 && ($$8.isSpectator() || $$8.isCreative()));
   }

   @Override
   public void start() {
      this.golem.setTarget(this.potentialTarget);
      super.start();
   }
}
