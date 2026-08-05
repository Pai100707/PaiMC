package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DragonHoldingPatternPhase extends AbstractDragonPhaseInstance {
   private static final TargetingConditions NEW_TARGET_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
   
   private Path currentPath;
   
   private Vec3 targetLocation;
   private boolean clockwise;

   public DragonHoldingPatternPhase(EnderDragon $$0) {
      super($$0);
   }

   @Override
   public EnderDragonPhase<DragonHoldingPatternPhase> getPhase() {
      return EnderDragonPhase.HOLDING_PATTERN;
   }

   @Override
   public void doServerTick(ServerLevel $$0) {
      double $$1 = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if ($$1 < 100.0 || $$1 > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
         this.findNewTarget($$0);
      }
   }

   @Override
   public void begin() {
      this.currentPath = null;
      this.targetLocation = null;
   }

   
   @Override
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   private void findNewTarget(ServerLevel $$0) {
      if (this.currentPath != null && this.currentPath.isDone()) {
         BlockPos $$1 = $$0.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.dragon.getFightOrigin()));
         int $$2 = this.dragon.getDragonFight() == null ? 0 : this.dragon.getDragonFight().getCrystalsAlive();
         if (this.dragon.getRandom().nextInt($$2 + 3) == 0) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
            return;
         }

         Player $$3 = $$0.getNearestPlayer(NEW_TARGET_TARGETING, this.dragon, $$1.getX(), $$1.getY(), $$1.getZ());
         double $$4;
         if ($$3 != null) {
            $$4 = $$1.distToCenterSqr($$3.position()) / 512.0;
         } else {
            $$4 = 64.0;
         }

         if ($$3 != null && (this.dragon.getRandom().nextInt((int)($$4 + 2.0)) == 0 || this.dragon.getRandom().nextInt($$2 + 2) == 0)) {
            this.strafePlayer($$3);
            return;
         }
      }

      if (this.currentPath == null || this.currentPath.isDone()) {
         int $$6 = this.dragon.findClosestNode();
         int $$7 = $$6;
         if (this.dragon.getRandom().nextInt(8) == 0) {
            this.clockwise = !this.clockwise;
            $$7 = $$6 + 6;
         }

         if (this.clockwise) {
            $$7++;
         } else {
            $$7--;
         }

         if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() >= 0) {
            $$7 %= 12;
            if ($$7 < 0) {
               $$7 += 12;
            }
         } else {
            $$7 -= 12;
            $$7 &= 7;
            $$7 += 12;
         }

         this.currentPath = this.dragon.findPath($$6, $$7, null);
         if (this.currentPath != null) {
            this.currentPath.advance();
         }
      }

      this.navigateToNextPathNode();
   }

   private void strafePlayer(Player $$0) {
      this.dragon.getPhaseManager().setPhase(EnderDragonPhase.STRAFE_PLAYER);
      this.dragon.getPhaseManager().getPhase(EnderDragonPhase.STRAFE_PLAYER).setTarget($$0);
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
   public void onCrystalDestroyed(EndCrystal $$0, BlockPos $$1, DamageSource $$2, Player $$3) {
      if ($$3 != null && this.dragon.canAttack($$3)) {
         this.strafePlayer($$3);
      }
   }
}
