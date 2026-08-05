/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gizmos;

import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record CuboidGizmo(AABB aabb, GizmoStyle style, boolean coloredCornerStroke) implements Gizmo
{
    @Override
    public void emit(GizmoPrimitives $$0, float $$1) {
        double $$2 = this.aabb.minX;
        double $$3 = this.aabb.minY;
        double $$4 = this.aabb.minZ;
        double $$5 = this.aabb.maxX;
        double $$6 = this.aabb.maxY;
        double $$7 = this.aabb.maxZ;
        if (this.style.hasFill()) {
            int $$8 = this.style.multipliedFill($$1);
            $$0.addQuad(new Vec3($$5, $$3, $$4), new Vec3($$5, $$6, $$4), new Vec3($$5, $$6, $$7), new Vec3($$5, $$3, $$7), $$8);
            $$0.addQuad(new Vec3($$2, $$3, $$4), new Vec3($$2, $$3, $$7), new Vec3($$2, $$6, $$7), new Vec3($$2, $$6, $$4), $$8);
            $$0.addQuad(new Vec3($$2, $$3, $$4), new Vec3($$2, $$6, $$4), new Vec3($$5, $$6, $$4), new Vec3($$5, $$3, $$4), $$8);
            $$0.addQuad(new Vec3($$2, $$3, $$7), new Vec3($$5, $$3, $$7), new Vec3($$5, $$6, $$7), new Vec3($$2, $$6, $$7), $$8);
            $$0.addQuad(new Vec3($$2, $$6, $$4), new Vec3($$2, $$6, $$7), new Vec3($$5, $$6, $$7), new Vec3($$5, $$6, $$4), $$8);
            $$0.addQuad(new Vec3($$2, $$3, $$4), new Vec3($$5, $$3, $$4), new Vec3($$5, $$3, $$7), new Vec3($$2, $$3, $$7), $$8);
        }
        if (this.style.hasStroke()) {
            int $$9 = this.style.multipliedStroke($$1);
            $$0.addLine(new Vec3($$2, $$3, $$4), new Vec3($$5, $$3, $$4), this.coloredCornerStroke ? ARGB.multiply($$9, -34953) : $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$2, $$3, $$4), new Vec3($$2, $$6, $$4), this.coloredCornerStroke ? ARGB.multiply($$9, -8913033) : $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$2, $$3, $$4), new Vec3($$2, $$3, $$7), this.coloredCornerStroke ? ARGB.multiply($$9, -8947713) : $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$5, $$3, $$4), new Vec3($$5, $$6, $$4), $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$5, $$6, $$4), new Vec3($$2, $$6, $$4), $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$2, $$6, $$4), new Vec3($$2, $$6, $$7), $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$2, $$6, $$7), new Vec3($$2, $$3, $$7), $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$2, $$3, $$7), new Vec3($$5, $$3, $$7), $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$5, $$3, $$7), new Vec3($$5, $$3, $$4), $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$2, $$6, $$7), new Vec3($$5, $$6, $$7), $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$5, $$3, $$7), new Vec3($$5, $$6, $$7), $$9, this.style.strokeWidth());
            $$0.addLine(new Vec3($$5, $$6, $$4), new Vec3($$5, $$6, $$7), $$9, this.style.strokeWidth());
        }
    }
}

