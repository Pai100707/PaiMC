/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.gametest.framework;

import org.jspecify.annotations.Nullable;

class GameTestEvent {
    public final @Nullable Long expectedDelay;
    public final Runnable assertion;

    private GameTestEvent(@Nullable Long $$0, Runnable $$1) {
        this.expectedDelay = $$0;
        this.assertion = $$1;
    }

    static GameTestEvent create(Runnable $$0) {
        return new GameTestEvent(null, $$0);
    }

    static GameTestEvent create(long $$0, Runnable $$1) {
        return new GameTestEvent($$0, $$1);
    }
}

