/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated;

import java.nio.file.Path;
import java.util.function.UnaryOperator;
import net.minecraft.server.dedicated.DedicatedServerProperties;

public class DedicatedServerSettings {
    private final Path source;
    private DedicatedServerProperties properties;

    public DedicatedServerSettings(Path $$0) {
        this.source = $$0;
        this.properties = DedicatedServerProperties.fromFile($$0);
    }

    public DedicatedServerProperties getProperties() {
        return this.properties;
    }

    public void forceSave() {
        this.properties.store(this.source);
    }

    public DedicatedServerSettings update(UnaryOperator<DedicatedServerProperties> $$0) {
        this.properties = (DedicatedServerProperties)$$0.apply(this.properties);
        this.properties.store(this.source);
        return this;
    }
}

