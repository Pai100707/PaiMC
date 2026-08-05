/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gizmos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoCollector;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class SimpleGizmoCollector
implements GizmoCollector {
    private final List<GizmoInstance> gizmos = new ArrayList<GizmoInstance>();
    private final List<GizmoInstance> temporaryGizmos = new ArrayList<GizmoInstance>();

    @Override
    public GizmoProperties add(Gizmo $$0) {
        GizmoInstance $$1 = new GizmoInstance($$0);
        this.gizmos.add($$1);
        return $$1;
    }

    public List<GizmoInstance> drainGizmos() {
        ArrayList<GizmoInstance> $$0 = new ArrayList<GizmoInstance>(this.gizmos);
        $$0.addAll(this.temporaryGizmos);
        long $$12 = Util.getMillis();
        this.gizmos.removeIf($$1 -> $$1.getExpireTimeMillis() < $$12);
        this.temporaryGizmos.clear();
        return $$0;
    }

    public List<GizmoInstance> getGizmos() {
        return this.gizmos;
    }

    public void addTemporaryGizmos(Collection<GizmoInstance> $$0) {
        this.temporaryGizmos.addAll($$0);
    }

    public static class GizmoInstance
    implements GizmoProperties {
        private final Gizmo gizmo;
        private boolean isAlwaysOnTop;
        private long startTimeMillis;
        private long expireTimeMillis;
        private boolean shouldFadeOut;

        GizmoInstance(Gizmo $$0) {
            this.gizmo = $$0;
        }

        @Override
        public GizmoProperties setAlwaysOnTop() {
            this.isAlwaysOnTop = true;
            return this;
        }

        @Override
        public GizmoProperties persistForMillis(int $$0) {
            this.startTimeMillis = Util.getMillis();
            this.expireTimeMillis = this.startTimeMillis + (long)$$0;
            return this;
        }

        @Override
        public GizmoProperties fadeOut() {
            this.shouldFadeOut = true;
            return this;
        }

        public float getAlphaMultiplier(long $$0) {
            if (this.shouldFadeOut) {
                long $$1 = this.expireTimeMillis - this.startTimeMillis;
                long $$2 = $$0 - this.startTimeMillis;
                return 1.0f - Mth.clamp((float)$$2 / (float)$$1, 0.0f, 1.0f);
            }
            return 1.0f;
        }

        public boolean isAlwaysOnTop() {
            return this.isAlwaysOnTop;
        }

        public long getExpireTimeMillis() {
            return this.expireTimeMillis;
        }

        public Gizmo gizmo() {
            return this.gizmo;
        }
    }
}

