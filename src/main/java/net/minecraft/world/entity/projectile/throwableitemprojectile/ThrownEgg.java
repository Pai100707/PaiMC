package net.minecraft.world.entity.projectile.throwableitemprojectile;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownEgg extends ThrowableItemProjectile {
   private static final net.minecraft.world.entity.EntityDimensions ZERO_SIZED_DIMENSIONS = net.minecraft.world.entity.EntityDimensions.fixed(0.0F, 0.0F);

   public ThrownEgg(net.minecraft.world.entity.EntityType<? extends ThrownEgg> $$0, Level $$1) {
      super($$0, $$1);
   }

   public ThrownEgg(Level $$0, net.minecraft.world.entity.LivingEntity $$1, ItemStack $$2) {
      super(net.minecraft.world.entity.EntityType.EGG, $$1, $$0, $$2);
   }

   public ThrownEgg(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
      super(net.minecraft.world.entity.EntityType.EGG, $$1, $$2, $$3, $$0, $$4);
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 3) {
         double $$1 = 0.08;

         for (int $$2 = 0; $$2 < 8; $$2++) {
            this.level()
               .addParticle(
                  new ItemParticleOption(ParticleTypes.ITEM, this.getItem()),
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  (this.random.nextFloat() - 0.5) * 0.08,
                  (this.random.nextFloat() - 0.5) * 0.08,
                  (this.random.nextFloat() - 0.5) * 0.08
               );
         }
      }
   }

   @Override
   protected void onHitEntity(EntityHitResult $$0) {
      super.onHitEntity($$0);
      $$0.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
   }

   @Override
   protected void onHit(HitResult $$0) {
      super.onHit($$0);
      if (!this.level().isClientSide()) {
         if (this.random.nextInt(8) == 0) {
            int $$1 = 1;
            if (this.random.nextInt(32) == 0) {
               $$1 = 4;
            }

            for (int $$2 = 0; $$2 < $$1; $$2++) {
               Chicken $$3 = net.minecraft.world.entity.EntityType.CHICKEN.create(this.level(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
               if ($$3 != null) {
                  $$3.setAge(-24000);
                  $$3.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                  Optional.ofNullable((EitherHolder)this.getItem().get(DataComponents.CHICKEN_VARIANT))
                     .flatMap($$0x -> $$0x.unwrap(this.registryAccess()))
                     .ifPresent($$3::setVariant);
                  if (!$$3.fudgePositionAfterSizeChange(ZERO_SIZED_DIMENSIONS)) {
                     break;
                  }

                  this.level().addFreshEntity($$3);
               }
            }
         }

         this.level().broadcastEntityEvent(this, (byte)3);
         this.discard();
      }
   }

   @Override
   protected Item getDefaultItem() {
      return Items.EGG;
   }
}
