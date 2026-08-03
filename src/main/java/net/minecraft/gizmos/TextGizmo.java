package net.minecraft.gizmos;

import java.util.OptionalDouble;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public record TextGizmo(Vec3 pos, String text, net.minecraft.gizmos.TextGizmo.Style style) implements net.minecraft.gizmos.Gizmo {
   @Override
   public void emit(net.minecraft.gizmos.GizmoPrimitives $$0, float $$1) {
      net.minecraft.gizmos.TextGizmo.Style $$2;
      if ($$1 < 1.0F) {
         $$2 = new net.minecraft.gizmos.TextGizmo.Style(ARGB.multiplyAlpha(this.style.color, $$1), this.style.scale, this.style.adjustLeft);
      } else {
         $$2 = this.style;
      }

      $$0.addText(this.pos, this.text, $$2);
   }

   public record Style(int color, float scale, OptionalDouble adjustLeft) {
      public static final float DEFAULT_SCALE = 0.32F;

      public static net.minecraft.gizmos.TextGizmo.Style whiteAndCentered() {
         return new net.minecraft.gizmos.TextGizmo.Style(-1, 0.32F, OptionalDouble.empty());
      }

      public static net.minecraft.gizmos.TextGizmo.Style forColorAndCentered(int $$0) {
         return new net.minecraft.gizmos.TextGizmo.Style($$0, 0.32F, OptionalDouble.empty());
      }

      public static net.minecraft.gizmos.TextGizmo.Style forColor(int $$0) {
         return new net.minecraft.gizmos.TextGizmo.Style($$0, 0.32F, OptionalDouble.of(0.0));
      }

      public net.minecraft.gizmos.TextGizmo.Style withScale(float $$0) {
         return new net.minecraft.gizmos.TextGizmo.Style(this.color, $$0, this.adjustLeft);
      }

      public net.minecraft.gizmos.TextGizmo.Style withLeftAlignment(float $$0) {
         return new net.minecraft.gizmos.TextGizmo.Style(this.color, this.scale, OptionalDouble.of($$0));
      }
   }
}
