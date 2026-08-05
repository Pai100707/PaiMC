/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs;

public enum PackType {
    CLIENT_RESOURCES("assets"),
    SERVER_DATA("data");

    private final String directory;

    private PackType(String $$0) {
        this.directory = $$0;
    }

    public String getDirectory() {
        return this.directory;
    }
}

