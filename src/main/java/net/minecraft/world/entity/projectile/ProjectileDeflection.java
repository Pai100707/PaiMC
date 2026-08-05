package net.minecraft.world.entity.projectile;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface ProjectileDeflection {
   ProjectileDeflection NONE = ($$0, $$1, $$2) -> {};
   ProjectileDeflection REVERSE = ($$0, $$1, $$2) -> {
      float $$3 = 170.0F + $$2.nextFloat() * 20.0F;
      $$0.setDeltaMovement($$0.getDeltaMovement().scale(-0.5));
      $$0.setYRot($$0.getYRot() + $$3);
      $$0.yRotO += $$3;
      $$0.needsSync = true;
   };
   ProjectileDeflection AIM_DEFLECT = ($$0, $$1, $$2) -> {
      if ($$1 != null) {
         Vec3 $$3 = $$1.getLookAngle();
         $$0.setDeltaMovement($$3);
         $$0.needsSync = true;
      }
   };
   ProjectileDeflection MOMENTUM_DEFLECT = ($$0, $$1, $$2) -> {
      if ($$1 != null) {
         Vec3 $$3 = $$1.getDeltaMovement().normalize();
         $$0.setDeltaMovement($$3);
         $$0.needsSync = true;
      }
   };

   void deflect(Projectile var1, net.minecraft.world.entity.Entity var2, RandomSource var3);
}
