/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import net.minecraft.gametest.framework.GameTestException;
import net.minecraft.network.chat.Component;

public class UnknownGameTestException
extends GameTestException {
    private final Throwable reason;

    public UnknownGameTestException(Throwable $$0) {
        super($$0.getMessage());
        this.reason = $$0;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("test.error.unknown", this.reason.getMessage());
    }
}

