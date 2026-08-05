package net.minecraft.gizmos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Gizmos {
   static final ThreadLocal<net.minecraft.gizmos.GizmoCollector> collector = new ThreadLocal<>();

   private Gizmos() {
   }

   public static net.minecraft.gizmos.Gizmos.TemporaryCollection withCollector(net.minecraft.gizmos.GizmoCollector $$0) {
      net.minecraft.gizmos.Gizmos.TemporaryCollection $$1 = new net.minecraft.gizmos.Gizmos.TemporaryCollection();
      collector.set($$0);
      return $$1;
   }

   public static net.minecraft.gizmos.GizmoProperties addGizmo(net.minecraft.gizmos.Gizmo $$0) {
      net.minecraft.gizmos.GizmoCollector $$1 = collector.get();
      if ($$1 == null) {
         throw new IllegalStateException("Gizmos cannot be created here! No GizmoCollector has been registered.");
      } else {
         return $$1.add($$0);
      }
   }

   public static net.minecraft.gizmos.GizmoProperties cuboid(AABB $$0, net.minecraft.gizmos.GizmoStyle $$1) {
      return cuboid($$0, $$1, false);
   }

   public static net.minecraft.gizmos.GizmoProperties cuboid(AABB $$0, net.minecraft.gizmos.GizmoStyle $$1, boolean $$2) {
      return addGizmo(new net.minecraft.gizmos.CuboidGizmo($$0, $$1, $$2));
   }

   public static net.minecraft.gizmos.GizmoProperties cuboid(BlockPos $$0, net.minecraft.gizmos.GizmoStyle $$1) {
      return cuboid(new AABB($$0), $$1);
   }

   public static net.minecraft.gizmos.GizmoProperties cuboid(BlockPos $$0, float $$1, net.minecraft.gizmos.GizmoStyle $$2) {
      return cuboid(new AABB($$0).inflate($$1), $$2);
   }

   public static net.minecraft.gizmos.GizmoProperties circle(Vec3 $$0, float $$1, net.minecraft.gizmos.GizmoStyle $$2) {
      return addGizmo(new net.minecraft.gizmos.CircleGizmo($$0, $$1, $$2));
   }

   public static net.minecraft.gizmos.GizmoProperties line(Vec3 $$0, Vec3 $$1, int $$2) {
      return addGizmo(new net.minecraft.gizmos.LineGizmo($$0, $$1, $$2, 3.0F));
   }

   public static net.minecraft.gizmos.GizmoProperties line(Vec3 $$0, Vec3 $$1, int $$2, float $$3) {
      return addGizmo(new net.minecraft.gizmos.LineGizmo($$0, $$1, $$2, $$3));
   }

   public static net.minecraft.gizmos.GizmoProperties arrow(Vec3 $$0, Vec3 $$1, int $$2) {
      return addGizmo(new net.minecraft.gizmos.ArrowGizmo($$0, $$1, $$2, 2.5F));
   }

   public static net.minecraft.gizmos.GizmoProperties arrow(Vec3 $$0, Vec3 $$1, int $$2, float $$3) {
      return addGizmo(new net.minecraft.gizmos.ArrowGizmo($$0, $$1, $$2, $$3));
   }

   public static net.minecraft.gizmos.GizmoProperties rect(Vec3 $$0, Vec3 $$1, Direction $$2, net.minecraft.gizmos.GizmoStyle $$3) {
      return addGizmo(net.minecraft.gizmos.RectGizmo.fromCuboidFace($$0, $$1, $$2, $$3));
   }

   public static net.minecraft.gizmos.GizmoProperties rect(Vec3 $$0, Vec3 $$1, Vec3 $$2, Vec3 $$3, net.minecraft.gizmos.GizmoStyle $$4) {
      return addGizmo(new net.minecraft.gizmos.RectGizmo($$0, $$1, $$2, $$3, $$4));
   }

   public static net.minecraft.gizmos.GizmoProperties point(Vec3 $$0, int $$1, float $$2) {
      return addGizmo(new net.minecraft.gizmos.PointGizmo($$0, $$1, $$2));
   }

   public static net.minecraft.gizmos.GizmoProperties billboardTextOverBlock(String $$0, BlockPos $$1, int $$2, int $$3, float $$4) {
      double $$5 = 1.3;
      double $$6 = 0.2;
      net.minecraft.gizmos.GizmoProperties $$7 = billboardText(
         $$0, Vec3.atLowerCornerWithOffset($$1, 0.5, 1.3 + $$2 * 0.2, 0.5), net.minecraft.gizmos.TextGizmo.Style.forColorAndCentered($$3).withScale($$4)
      );
      $$7.setAlwaysOnTop();
      return $$7;
   }

   public static net.minecraft.gizmos.GizmoProperties billboardTextOverMob(Entity $$0, int $$1, String $$2, int $$3, float $$4) {
      double $$5 = 2.4;
      double $$6 = 0.25;
      double $$7 = $$0.getBlockX() + 0.5;
      double $$8 = $$0.getY() + 2.4 + $$1 * 0.25;
      double $$9 = $$0.getBlockZ() + 0.5;
      float $$10 = 0.5F;
      net.minecraft.gizmos.GizmoProperties $$11 = billboardText(
         $$2, new Vec3($$7, $$8, $$9), net.minecraft.gizmos.TextGizmo.Style.forColor($$3).withScale($$4).withLeftAlignment(0.5F)
      );
      $$11.setAlwaysOnTop();
      return $$11;
   }

   public static net.minecraft.gizmos.GizmoProperties billboardText(String $$0, Vec3 $$1, net.minecraft.gizmos.TextGizmo.Style $$2) {
      return addGizmo(new net.minecraft.gizmos.TextGizmo($$1, $$0, $$2));
   }

   public static class TemporaryCollection implements AutoCloseable {
      
      private final net.minecraft.gizmos.GizmoCollector old = net.minecraft.gizmos.Gizmos.collector.get();
      private boolean closed;

      TemporaryCollection() {
      }

      @Override
      public void close() {
         if (!this.closed) {
            this.closed = true;
            net.minecraft.gizmos.Gizmos.collector.set(this.old);
         }
      }
   }
}
