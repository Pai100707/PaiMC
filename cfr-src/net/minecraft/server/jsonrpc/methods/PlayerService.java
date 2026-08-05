/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.Message;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public class PlayerService {
    private static final Component DEFAULT_KICK_MESSAGE = Component.translatable("multiplayer.disconnect.kicked");

    public static List<PlayerDto> get(MinecraftApi $$0) {
        return $$0.playerListService().getPlayers().stream().map(PlayerDto::from).toList();
    }

    public static List<PlayerDto> kick(MinecraftApi $$0, List<KickDto> $$1, ClientInfo $$2) {
        ArrayList<PlayerDto> $$3 = new ArrayList<PlayerDto>();
        for (KickDto $$4 : $$1) {
            ServerPlayer $$5 = PlayerService.getServerPlayer($$0, $$4.player());
            if ($$5 == null) continue;
            $$0.playerListService().remove($$5, $$2);
            $$5.connection.disconnect($$4.message.flatMap(Message::asComponent).orElse(DEFAULT_KICK_MESSAGE));
            $$3.add($$4.player());
        }
        return $$3;
    }

    private static @Nullable ServerPlayer getServerPlayer(MinecraftApi $$0, PlayerDto $$1) {
        if ($$1.id().isPresent()) {
            return $$0.playerListService().getPlayer($$1.id().get());
        }
        if ($$1.name().isPresent()) {
            return $$0.playerListService().getPlayerByName($$1.name().get());
        }
        return null;
    }

    public static final class KickDto
    extends Record {
        private final PlayerDto player;
        final Optional<Message> message;
        public static final MapCodec<KickDto> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)PlayerDto.CODEC.codec().fieldOf("player").forGetter(KickDto::player), (App)Message.CODEC.optionalFieldOf("message").forGetter(KickDto::message)).apply((Applicative)$$0, KickDto::new));

        public KickDto(PlayerDto $$0, Optional<Message> $$1) {
            this.player = $$0;
            this.message = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{KickDto.class, "player;message", "player", "message"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{KickDto.class, "player;message", "player", "message"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{KickDto.class, "player;message", "player", "message"}, this, $$0);
        }

        public PlayerDto player() {
            return this.player;
        }

        public Optional<Message> message() {
            return this.message;
        }
    }
}

