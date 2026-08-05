/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gizmos;

import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.phys.Vec3;

public record CircleGizmo(Vec3 pos, float radius, GizmoStyle style) implements Gizmo
{
    private static final int CIRCLE_VERTICES = 20;
    private static final float SEGMENT_SIZE_RADIANS = 0.31415927f;

    @Override
    public void emit(GizmoPrimitives $$0, float $$1) {
        if (!this.style.hasStroke() && !this.style.hasFill()) {
            return;
        }
        Vec3[] $$2 = new Vec3[21];
        for (int $$3 = 0; $$3 < 20; ++$$3) {
            Vec3 $$5;
            float $$4 = (float)$$3 * 0.31415927f;
            $$2[$$3] = $$5 = this.pos.add((float)((double)this.radius * Math.cos($$4)), 0.0, (float)((double)this.radius * Math.sin($$4)));
        }
        $$2[20] = $$2[0];
        if (this.style.hasFill()) {
            int $$6 = this.style.multipliedFill($$1);
            $$0.addTriangleFan($$2, $$6);
        }
        if (this.style.hasStroke()) {
            int $$7 = this.style.multipliedStroke($$1);
            for (int $$8 = 0; $$8 < 20; ++$$8) {
                $$0.addLine($$2[$$8], $$2[$$8 + 1], $$7, this.style.strokeWidth());
            }
        }
    }
}

