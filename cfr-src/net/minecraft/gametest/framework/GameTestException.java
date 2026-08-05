/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import net.minecraft.network.chat.Component;

public abstract class GameTestException
extends RuntimeException {
    public GameTestException(String $$0) {
        super($$0);
    }

    public abstract Component getDescription();
}

