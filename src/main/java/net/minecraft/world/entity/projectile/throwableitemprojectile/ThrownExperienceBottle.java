package net.minecraft.world.entity.projectile.throwableitemprojectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownExperienceBottle extends ThrowableItemProjectile {
   public ThrownExperienceBottle(net.minecraft.world.entity.EntityType<? extends ThrownExperienceBottle> $$0, Level $$1) {
      super($$0, $$1);
   }

   public ThrownExperienceBottle(Level $$0, net.minecraft.world.entity.LivingEntity $$1, ItemStack $$2) {
      super(net.minecraft.world.entity.EntityType.EXPERIENCE_BOTTLE, $$1, $$0, $$2);
   }

   public ThrownExperienceBottle(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
      super(net.minecraft.world.entity.EntityType.EXPERIENCE_BOTTLE, $$1, $$2, $$3, $$0, $$4);
   }

   @Override
   protected Item getDefaultItem() {
      return Items.EXPERIENCE_BOTTLE;
   }

   @Override
   protected double getDefaultGravity() {
      return 0.07;
   }

   @Override
   protected void onHit(HitResult $$0) {
      super.onHit($$0);
      if (this.level() instanceof ServerLevel $$1) {
         $$1.levelEvent(2002, this.blockPosition(), -13083194);
         int $$2 = 3 + $$1.random.nextInt(5) + $$1.random.nextInt(5);
         if ($$0 instanceof BlockHitResult $$3) {
            Vec3 $$4 = $$3.getDirection().getUnitVec3();
            net.minecraft.world.entity.ExperienceOrb.awardWithDirection($$1, $$0.getLocation(), $$4, $$2);
         } else {
            net.minecraft.world.entity.ExperienceOrb.awardWithDirection($$1, $$0.getLocation(), this.getDeltaMovement().scale(-1.0), $$2);
         }

         this.discard();
      }
   }
}
