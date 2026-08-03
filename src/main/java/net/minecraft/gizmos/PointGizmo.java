package net.minecraft.gizmos;

import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public record PointGizmo(Vec3 pos, int color, float size) implements net.minecraft.gizmos.Gizmo {
   @Override
   public void emit(net.minecraft.gizmos.GizmoPrimitives $$0, float $$1) {
      $$0.addPoint(this.pos, ARGB.multiplyAlpha(this.color, $$1), this.size);
   }
}
