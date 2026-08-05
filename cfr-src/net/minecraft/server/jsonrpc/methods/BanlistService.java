/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class BanlistService {
    private static final String BAN_SOURCE = "Management server";

    public static List<UserBanDto> get(MinecraftApi $$02) {
        return $$02.banListService().getUserBanEntries().stream().filter($$0 -> $$0.getUser() != null).map(UserBan::from).map(UserBanDto::from).toList();
    }

    public static List<UserBanDto> add(MinecraftApi $$0, List<UserBanDto> $$1, ClientInfo $$2) {
        List<CompletableFuture> $$3 = $$1.stream().map($$12 -> $$0.playerListService().getUser($$12.player().id(), $$12.player().name()).thenApply($$1 -> $$1.map($$12::toUserBan))).toList();
        for (Optional $$4 : Util.sequence($$3).join()) {
            if ($$4.isEmpty()) continue;
            UserBan $$5 = (UserBan)$$4.get();
            $$0.banListService().addUserBan($$5.toBanEntry(), $$2);
            ServerPlayer $$6 = $$0.playerListService().getPlayer(((UserBan)$$4.get()).player().id());
            if ($$6 == null) continue;
            $$6.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
        }
        return BanlistService.get($$0);
    }

    public static List<UserBanDto> clear(MinecraftApi $$0, ClientInfo $$1) {
        $$0.banListService().clearUserBans($$1);
        return BanlistService.get($$0);
    }

    public static List<UserBanDto> remove(MinecraftApi $$0, List<PlayerDto> $$12, ClientInfo $$2) {
        List<CompletableFuture> $$3 = $$12.stream().map($$1 -> $$0.playerListService().getUser($$1.id(), $$1.name())).toList();
        for (Optional $$4 : Util.sequence($$3).join()) {
            if ($$4.isEmpty()) continue;
            $$0.banListService().removeUserBan((NameAndId)$$4.get(), $$2);
        }
        return BanlistService.get($$0);
    }

    public static List<UserBanDto> set(MinecraftApi $$02, List<UserBanDto> $$13, ClientInfo $$22) {
        List<CompletableFuture> $$3 = $$13.stream().map($$12 -> $$02.playerListService().getUser($$12.player().id(), $$12.player().name()).thenApply($$1 -> $$1.map($$12::toUserBan))).toList();
        Set $$4 = Util.sequence($$3).join().stream().flatMap(Optional::stream).collect(Collectors.toSet());
        Set $$5 = $$02.banListService().getUserBanEntries().stream().filter($$0 -> $$0.getUser() != null).map(UserBan::from).collect(Collectors.toSet());
        $$5.stream().filter($$1 -> !$$4.contains($$1)).forEach($$2 -> $$02.banListService().removeUserBan($$2.player(), $$22));
        $$4.stream().filter($$1 -> !$$5.contains($$1)).forEach($$2 -> {
            $$02.banListService().addUserBan($$2.toBanEntry(), $$22);
            ServerPlayer $$3 = $$02.playerListService().getPlayer($$2.player().id());
            if ($$3 != null) {
                $$3.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            }
        });
        return BanlistService.get($$02);
    }

    record UserBan(NameAndId player, @Nullable String reason, String source, Optional<Instant> expires) {
        static UserBan from(UserBanListEntry $$0) {
            return new UserBan(Objects.requireNonNull((NameAndId)$$0.getUser()), $$0.getReason(), $$0.getSource(), Optional.ofNullable($$0.getExpires()).map(Date::toInstant));
        }

        UserBanListEntry toBanEntry() {
            return new UserBanListEntry(new NameAndId(this.player().id(), this.player().name()), null, this.source(), (Date)this.expires().map(Date::from).orElse(null), this.reason());
        }
    }

    public record UserBanDto(PlayerDto player, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<UserBanDto> CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)PlayerDto.CODEC.codec().fieldOf("player").forGetter(UserBanDto::player), (App)Codec.STRING.optionalFieldOf("reason").forGetter(UserBanDto::reason), (App)Codec.STRING.optionalFieldOf("source").forGetter(UserBanDto::source), (App)ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("expires").forGetter(UserBanDto::expires)).apply((Applicative)$$0, UserBanDto::new));

        private static UserBanDto from(UserBan $$0) {
            return new UserBanDto(PlayerDto.from($$0.player()), Optional.ofNullable($$0.reason()), Optional.of($$0.source()), $$0.expires());
        }

        public static UserBanDto from(UserBanListEntry $$0) {
            return UserBanDto.from(UserBan.from($$0));
        }

        private UserBan toUserBan(NameAndId $$0) {
            return new UserBan($$0, this.reason().orElse(null), this.source().orElse(BanlistService.BAN_SOURCE), this.expires());
        }
    }
}

