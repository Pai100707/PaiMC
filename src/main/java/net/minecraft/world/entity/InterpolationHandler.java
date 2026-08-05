package net.minecraft.world.entity;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class InterpolationHandler {
   public static final int DEFAULT_INTERPOLATION_STEPS = 3;
   private final net.minecraft.world.entity.Entity entity;
   private int interpolationSteps;
   private final net.minecraft.world.entity.InterpolationHandler.InterpolationData interpolationData = new net.minecraft.world.entity.InterpolationHandler.InterpolationData(
      0, Vec3.ZERO, 0.0F, 0.0F
   );
   
   private Vec3 previousTickPosition;
   
   private Vec2 previousTickRot;
   
   private final Consumer<net.minecraft.world.entity.InterpolationHandler> onInterpolationStart;

   public InterpolationHandler(net.minecraft.world.entity.Entity $$0) {
      this($$0, 3, null);
   }

   public InterpolationHandler(net.minecraft.world.entity.Entity $$0, int $$1) {
      this($$0, $$1, null);
   }

   public InterpolationHandler(net.minecraft.world.entity.Entity $$0, Consumer<net.minecraft.world.entity.InterpolationHandler> $$1) {
      this($$0, 3, $$1);
   }

   public InterpolationHandler(net.minecraft.world.entity.Entity $$0, int $$1, Consumer<net.minecraft.world.entity.InterpolationHandler> $$2) {
      this.interpolationSteps = $$1;
      this.entity = $$0;
      this.onInterpolationStart = $$2;
   }

   public Vec3 position() {
      return this.interpolationData.steps > 0 ? this.interpolationData.position : this.entity.position();
   }

   public float yRot() {
      return this.interpolationData.steps > 0 ? this.interpolationData.yRot : this.entity.getYRot();
   }

   public float xRot() {
      return this.interpolationData.steps > 0 ? this.interpolationData.xRot : this.entity.getXRot();
   }

   public void interpolateTo(Vec3 $$0, float $$1, float $$2) {
      if (this.interpolationSteps == 0) {
         this.entity.snapTo($$0, $$1, $$2);
         this.cancel();
      } else if (!this.hasActiveInterpolation()
         || !Objects.equals(this.yRot(), $$1)
         || !Objects.equals(this.xRot(), $$2)
         || !Objects.equals(this.position(), $$0)) {
         this.interpolationData.steps = this.interpolationSteps;
         this.interpolationData.position = $$0;
         this.interpolationData.yRot = $$1;
         this.interpolationData.xRot = $$2;
         this.previousTickPosition = this.entity.position();
         this.previousTickRot = new Vec2(this.entity.getXRot(), this.entity.getYRot());
         if (this.onInterpolationStart != null) {
            this.onInterpolationStart.accept(this);
         }
      }
   }

   public boolean hasActiveInterpolation() {
      return this.interpolationData.steps > 0;
   }

   public void setInterpolationLength(int $$0) {
      this.interpolationSteps = $$0;
   }

   public void interpolate() {
      if (!this.hasActiveInterpolation()) {
         this.cancel();
      } else {
         double $$0 = 1.0 / this.interpolationData.steps;
         if (this.previousTickPosition != null) {
            Vec3 $$1 = this.entity.position().subtract(this.previousTickPosition);
            if (this.entity.level().noCollision(this.entity, this.entity.makeBoundingBox(this.interpolationData.position.add($$1)))) {
               this.interpolationData.addDelta($$1);
            }
         }

         if (this.previousTickRot != null) {
            float $$2 = this.entity.getYRot() - this.previousTickRot.y;
            float $$3 = this.entity.getXRot() - this.previousTickRot.x;
            this.interpolationData.addRotation($$2, $$3);
         }

         double $$4 = Mth.lerp($$0, this.entity.getX(), this.interpolationData.position.x);
         double $$5 = Mth.lerp($$0, this.entity.getY(), this.interpolationData.position.y);
         double $$6 = Mth.lerp($$0, this.entity.getZ(), this.interpolationData.position.z);
         Vec3 $$7 = new Vec3($$4, $$5, $$6);
         float $$8 = (float)Mth.rotLerp($$0, this.entity.getYRot(), this.interpolationData.yRot);
         float $$9 = (float)Mth.lerp($$0, this.entity.getXRot(), this.interpolationData.xRot);
         this.entity.setPos($$7);
         this.entity.setRot($$8, $$9);
         this.interpolationData.decrease();
         this.previousTickPosition = $$7;
         this.previousTickRot = new Vec2(this.entity.getXRot(), this.entity.getYRot());
      }
   }

   public void cancel() {
      this.interpolationData.steps = 0;
      this.previousTickPosition = null;
      this.previousTickRot = null;
   }

   static class InterpolationData {
      protected int steps;
      Vec3 position;
      float yRot;
      float xRot;

      InterpolationData(int $$0, Vec3 $$1, float $$2, float $$3) {
         this.steps = $$0;
         this.position = $$1;
         this.yRot = $$2;
         this.xRot = $$3;
      }

      public void decrease() {
         this.steps--;
      }

      public void addDelta(Vec3 $$0) {
         this.position = this.position.add($$0);
      }

      public void addRotation(float $$0, float $$1) {
         this.yRot += $$0;
         this.xRot += $$1;
      }
   }
}
