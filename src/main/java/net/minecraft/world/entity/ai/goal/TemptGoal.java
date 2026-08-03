package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TemptGoal extends Goal {
   private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().ignoreLineOfSight();
   private static final double DEFAULT_STOP_DISTANCE = 2.5;
   private final TargetingConditions targetingConditions;
   protected final net.minecraft.world.entity.Mob mob;
   protected final double speedModifier;
   private double px;
   private double py;
   private double pz;
   private double pRotX;
   private double pRotY;
   @Nullable
   protected Player player;
   private int calmDown;
   private boolean isRunning;
   private final Predicate<ItemStack> items;
   private final boolean canScare;
   private final double stopDistance;

   public TemptGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, Predicate<ItemStack> $$2, boolean $$3) {
      this((net.minecraft.world.entity.Mob)$$0, $$1, $$2, $$3, 2.5);
   }

   public TemptGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, Predicate<ItemStack> $$2, boolean $$3, double $$4) {
      this((net.minecraft.world.entity.Mob)$$0, $$1, $$2, $$3, $$4);
   }

   TemptGoal(net.minecraft.world.entity.Mob $$0, double $$1, Predicate<ItemStack> $$2, boolean $$3, double $$4) {
      this.mob = $$0;
      this.speedModifier = $$1;
      this.items = $$2;
      this.canScare = $$3;
      this.stopDistance = $$4;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      this.targetingConditions = TEMPT_TARGETING.copy().selector(($$0x, $$1x) -> this.shouldFollow($$0x));
   }

   @Override
   public boolean canUse() {
      if (this.calmDown > 0) {
         this.calmDown--;
         return false;
      } else {
         this.player = getServerLevel(this.mob).getNearestPlayer(this.targetingConditions.range(this.mob.getAttributeValue(Attributes.TEMPT_RANGE)), this.mob);
         return this.player != null;
      }
   }

   private boolean shouldFollow(net.minecraft.world.entity.LivingEntity $$0) {
      return this.items.test($$0.getMainHandItem()) || this.items.test($$0.getOffhandItem());
   }

   @Override
   public boolean canContinueToUse() {
      if (this.canScare()) {
         if (this.mob.distanceToSqr(this.player) < 36.0) {
            if (this.player.distanceToSqr(this.px, this.py, this.pz) > 0.010000000000000002) {
               return false;
            }

            if (Math.abs(this.player.getXRot() - this.pRotX) > 5.0 || Math.abs(this.player.getYRot() - this.pRotY) > 5.0) {
               return false;
            }
         } else {
            this.px = this.player.getX();
            this.py = this.player.getY();
            this.pz = this.player.getZ();
         }

         this.pRotX = this.player.getXRot();
         this.pRotY = this.player.getYRot();
      }

      return this.canUse();
   }

   protected boolean canScare() {
      return this.canScare;
   }

   @Override
   public void start() {
      this.px = this.player.getX();
      this.py = this.player.getY();
      this.pz = this.player.getZ();
      this.isRunning = true;
   }

   @Override
   public void stop() {
      this.player = null;
      this.stopNavigation();
      this.calmDown = reducedTickDelay(100);
      this.isRunning = false;
   }

   @Override
   public void tick() {
      this.mob.getLookControl().setLookAt(this.player, this.mob.getMaxHeadYRot() + 20, this.mob.getMaxHeadXRot());
      if (this.mob.distanceToSqr(this.player) < this.stopDistance * this.stopDistance) {
         this.stopNavigation();
      } else {
         this.navigateTowards(this.player);
      }
   }

   protected void stopNavigation() {
      this.mob.getNavigation().stop();
   }

   protected void navigateTowards(Player $$0) {
      this.mob.getNavigation().moveTo($$0, this.speedModifier);
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public static class ForNonPathfinders extends TemptGoal {
      public ForNonPathfinders(net.minecraft.world.entity.Mob $$0, double $$1, Predicate<ItemStack> $$2, boolean $$3, double $$4) {
         super($$0, $$1, $$2, $$3, $$4);
      }

      @Override
      protected void stopNavigation() {
         this.mob.getMoveControl().setWait();
      }

      @Override
      protected void navigateTowards(Player $$0) {
         Vec3 $$1 = $$0.getEyePosition().subtract(this.mob.position()).scale(this.mob.getRandom().nextDouble()).add(this.mob.position());
         this.mob.getMoveControl().setWantedPosition($$1.x, $$1.y, $$1.z, this.speedModifier);
      }
   }
}
