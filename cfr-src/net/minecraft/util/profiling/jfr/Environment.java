/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr;

import net.minecraft.server.MinecraftServer;

public enum Environment {
    CLIENT("client"),
    SERVER("server");

    private final String description;

    private Environment(String $$0) {
        this.description = $$0;
    }

    public static Environment from(MinecraftServer $$0) {
        return $$0.isDedicatedServer() ? SERVER : CLIENT;
    }

    public String getDescription() {
        return this.description;
    }
}

