/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gizmos;

import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoProperties;

public interface GizmoCollector {
    public static final GizmoProperties IGNORED = new GizmoProperties(){

        @Override
        public GizmoProperties setAlwaysOnTop() {
            return this;
        }

        @Override
        public GizmoProperties persistForMillis(int $$0) {
            return this;
        }

        @Override
        public GizmoProperties fadeOut() {
            return this;
        }
    };
    public static final GizmoCollector NOOP = $$0 -> IGNORED;

    public GizmoProperties add(Gizmo var1);
}

