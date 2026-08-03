package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class BegGoal extends Goal {
   private final Wolf wolf;
   @Nullable
   private Player player;
   private final ServerLevel level;
   private final float lookDistance;
   private int lookTime;
   private final TargetingConditions begTargeting;

   public BegGoal(Wolf $$0, float $$1) {
      this.wolf = $$0;
      this.level = getServerLevel($$0);
      this.lookDistance = $$1;
      this.begTargeting = TargetingConditions.forNonCombat().range($$1);
      this.setFlags(EnumSet.of(Goal.Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      this.player = this.level.getNearestPlayer(this.begTargeting, this.wolf);
      return this.player == null ? false : this.playerHoldingInteresting(this.player);
   }

   @Override
   public boolean canContinueToUse() {
      if (!this.player.isAlive()) {
         return false;
      } else {
         return this.wolf.distanceToSqr(this.player) > this.lookDistance * this.lookDistance
            ? false
            : this.lookTime > 0 && this.playerHoldingInteresting(this.player);
      }
   }

   @Override
   public void start() {
      this.wolf.setIsInterested(true);
      this.lookTime = this.adjustedTickDelay(40 + this.wolf.getRandom().nextInt(40));
   }

   @Override
   public void stop() {
      this.wolf.setIsInterested(false);
      this.player = null;
   }

   @Override
   public void tick() {
      this.wolf.getLookControl().setLookAt(this.player.getX(), this.player.getEyeY(), this.player.getZ(), 10.0F, this.wolf.getMaxHeadXRot());
      this.lookTime--;
   }

   private boolean playerHoldingInteresting(Player $$0) {
      for (InteractionHand $$1 : InteractionHand.values()) {
         ItemStack $$2 = $$0.getItemInHand($$1);
         if ($$2.is(Items.BONE) || this.wolf.isFood($$2)) {
            return true;
         }
      }

      return false;
   }
}
