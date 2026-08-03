package net.minecraft.gizmos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class SimpleGizmoCollector implements net.minecraft.gizmos.GizmoCollector {
   private final List<net.minecraft.gizmos.SimpleGizmoCollector.GizmoInstance> gizmos = new ArrayList<>();
   private final List<net.minecraft.gizmos.SimpleGizmoCollector.GizmoInstance> temporaryGizmos = new ArrayList<>();

   @Override
   public net.minecraft.gizmos.GizmoProperties add(net.minecraft.gizmos.Gizmo $$0) {
      net.minecraft.gizmos.SimpleGizmoCollector.GizmoInstance $$1 = new net.minecraft.gizmos.SimpleGizmoCollector.GizmoInstance($$0);
      this.gizmos.add($$1);
      return $$1;
   }

   public List<net.minecraft.gizmos.SimpleGizmoCollector.GizmoInstance> drainGizmos() {
      ArrayList<net.minecraft.gizmos.SimpleGizmoCollector.GizmoInstance> $$0 = new ArrayList<>(this.gizmos);
      $$0.addAll(this.temporaryGizmos);
      long $$1 = Util.getMillis();
      this.gizmos.removeIf($$1x -> $$1x.getExpireTimeMillis() < $$1);
      this.temporaryGizmos.clear();
      return $$0;
   }

   public List<net.minecraft.gizmos.SimpleGizmoCollector.GizmoInstance> getGizmos() {
      return this.gizmos;
   }

   public void addTemporaryGizmos(Collection<net.minecraft.gizmos.SimpleGizmoCollector.GizmoInstance> $$0) {
      this.temporaryGizmos.addAll($$0);
   }

   public static class GizmoInstance implements net.minecraft.gizmos.GizmoProperties {
      private final net.minecraft.gizmos.Gizmo gizmo;
      private boolean isAlwaysOnTop;
      private long startTimeMillis;
      private long expireTimeMillis;
      private boolean shouldFadeOut;

      GizmoInstance(net.minecraft.gizmos.Gizmo $$0) {
         this.gizmo = $$0;
      }

      @Override
      public net.minecraft.gizmos.GizmoProperties setAlwaysOnTop() {
         this.isAlwaysOnTop = true;
         return this;
      }

      @Override
      public net.minecraft.gizmos.GizmoProperties persistForMillis(int $$0) {
         this.startTimeMillis = Util.getMillis();
         this.expireTimeMillis = this.startTimeMillis + $$0;
         return this;
      }

      @Override
      public net.minecraft.gizmos.GizmoProperties fadeOut() {
         this.shouldFadeOut = true;
         return this;
      }

      public float getAlphaMultiplier(long $$0) {
         if (this.shouldFadeOut) {
            long $$1 = this.expireTimeMillis - this.startTimeMillis;
            long $$2 = $$0 - this.startTimeMillis;
            return 1.0F - Mth.clamp((float)$$2 / (float)$$1, 0.0F, 1.0F);
         } else {
            return 1.0F;
         }
      }

      public boolean isAlwaysOnTop() {
         return this.isAlwaysOnTop;
      }

      public long getExpireTimeMillis() {
         return this.expireTimeMillis;
      }

      public net.minecraft.gizmos.Gizmo gizmo() {
         return this.gizmo;
      }
   }
}
