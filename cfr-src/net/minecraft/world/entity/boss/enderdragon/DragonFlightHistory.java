/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.boss.enderdragon;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import net.minecraft.util.Mth;

public class DragonFlightHistory {
    public static final int LENGTH = 64;
    private static final int MASK = 63;
    private final Sample[] samples = new Sample[64];
    private int head = -1;

    public DragonFlightHistory() {
        Arrays.fill(this.samples, new Sample(0.0, 0.0f));
    }

    public void copyFrom(DragonFlightHistory $$0) {
        System.arraycopy($$0.samples, 0, this.samples, 0, 64);
        this.head = $$0.head;
    }

    public void record(double $$0, float $$1) {
        Sample $$2 = new Sample($$0, $$1);
        if (this.head < 0) {
            Arrays.fill(this.samples, $$2);
        }
        if (++this.head == 64) {
            this.head = 0;
        }
        this.samples[this.head] = $$2;
    }

    public Sample get(int $$0) {
        return this.samples[this.head - $$0 & 0x3F];
    }

    public Sample get(int $$0, float $$1) {
        Sample $$2 = this.get($$0);
        Sample $$3 = this.get($$0 + 1);
        return new Sample(Mth.lerp((double)$$1, $$3.y, $$2.y), Mth.rotLerp($$1, $$3.yRot, $$2.yRot));
    }

    public static final class Sample
    extends Record {
        final double y;
        final float yRot;

        public Sample(double $$0, float $$1) {
            this.y = $$0;
            this.yRot = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Sample.class, "y;yRot", "y", "yRot"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Sample.class, "y;yRot", "y", "yRot"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Sample.class, "y;yRot", "y", "yRot"}, this, $$0);
        }

        public double y() {
            return this.y;
        }

        public float yRot() {
            return this.yRot;
        }
    }
}

