/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution;

public record ChainModifiers(byte flags) {
    public static final ChainModifiers DEFAULT = new ChainModifiers(0);
    private static final byte FLAG_FORKED = 1;
    private static final byte FLAG_IS_RETURN = 2;

    private ChainModifiers setFlag(byte $$0) {
        int $$1 = this.flags | $$0;
        return $$1 != this.flags ? new ChainModifiers((byte)$$1) : this;
    }

    public boolean isForked() {
        return (this.flags & 1) != 0;
    }

    public ChainModifiers setForked() {
        return this.setFlag((byte)1);
    }

    public boolean isReturn() {
        return (this.flags & 2) != 0;
    }

    public ChainModifiers setReturn() {
        return this.setFlag((byte)2);
    }
}

