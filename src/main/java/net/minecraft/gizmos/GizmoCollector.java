package net.minecraft.gizmos;

public interface GizmoCollector {
   net.minecraft.gizmos.GizmoProperties IGNORED = new net.minecraft.gizmos.GizmoProperties() {
      @Override
      public net.minecraft.gizmos.GizmoProperties setAlwaysOnTop() {
         return this;
      }

      @Override
      public net.minecraft.gizmos.GizmoProperties persistForMillis(int $$0) {
         return this;
      }

      @Override
      public net.minecraft.gizmos.GizmoProperties fadeOut() {
         return this;
      }
   };
   net.minecraft.gizmos.GizmoCollector NOOP = $$0 -> IGNORED;

   net.minecraft.gizmos.GizmoProperties add(net.minecraft.gizmos.Gizmo var1);
}
