package net.minecraft.world.entity.boss.enderdragon.phases;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class DragonStrafePlayerPhase extends AbstractDragonPhaseInstance {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int FIREBALL_CHARGE_AMOUNT = 5;
   private int fireballCharge;
   
   private Path currentPath;
   
   private Vec3 targetLocation;
   
   private net.minecraft.world.entity.LivingEntity attackTarget;
   private boolean holdingPatternClockwise;

   public DragonStrafePlayerPhase(EnderDragon $$0) {
      super($$0);
   }

   @Override
   public void doServerTick(ServerLevel $$0) {
      if (this.attackTarget == null) {
         LOGGER.warn("Skipping player strafe phase because no player was found");
         this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
      } else {
         if (this.currentPath != null && this.currentPath.isDone()) {
            double $$1 = this.attackTarget.getX();
            double $$2 = this.attackTarget.getZ();
            double $$3 = $$1 - this.dragon.getX();
            double $$4 = $$2 - this.dragon.getZ();
            double $$5 = Math.sqrt($$3 * $$3 + $$4 * $$4);
            double $$6 = Math.min(0.4F + $$5 / 80.0 - 1.0, 10.0);
            this.targetLocation = new Vec3($$1, this.attackTarget.getY() + $$6, $$2);
         }

         double $$7 = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
         if ($$7 < 100.0 || $$7 > 22500.0) {
            this.findNewTarget();
         }

         double $$8 = 64.0;
         if (this.attackTarget.distanceToSqr(this.dragon) < 4096.0) {
            if (this.dragon.hasLineOfSight(this.attackTarget)) {
               this.fireballCharge++;
               Vec3 $$9 = new Vec3(this.attackTarget.getX() - this.dragon.getX(), 0.0, this.attackTarget.getZ() - this.dragon.getZ()).normalize();
               Vec3 $$10 = new Vec3(
                     Mth.sin(this.dragon.getYRot() * (float) (Math.PI / 180.0)), 0.0, -Mth.cos(this.dragon.getYRot() * (float) (Math.PI / 180.0))
                  )
                  .normalize();
               float $$11 = (float)$$10.dot($$9);
               float $$12 = (float)(Math.acos($$11) * 180.0F / (float)Math.PI);
               $$12 += 0.5F;
               if (this.fireballCharge >= 5 && $$12 >= 0.0F && $$12 < 10.0F) {
                  double $$13 = 1.0;
                  Vec3 $$14 = this.dragon.getViewVector(1.0F);
                  double $$15 = this.dragon.head.getX() - $$14.x * 1.0;
                  double $$16 = this.dragon.head.getY(0.5) + 0.5;
                  double $$17 = this.dragon.head.getZ() - $$14.z * 1.0;
                  double $$18 = this.attackTarget.getX() - $$15;
                  double $$19 = this.attackTarget.getY(0.5) - $$16;
                  double $$20 = this.attackTarget.getZ() - $$17;
                  Vec3 $$21 = new Vec3($$18, $$19, $$20);
                  if (!this.dragon.isSilent()) {
                     $$0.levelEvent(null, 1017, this.dragon.blockPosition(), 0);
                  }

                  DragonFireball $$22 = new DragonFireball($$0, this.dragon, $$21.normalize());
                  $$22.snapTo($$15, $$16, $$17, 0.0F, 0.0F);
                  $$0.addFreshEntity($$22);
                  this.fireballCharge = 0;
                  if (this.currentPath != null) {
                     while (!this.currentPath.isDone()) {
                        this.currentPath.advance();
                     }
                  }

                  this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
               }
            } else if (this.fireballCharge > 0) {
               this.fireballCharge--;
            }
         } else if (this.fireballCharge > 0) {
            this.fireballCharge--;
         }
      }
   }

   private void findNewTarget() {
      if (this.currentPath == null || this.currentPath.isDone()) {
         int $$0 = this.dragon.findClosestNode();
         int $$1 = $$0;
         if (this.dragon.getRandom().nextInt(8) == 0) {
            this.holdingPatternClockwise = !this.holdingPatternClockwise;
            $$1 = $$0 + 6;
         }

         if (this.holdingPatternClockwise) {
            $$1++;
         } else {
            $$1--;
         }

         if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() > 0) {
            $$1 %= 12;
            if ($$1 < 0) {
               $$1 += 12;
            }
         } else {
            $$1 -= 12;
            $$1 &= 7;
            $$1 += 12;
         }

         this.currentPath = this.dragon.findPath($$0, $$1, null);
         if (this.currentPath != null) {
            this.currentPath.advance();
         }
      }

      this.navigateToNextPathNode();
   }

   private void navigateToNextPathNode() {
      if (this.currentPath != null && !this.currentPath.isDone()) {
         Vec3i $$0 = this.currentPath.getNextNodePos();
         this.currentPath.advance();
         double $$1 = $$0.getX();
         double $$2 = $$0.getZ();

         double $$3;
         do {
            $$3 = $$0.getY() + this.dragon.getRandom().nextFloat() * 20.0F;
         } while ($$3 < $$0.getY());

         this.targetLocation = new Vec3($$1, $$3, $$2);
      }
   }

   @Override
   public void begin() {
      this.fireballCharge = 0;
      this.targetLocation = null;
      this.currentPath = null;
      this.attackTarget = null;
   }

   public void setTarget(net.minecraft.world.entity.LivingEntity $$0) {
      this.attackTarget = $$0;
      int $$1 = this.dragon.findClosestNode();
      int $$2 = this.dragon.findClosestNode(this.attackTarget.getX(), this.attackTarget.getY(), this.attackTarget.getZ());
      int $$3 = this.attackTarget.getBlockX();
      int $$4 = this.attackTarget.getBlockZ();
      double $$5 = $$3 - this.dragon.getX();
      double $$6 = $$4 - this.dragon.getZ();
      double $$7 = Math.sqrt($$5 * $$5 + $$6 * $$6);
      double $$8 = Math.min(0.4F + $$7 / 80.0 - 1.0, 10.0);
      int $$9 = Mth.floor(this.attackTarget.getY() + $$8);
      Node $$10 = new Node($$3, $$9, $$4);
      this.currentPath = this.dragon.findPath($$1, $$2, $$10);
      if (this.currentPath != null) {
         this.currentPath.advance();
         this.navigateToNextPathNode();
      }
   }

   
   @Override
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   @Override
   public EnderDragonPhase<DragonStrafePlayerPhase> getPhase() {
      return EnderDragonPhase.STRAFE_PLAYER;
   }
}
