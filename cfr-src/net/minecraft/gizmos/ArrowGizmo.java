/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.gizmos;

import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record ArrowGizmo(Vec3 start, Vec3 end, int color, float width) implements Gizmo
{
    public static final float DEFAULT_WIDTH = 2.5f;

    @Override
    public void emit(GizmoPrimitives $$0, float $$1) {
        Vector3f[] $$5;
        int $$2 = ARGB.multiplyAlpha(this.color, $$1);
        $$0.addLine(this.start, this.end, $$2, this.width);
        Quaternionf $$3 = new Quaternionf().rotationTo((Vector3fc)new Vector3f(1.0f, 0.0f, 0.0f), (Vector3fc)this.end.subtract(this.start).toVector3f().normalize());
        float $$4 = (float)Mth.clamp(this.end.distanceTo(this.start) * (double)0.1f, (double)0.1f, 1.0);
        for (Vector3f $$6 : $$5 = new Vector3f[]{$$3.transform(-$$4, $$4, 0.0f, new Vector3f()), $$3.transform(-$$4, 0.0f, $$4, new Vector3f()), $$3.transform(-$$4, -$$4, 0.0f, new Vector3f()), $$3.transform(-$$4, 0.0f, -$$4, new Vector3f())}) {
            $$0.addLine(this.end.add($$6.x, $$6.y, $$6.z), this.end, $$2, this.width);
        }
    }
}

