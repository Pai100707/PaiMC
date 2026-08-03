package com.mojang.math;

import org.joml.Quaternionf;
import org.joml.Vector3f;

@FunctionalInterface
public interface Axis {
   Axis XN = $$0 -> new Quaternionf().rotationX(-$$0);
   Axis XP = $$0 -> new Quaternionf().rotationX($$0);
   Axis YN = $$0 -> new Quaternionf().rotationY(-$$0);
   Axis YP = $$0 -> new Quaternionf().rotationY($$0);
   Axis ZN = $$0 -> new Quaternionf().rotationZ(-$$0);
   Axis ZP = $$0 -> new Quaternionf().rotationZ($$0);

   static Axis of(Vector3f $$0) {
      return $$1 -> new Quaternionf().rotationAxis($$1, $$0);
   }

   Quaternionf rotation(float var1);

   default Quaternionf rotationDegrees(float $$0) {
      return this.rotation($$0 * (float) (Math.PI / 180.0));
   }
}
