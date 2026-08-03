package net.minecraft.world.entity;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record EntityDimensions(float width, float height, float eyeHeight, net.minecraft.world.entity.EntityAttachments attachments, boolean fixed) {
   private EntityDimensions(float $$0, float $$1, boolean $$2) {
      this($$0, $$1, defaultEyeHeight($$1), net.minecraft.world.entity.EntityAttachments.createDefault($$0, $$1), $$2);
   }

   private static float defaultEyeHeight(float $$0) {
      return $$0 * 0.85F;
   }

   public AABB makeBoundingBox(Vec3 $$0) {
      return this.makeBoundingBox($$0.x, $$0.y, $$0.z);
   }

   public AABB makeBoundingBox(double $$0, double $$1, double $$2) {
      float $$3 = this.width / 2.0F;
      float $$4 = this.height;
      return new AABB($$0 - $$3, $$1, $$2 - $$3, $$0 + $$3, $$1 + $$4, $$2 + $$3);
   }

   public net.minecraft.world.entity.EntityDimensions scale(float $$0) {
      return this.scale($$0, $$0);
   }

   public net.minecraft.world.entity.EntityDimensions scale(float $$0, float $$1) {
      return !this.fixed && ($$0 != 1.0F || $$1 != 1.0F)
         ? new net.minecraft.world.entity.EntityDimensions(
            this.width * $$0, this.height * $$1, this.eyeHeight * $$1, this.attachments.scale($$0, $$1, $$0), false
         )
         : this;
   }

   public static net.minecraft.world.entity.EntityDimensions scalable(float $$0, float $$1) {
      return new net.minecraft.world.entity.EntityDimensions($$0, $$1, false);
   }

   public static net.minecraft.world.entity.EntityDimensions fixed(float $$0, float $$1) {
      return new net.minecraft.world.entity.EntityDimensions($$0, $$1, true);
   }

   public net.minecraft.world.entity.EntityDimensions withEyeHeight(float $$0) {
      return new net.minecraft.world.entity.EntityDimensions(this.width, this.height, $$0, this.attachments, this.fixed);
   }

   public net.minecraft.world.entity.EntityDimensions withAttachments(net.minecraft.world.entity.EntityAttachments.Builder $$0) {
      return new net.minecraft.world.entity.EntityDimensions(this.width, this.height, this.eyeHeight, $$0.build(this.width, this.height), this.fixed);
   }
}
