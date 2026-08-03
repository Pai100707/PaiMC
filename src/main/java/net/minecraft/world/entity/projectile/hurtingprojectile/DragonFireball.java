package net.minecraft.world.entity.projectile.hurtingprojectile;

import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class DragonFireball extends AbstractHurtingProjectile {
   public static final float SPLASH_RANGE = 4.0F;

   public DragonFireball(net.minecraft.world.entity.EntityType<? extends DragonFireball> $$0, Level $$1) {
      super($$0, $$1);
   }

   public DragonFireball(Level $$0, net.minecraft.world.entity.LivingEntity $$1, Vec3 $$2) {
      super(net.minecraft.world.entity.EntityType.DRAGON_FIREBALL, $$1, $$2, $$0);
   }

   @Override
   protected void onHit(HitResult $$0) {
      super.onHit($$0);
      if ($$0.getType() != Type.ENTITY || !this.ownedBy(((EntityHitResult)$$0).getEntity())) {
         if (!this.level().isClientSide()) {
            List<net.minecraft.world.entity.LivingEntity> $$1 = this.level()
               .getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, this.getBoundingBox().inflate(4.0, 2.0, 4.0));
            net.minecraft.world.entity.AreaEffectCloud $$2 = new net.minecraft.world.entity.AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
            net.minecraft.world.entity.Entity $$3 = this.getOwner();
            if ($$3 instanceof net.minecraft.world.entity.LivingEntity) {
               $$2.setOwner((net.minecraft.world.entity.LivingEntity)$$3);
            }

            $$2.setCustomParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F));
            $$2.setRadius(3.0F);
            $$2.setDuration(600);
            $$2.setRadiusPerTick((7.0F - $$2.getRadius()) / $$2.getDuration());
            $$2.setPotionDurationScale(0.25F);
            $$2.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 1));
            if (!$$1.isEmpty()) {
               for (net.minecraft.world.entity.LivingEntity $$4 : $$1) {
                  double $$5 = this.distanceToSqr($$4);
                  if ($$5 < 16.0) {
                     $$2.setPos($$4.getX(), $$4.getY(), $$4.getZ());
                     break;
                  }
               }
            }

            this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
            this.level().addFreshEntity($$2);
            this.discard();
         }
      }
   }

   @Override
   protected ParticleOptions getTrailParticle() {
      return PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F);
   }

   @Override
   protected boolean shouldBurn() {
      return false;
   }
}
