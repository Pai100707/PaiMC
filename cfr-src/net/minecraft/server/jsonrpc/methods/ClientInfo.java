/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.methods;

public record ClientInfo(Integer connectionId) {
    public static ClientInfo of(Integer $$0) {
        return new ClientInfo($$0);
    }
}

