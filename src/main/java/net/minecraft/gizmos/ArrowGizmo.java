package net.minecraft.gizmos;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record ArrowGizmo(Vec3 start, Vec3 end, int color, float width) implements net.minecraft.gizmos.Gizmo {
   public static final float DEFAULT_WIDTH = 2.5F;

   @Override
   public void emit(net.minecraft.gizmos.GizmoPrimitives $$0, float $$1) {
      int $$2 = ARGB.multiplyAlpha(this.color, $$1);
      $$0.addLine(this.start, this.end, $$2, this.width);
      Quaternionf $$3 = new Quaternionf().rotationTo(new Vector3f(1.0F, 0.0F, 0.0F), this.end.subtract(this.start).toVector3f().normalize());
      float $$4 = (float)Mth.clamp(this.end.distanceTo(this.start) * 0.1F, 0.1F, 1.0);
      Vector3f[] $$5 = new Vector3f[]{
         $$3.transform(-$$4, $$4, 0.0F, new Vector3f()),
         $$3.transform(-$$4, 0.0F, $$4, new Vector3f()),
         $$3.transform(-$$4, -$$4, 0.0F, new Vector3f()),
         $$3.transform(-$$4, 0.0F, -$$4, new Vector3f())
      };

      for (Vector3f $$6 : $$5) {
         $$0.addLine(this.end.add($$6.x, $$6.y, $$6.z), this.end, $$2, this.width);
      }
   }
}
