/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.gizmos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.ArrowGizmo;
import net.minecraft.gizmos.CircleGizmo;
import net.minecraft.gizmos.CuboidGizmo;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoCollector;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.LineGizmo;
import net.minecraft.gizmos.PointGizmo;
import net.minecraft.gizmos.RectGizmo;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Gizmos {
    static final ThreadLocal<@Nullable GizmoCollector> collector = new ThreadLocal();

    private Gizmos() {
    }

    public static TemporaryCollection withCollector(GizmoCollector $$0) {
        TemporaryCollection $$1 = new TemporaryCollection();
        collector.set($$0);
        return $$1;
    }

    public static GizmoProperties addGizmo(Gizmo $$0) {
        GizmoCollector $$1 = collector.get();
        if ($$1 == null) {
            throw new IllegalStateException("Gizmos cannot be created here! No GizmoCollector has been registered.");
        }
        return $$1.add($$0);
    }

    public static GizmoProperties cuboid(AABB $$0, GizmoStyle $$1) {
        return Gizmos.cuboid($$0, $$1, false);
    }

    public static GizmoProperties cuboid(AABB $$0, GizmoStyle $$1, boolean $$2) {
        return Gizmos.addGizmo(new CuboidGizmo($$0, $$1, $$2));
    }

    public static GizmoProperties cuboid(BlockPos $$0, GizmoStyle $$1) {
        return Gizmos.cuboid(new AABB($$0), $$1);
    }

    public static GizmoProperties cuboid(BlockPos $$0, float $$1, GizmoStyle $$2) {
        return Gizmos.cuboid(new AABB($$0).inflate($$1), $$2);
    }

    public static GizmoProperties circle(Vec3 $$0, float $$1, GizmoStyle $$2) {
        return Gizmos.addGizmo(new CircleGizmo($$0, $$1, $$2));
    }

    public static GizmoProperties line(Vec3 $$0, Vec3 $$1, int $$2) {
        return Gizmos.addGizmo(new LineGizmo($$0, $$1, $$2, 3.0f));
    }

    public static GizmoProperties line(Vec3 $$0, Vec3 $$1, int $$2, float $$3) {
        return Gizmos.addGizmo(new LineGizmo($$0, $$1, $$2, $$3));
    }

    public static GizmoProperties arrow(Vec3 $$0, Vec3 $$1, int $$2) {
        return Gizmos.addGizmo(new ArrowGizmo($$0, $$1, $$2, 2.5f));
    }

    public static GizmoProperties arrow(Vec3 $$0, Vec3 $$1, int $$2, float $$3) {
        return Gizmos.addGizmo(new ArrowGizmo($$0, $$1, $$2, $$3));
    }

    public static GizmoProperties rect(Vec3 $$0, Vec3 $$1, Direction $$2, GizmoStyle $$3) {
        return Gizmos.addGizmo(RectGizmo.fromCuboidFace($$0, $$1, $$2, $$3));
    }

    public static GizmoProperties rect(Vec3 $$0, Vec3 $$1, Vec3 $$2, Vec3 $$3, GizmoStyle $$4) {
        return Gizmos.addGizmo(new RectGizmo($$0, $$1, $$2, $$3, $$4));
    }

    public static GizmoProperties point(Vec3 $$0, int $$1, float $$2) {
        return Gizmos.addGizmo(new PointGizmo($$0, $$1, $$2));
    }

    public static GizmoProperties billboardTextOverBlock(String $$0, BlockPos $$1, int $$2, int $$3, float $$4) {
        double $$5 = 1.3;
        double $$6 = 0.2;
        GizmoProperties $$7 = Gizmos.billboardText($$0, Vec3.atLowerCornerWithOffset($$1, 0.5, 1.3 + (double)$$2 * 0.2, 0.5), TextGizmo.Style.forColorAndCentered($$3).withScale($$4));
        $$7.setAlwaysOnTop();
        return $$7;
    }

    public static GizmoProperties billboardTextOverMob(Entity $$0, int $$1, String $$2, int $$3, float $$4) {
        double $$5 = 2.4;
        double $$6 = 0.25;
        double $$7 = (double)$$0.getBlockX() + 0.5;
        double $$8 = $$0.getY() + 2.4 + (double)$$1 * 0.25;
        double $$9 = (double)$$0.getBlockZ() + 0.5;
        float $$10 = 0.5f;
        GizmoProperties $$11 = Gizmos.billboardText($$2, new Vec3($$7, $$8, $$9), TextGizmo.Style.forColor($$3).withScale($$4).withLeftAlignment(0.5f));
        $$11.setAlwaysOnTop();
        return $$11;
    }

    public static GizmoProperties billboardText(String $$0, Vec3 $$1, TextGizmo.Style $$2) {
        return Gizmos.addGizmo(new TextGizmo($$1, $$0, $$2));
    }

    public static class TemporaryCollection
    implements AutoCloseable {
        private final @Nullable GizmoCollector old = collector.get();
        private boolean closed;

        TemporaryCollection() {
        }

        @Override
        public void close() {
            if (!this.closed) {
                this.closed = true;
                collector.set(this.old);
            }
        }
    }
}

