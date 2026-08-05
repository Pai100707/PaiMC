/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.net.InetAddresses
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc.methods;

import com.google.common.net.InetAddresses;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.util.ExtraCodecs;
import org.jspecify.annotations.Nullable;

public class IpBanlistService {
    private static final String BAN_SOURCE = "Management server";

    public static List<IpBanDto> get(MinecraftApi $$0) {
        return $$0.banListService().getIpBanEntries().stream().map(IpBan::from).map(IpBanDto::from).toList();
    }

    public static List<IpBanDto> add(MinecraftApi $$02, List<IncomingIpBanDto> $$1, ClientInfo $$22) {
        $$1.stream().map($$2 -> IpBanlistService.banIp($$02, $$2, $$22)).flatMap(Collection::stream).forEach($$0 -> $$0.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned")));
        return IpBanlistService.get($$02);
    }

    private static List<ServerPlayer> banIp(MinecraftApi $$0, IncomingIpBanDto $$1, ClientInfo $$2) {
        Optional<ServerPlayer> $$4;
        IpBan $$3 = $$1.toIpBan();
        if ($$3 != null) {
            return IpBanlistService.banIp($$0, $$3, $$2);
        }
        if ($$1.player().isPresent() && ($$4 = $$0.playerListService().getPlayer($$1.player().get().id(), $$1.player().get().name())).isPresent()) {
            return IpBanlistService.banIp($$0, $$1.toIpBan($$4.get()), $$2);
        }
        return List.of();
    }

    private static List<ServerPlayer> banIp(MinecraftApi $$0, IpBan $$1, ClientInfo $$2) {
        $$0.banListService().addIpBan($$1.toIpBanEntry(), $$2);
        return $$0.playerListService().getPlayersWithAddress($$1.ip());
    }

    public static List<IpBanDto> clear(MinecraftApi $$0, ClientInfo $$1) {
        $$0.banListService().clearIpBans($$1);
        return IpBanlistService.get($$0);
    }

    public static List<IpBanDto> remove(MinecraftApi $$0, List<String> $$1, ClientInfo $$22) {
        $$1.forEach($$2 -> $$0.banListService().removeIpBan((String)$$2, $$22));
        return IpBanlistService.get($$0);
    }

    public static List<IpBanDto> set(MinecraftApi $$02, List<IpBanDto> $$12, ClientInfo $$22) {
        Set $$3 = $$12.stream().filter($$0 -> InetAddresses.isInetAddress((String)$$0.ip())).map(IpBanDto::toIpBan).collect(Collectors.toSet());
        Set $$4 = $$02.banListService().getIpBanEntries().stream().map(IpBan::from).collect(Collectors.toSet());
        $$4.stream().filter($$1 -> !$$3.contains($$1)).forEach($$2 -> $$02.banListService().removeIpBan($$2.ip(), $$22));
        $$3.stream().filter($$1 -> !$$4.contains($$1)).forEach($$2 -> $$02.banListService().addIpBan($$2.toIpBanEntry(), $$22));
        $$3.stream().filter($$1 -> !$$4.contains($$1)).flatMap($$1 -> $$02.playerListService().getPlayersWithAddress($$1.ip()).stream()).forEach($$0 -> $$0.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned")));
        return IpBanlistService.get($$02);
    }

    public record IncomingIpBanDto(Optional<PlayerDto> player, Optional<String> ip, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<IncomingIpBanDto> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)PlayerDto.CODEC.codec().optionalFieldOf("player").forGetter(IncomingIpBanDto::player), (App)Codec.STRING.optionalFieldOf("ip").forGetter(IncomingIpBanDto::ip), (App)Codec.STRING.optionalFieldOf("reason").forGetter(IncomingIpBanDto::reason), (App)Codec.STRING.optionalFieldOf("source").forGetter(IncomingIpBanDto::source), (App)ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("expires").forGetter(IncomingIpBanDto::expires)).apply((Applicative)$$0, IncomingIpBanDto::new));

        IpBan toIpBan(ServerPlayer $$0) {
            return new IpBan($$0.getIpAddress(), this.reason().orElse(null), this.source().orElse(IpBanlistService.BAN_SOURCE), this.expires());
        }

        @Nullable IpBan toIpBan() {
            if (this.ip().isEmpty() || !InetAddresses.isInetAddress((String)this.ip().get())) {
                return null;
            }
            return new IpBan(this.ip().get(), this.reason().orElse(null), this.source().orElse(IpBanlistService.BAN_SOURCE), this.expires());
        }
    }

    record IpBan(String ip, @Nullable String reason, String source, Optional<Instant> expires) {
        static IpBan from(IpBanListEntry $$0) {
            return new IpBan(Objects.requireNonNull((String)$$0.getUser()), $$0.getReason(), $$0.getSource(), Optional.ofNullable($$0.getExpires()).map(Date::toInstant));
        }

        IpBanListEntry toIpBanEntry() {
            return new IpBanListEntry(this.ip(), null, this.source(), (Date)this.expires().map(Date::from).orElse(null), this.reason());
        }
    }

    public record IpBanDto(String ip, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<IpBanDto> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.STRING.fieldOf("ip").forGetter(IpBanDto::ip), (App)Codec.STRING.optionalFieldOf("reason").forGetter(IpBanDto::reason), (App)Codec.STRING.optionalFieldOf("source").forGetter(IpBanDto::source), (App)ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("expires").forGetter(IpBanDto::expires)).apply((Applicative)$$0, IpBanDto::new));

        private static IpBanDto from(IpBan $$0) {
            return new IpBanDto($$0.ip(), Optional.ofNullable($$0.reason()), Optional.of($$0.source()), $$0.expires());
        }

        public static IpBanDto from(IpBanListEntry $$0) {
            return IpBanDto.from(IpBan.from($$0));
        }

        private IpBan toIpBan() {
            return new IpBan(this.ip(), this.reason().orElse(null), this.source().orElse(IpBanlistService.BAN_SOURCE), this.expires());
        }
    }
}

