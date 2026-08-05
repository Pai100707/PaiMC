/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol;

public enum PacketFlow {
    SERVERBOUND("serverbound"),
    CLIENTBOUND("clientbound");

    private final String id;

    private PacketFlow(String $$0) {
        this.id = $$0;
    }

    public PacketFlow getOpposite() {
        return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
    }

    public String id() {
        return this.id;
    }
}

