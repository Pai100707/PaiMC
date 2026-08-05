/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gizmos;

import net.minecraft.core.Direction;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.phys.Vec3;

public record RectGizmo(Vec3 a, Vec3 b, Vec3 c, Vec3 d, GizmoStyle style) implements Gizmo
{
    public static RectGizmo fromCuboidFace(Vec3 $$0, Vec3 $$1, Direction $$2, GizmoStyle $$3) {
        return switch ($$2) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> new RectGizmo(new Vec3($$0.x, $$0.y, $$0.z), new Vec3($$1.x, $$0.y, $$0.z), new Vec3($$1.x, $$0.y, $$1.z), new Vec3($$0.x, $$0.y, $$1.z), $$3);
            case Direction.UP -> new RectGizmo(new Vec3($$0.x, $$1.y, $$0.z), new Vec3($$0.x, $$1.y, $$1.z), new Vec3($$1.x, $$1.y, $$1.z), new Vec3($$1.x, $$1.y, $$0.z), $$3);
            case Direction.NORTH -> new RectGizmo(new Vec3($$0.x, $$0.y, $$0.z), new Vec3($$0.x, $$1.y, $$0.z), new Vec3($$1.x, $$1.y, $$0.z), new Vec3($$1.x, $$0.y, $$0.z), $$3);
            case Direction.SOUTH -> new RectGizmo(new Vec3($$0.x, $$0.y, $$1.z), new Vec3($$1.x, $$0.y, $$1.z), new Vec3($$1.x, $$1.y, $$1.z), new Vec3($$0.x, $$1.y, $$1.z), $$3);
            case Direction.WEST -> new RectGizmo(new Vec3($$0.x, $$0.y, $$0.z), new Vec3($$0.x, $$0.y, $$1.z), new Vec3($$0.x, $$1.y, $$1.z), new Vec3($$0.x, $$1.y, $$0.z), $$3);
            case Direction.EAST -> new RectGizmo(new Vec3($$1.x, $$0.y, $$0.z), new Vec3($$1.x, $$1.y, $$0.z), new Vec3($$1.x, $$1.y, $$1.z), new Vec3($$1.x, $$0.y, $$1.z), $$3);
        };
    }

    @Override
    public void emit(GizmoPrimitives $$0, float $$1) {
        if (this.style.hasFill()) {
            int $$2 = this.style.multipliedFill($$1);
            $$0.addQuad(this.a, this.b, this.c, this.d, $$2);
        }
        if (this.style.hasStroke()) {
            int $$3 = this.style.multipliedStroke($$1);
            $$0.addLine(this.a, this.b, $$3, this.style.strokeWidth());
            $$0.addLine(this.b, this.c, $$3, this.style.strokeWidth());
            $$0.addLine(this.c, this.d, $$3, this.style.strokeWidth());
            $$0.addLine(this.d, this.a, $$3, this.style.strokeWidth());
        }
    }
}

