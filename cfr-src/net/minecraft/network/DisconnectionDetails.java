/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public record DisconnectionDetails(Component reason, Optional<Path> report, Optional<URI> bugReportLink) {
    public DisconnectionDetails(Component $$0) {
        this($$0, Optional.empty(), Optional.empty());
    }
}

