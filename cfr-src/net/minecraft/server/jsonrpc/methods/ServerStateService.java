/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.Message;
import net.minecraft.server.jsonrpc.methods.PlayerService;
import net.minecraft.server.level.ServerPlayer;

public class ServerStateService {
    public static ServerState status(MinecraftApi $$0) {
        if (!$$0.serverStateService().isReady()) {
            return ServerState.NOT_STARTED;
        }
        return new ServerState(true, PlayerService.get($$0), ServerStatus.Version.current());
    }

    public static boolean save(MinecraftApi $$0, boolean $$1, ClientInfo $$2) {
        return $$0.serverStateService().saveEverything(true, $$1, true, $$2);
    }

    public static boolean stop(MinecraftApi $$0, ClientInfo $$1) {
        $$0.submit(() -> $$0.serverStateService().halt(false, $$1));
        return true;
    }

    public static boolean systemMessage(MinecraftApi $$0, SystemMessage $$1, ClientInfo $$2) {
        Component $$3 = $$1.message().asComponent().orElse(null);
        if ($$3 == null) {
            return false;
        }
        if ($$1.receivingPlayers().isPresent()) {
            if ($$1.receivingPlayers().get().isEmpty()) {
                return false;
            }
            for (PlayerDto $$4 : $$1.receivingPlayers().get()) {
                ServerPlayer $$6;
                if ($$4.id().isPresent()) {
                    ServerPlayer $$5 = $$0.playerListService().getPlayer($$4.id().get());
                } else {
                    if (!$$4.name().isPresent()) continue;
                    $$6 = $$0.playerListService().getPlayerByName($$4.name().get());
                }
                if ($$6 == null) continue;
                $$6.sendSystemMessage($$3, $$1.overlay());
            }
        } else {
            $$0.serverStateService().broadcastSystemMessage($$3, $$1.overlay(), $$2);
        }
        return true;
    }

    public record ServerState(boolean started, List<PlayerDto> players, ServerStatus.Version version) {
        public static final Codec<ServerState> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.BOOL.fieldOf("started").forGetter(ServerState::started), (App)PlayerDto.CODEC.codec().listOf().lenientOptionalFieldOf("players", List.of()).forGetter(ServerState::players), (App)ServerStatus.Version.CODEC.fieldOf("version").forGetter(ServerState::version)).apply((Applicative)$$0, ServerState::new));
        public static final ServerState NOT_STARTED = new ServerState(false, List.of(), ServerStatus.Version.current());
    }

    public record SystemMessage(Message message, boolean overlay, Optional<List<PlayerDto>> receivingPlayers) {
        public static final Codec<SystemMessage> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Message.CODEC.fieldOf("message").forGetter(SystemMessage::message), (App)Codec.BOOL.fieldOf("overlay").forGetter(SystemMessage::overlay), (App)PlayerDto.CODEC.codec().listOf().lenientOptionalFieldOf("receivingPlayers").forGetter(SystemMessage::receivingPlayers)).apply((Applicative)$$0, SystemMessage::new));
    }
}

