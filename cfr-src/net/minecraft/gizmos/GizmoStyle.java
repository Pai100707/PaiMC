/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gizmos;

import net.minecraft.util.ARGB;

public record GizmoStyle(int stroke, float strokeWidth, int fill) {
    private static final float DEFAULT_WIDTH = 2.5f;

    public static GizmoStyle stroke(int $$0) {
        return new GizmoStyle($$0, 2.5f, 0);
    }

    public static GizmoStyle stroke(int $$0, float $$1) {
        return new GizmoStyle($$0, $$1, 0);
    }

    public static GizmoStyle fill(int $$0) {
        return new GizmoStyle(0, 0.0f, $$0);
    }

    public static GizmoStyle strokeAndFill(int $$0, float $$1, int $$2) {
        return new GizmoStyle($$0, $$1, $$2);
    }

    public boolean hasFill() {
        return this.fill != 0;
    }

    public boolean hasStroke() {
        return this.stroke != 0 && this.strokeWidth > 0.0f;
    }

    public int multipliedStroke(float $$0) {
        return ARGB.multiplyAlpha(this.stroke, $$0);
    }

    public int multipliedFill(float $$0) {
        return ARGB.multiplyAlpha(this.fill, $$0);
    }
}

