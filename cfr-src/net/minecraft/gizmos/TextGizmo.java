/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gizmos;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.OptionalDouble;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public record TextGizmo(Vec3 pos, String text, Style style) implements Gizmo
{
    @Override
    public void emit(GizmoPrimitives $$0, float $$1) {
        Style $$3;
        if ($$1 < 1.0f) {
            Style $$2 = new Style(ARGB.multiplyAlpha(this.style.color, $$1), this.style.scale, this.style.adjustLeft);
        } else {
            $$3 = this.style;
        }
        $$0.addText(this.pos, this.text, $$3);
    }

    public static final class Style
    extends Record {
        final int color;
        final float scale;
        final OptionalDouble adjustLeft;
        public static final float DEFAULT_SCALE = 0.32f;

        public Style(int $$0, float $$1, OptionalDouble $$2) {
            this.color = $$0;
            this.scale = $$1;
            this.adjustLeft = $$2;
        }

        public static Style whiteAndCentered() {
            return new Style(-1, 0.32f, OptionalDouble.empty());
        }

        public static Style forColorAndCentered(int $$0) {
            return new Style($$0, 0.32f, OptionalDouble.empty());
        }

        public static Style forColor(int $$0) {
            return new Style($$0, 0.32f, OptionalDouble.of(0.0));
        }

        public Style withScale(float $$0) {
            return new Style(this.color, $$0, this.adjustLeft);
        }

        public Style withLeftAlignment(float $$0) {
            return new Style(this.color, this.scale, OptionalDouble.of($$0));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Style.class, "color;scale;adjustLeft", "color", "scale", "adjustLeft"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Style.class, "color;scale;adjustLeft", "color", "scale", "adjustLeft"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Style.class, "color;scale;adjustLeft", "color", "scale", "adjustLeft"}, this, $$0);
        }

        public int color() {
            return this.color;
        }

        public float scale() {
            return this.scale;
        }

        public OptionalDouble adjustLeft() {
            return this.adjustLeft;
        }
    }
}

