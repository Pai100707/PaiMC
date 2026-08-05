/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import net.minecraft.util.EasingType;
import net.minecraft.util.Mth;

public class BinaryAnimator {
    private final int animationLength;
    private final EasingType easing;
    private int ticks;
    private int ticksOld;

    public BinaryAnimator(int $$0, EasingType $$1) {
        this.animationLength = $$0;
        this.easing = $$1;
    }

    public BinaryAnimator(int $$0) {
        this($$0, EasingType.LINEAR);
    }

    public void tick(boolean $$0) {
        this.ticksOld = this.ticks;
        if ($$0) {
            if (this.ticks < this.animationLength) {
                ++this.ticks;
            }
        } else if (this.ticks > 0) {
            --this.ticks;
        }
    }

    public float getFactor(float $$0) {
        float $$1 = Mth.lerp($$0, this.ticksOld, this.ticks) / (float)this.animationLength;
        return this.easing.apply($$1);
    }
}

