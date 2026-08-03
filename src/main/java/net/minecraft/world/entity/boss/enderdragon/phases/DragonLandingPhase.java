package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DragonLandingPhase extends AbstractDragonPhaseInstance {
   @Nullable
   private Vec3 targetLocation;

   public DragonLandingPhase(EnderDragon $$0) {
      super($$0);
   }

   @Override
   public void doClientTick() {
      Vec3 $$0 = this.dragon.getHeadLookVector(1.0F).normalize();
      $$0.yRot((float) (-Math.PI / 4));
      double $$1 = this.dragon.head.getX();
      double $$2 = this.dragon.head.getY(0.5);
      double $$3 = this.dragon.head.getZ();

      for (int $$4 = 0; $$4 < 8; $$4++) {
         RandomSource $$5 = this.dragon.getRandom();
         double $$6 = $$1 + $$5.nextGaussian() / 2.0;
         double $$7 = $$2 + $$5.nextGaussian() / 2.0;
         double $$8 = $$3 + $$5.nextGaussian() / 2.0;
         Vec3 $$9 = this.dragon.getDeltaMovement();
         this.dragon
            .level()
            .addParticle(
               PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F),
               $$6,
               $$7,
               $$8,
               -$$0.x * 0.08F + $$9.x,
               -$$0.y * 0.3F + $$9.y,
               -$$0.z * 0.08F + $$9.z
            );
         $$0.yRot((float) (Math.PI / 16));
      }
   }

   @Override
   public void doServerTick(ServerLevel $$0) {
      if (this.targetLocation == null) {
         this.targetLocation = Vec3.atBottomCenterOf(
            $$0.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.dragon.getFightOrigin()))
         );
      }

      if (this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()) < 1.0) {
         this.dragon.getPhaseManager().getPhase(EnderDragonPhase.SITTING_FLAMING).resetFlameCount();
         this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_SCANNING);
      }
   }

   @Override
   public float getFlySpeed() {
      return 1.5F;
   }

   @Override
   public float getTurnSpeed() {
      float $$0 = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0F;
      float $$1 = Math.min($$0, 40.0F);
      return $$1 / $$0;
   }

   @Override
   public void begin() {
      this.targetLocation = null;
   }

   @Nullable
   @Override
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   @Override
   public EnderDragonPhase<DragonLandingPhase> getPhase() {
      return EnderDragonPhase.LANDING;
   }
}
