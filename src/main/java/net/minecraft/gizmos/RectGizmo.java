package net.minecraft.gizmos;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public record RectGizmo(Vec3 a, Vec3 b, Vec3 c, Vec3 d, net.minecraft.gizmos.GizmoStyle style) implements net.minecraft.gizmos.Gizmo {
   public static net.minecraft.gizmos.RectGizmo fromCuboidFace(Vec3 $$0, Vec3 $$1, Direction $$2, net.minecraft.gizmos.GizmoStyle $$3) {
      return switch ($$2) {
         case DOWN -> new net.minecraft.gizmos.RectGizmo(
            new Vec3($$0.x, $$0.y, $$0.z), new Vec3($$1.x, $$0.y, $$0.z), new Vec3($$1.x, $$0.y, $$1.z), new Vec3($$0.x, $$0.y, $$1.z), $$3
         );
         case UP -> new net.minecraft.gizmos.RectGizmo(
            new Vec3($$0.x, $$1.y, $$0.z), new Vec3($$0.x, $$1.y, $$1.z), new Vec3($$1.x, $$1.y, $$1.z), new Vec3($$1.x, $$1.y, $$0.z), $$3
         );
         case NORTH -> new net.minecraft.gizmos.RectGizmo(
            new Vec3($$0.x, $$0.y, $$0.z), new Vec3($$0.x, $$1.y, $$0.z), new Vec3($$1.x, $$1.y, $$0.z), new Vec3($$1.x, $$0.y, $$0.z), $$3
         );
         case SOUTH -> new net.minecraft.gizmos.RectGizmo(
            new Vec3($$0.x, $$0.y, $$1.z), new Vec3($$1.x, $$0.y, $$1.z), new Vec3($$1.x, $$1.y, $$1.z), new Vec3($$0.x, $$1.y, $$1.z), $$3
         );
         case WEST -> new net.minecraft.gizmos.RectGizmo(
            new Vec3($$0.x, $$0.y, $$0.z), new Vec3($$0.x, $$0.y, $$1.z), new Vec3($$0.x, $$1.y, $$1.z), new Vec3($$0.x, $$1.y, $$0.z), $$3
         );
         case EAST -> new net.minecraft.gizmos.RectGizmo(
            new Vec3($$1.x, $$0.y, $$0.z), new Vec3($$1.x, $$1.y, $$0.z), new Vec3($$1.x, $$1.y, $$1.z), new Vec3($$1.x, $$0.y, $$1.z), $$3
         );
         default -> throw new MatchException(null, null);
      };
   }

   @Override
   public void emit(net.minecraft.gizmos.GizmoPrimitives $$0, float $$1) {
      if (this.style.hasFill()) {
         int $$2 = this.style.multipliedFill($$1);
         $$0.addQuad(this.a, this.b, this.c, this.d, $$2);
      }

      if (this.style.hasStroke()) {
         int $$3 = this.style.multipliedStroke($$1);
         $$0.addLine(this.a, this.b, $$3, this.style.strokeWidth());
         $$0.addLine(this.b, this.c, $$3, this.style.strokeWidth());
         $$0.addLine(this.c, this.d, $$3, this.style.strokeWidth());
         $$0.addLine(this.d, this.a, $$3, this.style.strokeWidth());
      }
   }
}
