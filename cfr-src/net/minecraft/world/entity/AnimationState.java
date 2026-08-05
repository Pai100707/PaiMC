/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.function.Consumer;

public class AnimationState {
    private static final int STOPPED = Integer.MIN_VALUE;
    private int startTick = Integer.MIN_VALUE;

    public void start(int $$0) {
        this.startTick = $$0;
    }

    public void startIfStopped(int $$0) {
        if (!this.isStarted()) {
            this.start($$0);
        }
    }

    public void animateWhen(boolean $$0, int $$1) {
        if ($$0) {
            this.startIfStopped($$1);
        } else {
            this.stop();
        }
    }

    public void stop() {
        this.startTick = Integer.MIN_VALUE;
    }

    public void ifStarted(Consumer<AnimationState> $$0) {
        if (this.isStarted()) {
            $$0.accept(this);
        }
    }

    public void fastForward(int $$0, float $$1) {
        if (!this.isStarted()) {
            return;
        }
        this.startTick -= (int)((float)$$0 * $$1);
    }

    public long getTimeInMillis(float $$0) {
        float $$1 = $$0 - (float)this.startTick;
        return (long)($$1 * 50.0f);
    }

    public boolean isStarted() {
        return this.startTick != Integer.MIN_VALUE;
    }

    public void copyFrom(AnimationState $$0) {
        this.startTick = $$0.startTick;
    }
}

