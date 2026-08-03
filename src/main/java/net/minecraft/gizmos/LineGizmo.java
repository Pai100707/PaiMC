package net.minecraft.gizmos;

import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public record LineGizmo(Vec3 start, Vec3 end, int color, float width) implements net.minecraft.gizmos.Gizmo {
   public static final float DEFAULT_WIDTH = 3.0F;

   @Override
   public void emit(net.minecraft.gizmos.GizmoPrimitives $$0, float $$1) {
      $$0.addLine(this.start, this.end, ARGB.multiplyAlpha(this.color, $$1), this.width);
   }
}
