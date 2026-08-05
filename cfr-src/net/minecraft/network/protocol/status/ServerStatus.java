/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.protocol.status;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.players.NameAndId;

public record ServerStatus(Component description, Optional<Players> players, Optional<Version> version, Optional<Favicon> favicon, boolean enforcesSecureChat) {
    public static final Codec<ServerStatus> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)ComponentSerialization.CODEC.lenientOptionalFieldOf("description", (Object)CommonComponents.EMPTY).forGetter(ServerStatus::description), (App)Players.CODEC.lenientOptionalFieldOf("players").forGetter(ServerStatus::players), (App)Version.CODEC.lenientOptionalFieldOf("version").forGetter(ServerStatus::version), (App)Favicon.CODEC.lenientOptionalFieldOf("favicon").forGetter(ServerStatus::favicon), (App)Codec.BOOL.lenientOptionalFieldOf("enforcesSecureChat", (Object)false).forGetter(ServerStatus::enforcesSecureChat)).apply((Applicative)$$0, ServerStatus::new));

    public record Players(int max, int online, List<NameAndId> sample) {
        public static final Codec<Players> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.INT.fieldOf("max").forGetter(Players::max), (App)Codec.INT.fieldOf("online").forGetter(Players::online), (App)NameAndId.CODEC.listOf().lenientOptionalFieldOf("sample", List.of()).forGetter(Players::sample)).apply((Applicative)$$0, Players::new));
    }

    public record Version(String name, int protocol) {
        public static final Codec<Version> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.STRING.fieldOf("name").forGetter(Version::name), (App)Codec.INT.fieldOf("protocol").forGetter(Version::protocol)).apply((Applicative)$$0, Version::new));

        public static Version current() {
            WorldVersion $$0 = SharedConstants.getCurrentVersion();
            return new Version($$0.name(), $$0.protocolVersion());
        }
    }

    public record Favicon(byte[] iconBytes) {
        private static final String PREFIX = "data:image/png;base64,";
        public static final Codec<Favicon> CODEC = Codec.STRING.comapFlatMap($$0 -> {
            if (!$$0.startsWith(PREFIX)) {
                return DataResult.error(() -> "Unknown format");
            }
            try {
                String $$1 = $$0.substring(PREFIX.length()).replaceAll("\n", "");
                byte[] $$2 = Base64.getDecoder().decode($$1.getBytes(StandardCharsets.UTF_8));
                return DataResult.success((Object)new Favicon($$2));
            }
            catch (IllegalArgumentException $$3) {
                return DataResult.error(() -> "Malformed base64 server icon");
            }
        }, $$0 -> PREFIX + new String(Base64.getEncoder().encode($$0.iconBytes), StandardCharsets.UTF_8));
    }
}

