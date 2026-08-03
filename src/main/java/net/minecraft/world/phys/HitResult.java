package net.minecraft.world.phys;

import net.minecraft.world.entity.Entity;

public abstract class HitResult {
   protected final net.minecraft.world.phys.Vec3 location;

   protected HitResult(net.minecraft.world.phys.Vec3 $$0) {
      this.location = $$0;
   }

   public double distanceTo(Entity $$0) {
      double $$1 = this.location.x - $$0.getX();
      double $$2 = this.location.y - $$0.getY();
      double $$3 = this.location.z - $$0.getZ();
      return $$1 * $$1 + $$2 * $$2 + $$3 * $$3;
   }

   public abstract net.minecraft.world.phys.HitResult.Type getType();

   public net.minecraft.world.phys.Vec3 getLocation() {
      return this.location;
   }

   public static enum Type {
      MISS,
      BLOCK,
      ENTITY;
   }
}
