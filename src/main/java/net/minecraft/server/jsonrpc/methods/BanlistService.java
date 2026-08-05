package net.minecraft.server.jsonrpc.methods;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;

public class BanlistService {
   private static final String BAN_SOURCE = "Management server";

   public static List<BanlistService.UserBanDto> get(MinecraftApi $$0) {
      return $$0.banListService()
         .getUserBanEntries()
         .stream()
         .filter($$0x -> $$0x.getUser() != null)
         .map(BanlistService.UserBan::from)
         .map(BanlistService.UserBanDto::from)
         .toList();
   }

   public static List<BanlistService.UserBanDto> add(MinecraftApi $$0, List<BanlistService.UserBanDto> $$1, ClientInfo $$2) {
      List<CompletableFuture<Optional<BanlistService.UserBan>>> $$3 = $$1.stream()
         .map($$1x -> $$0.playerListService().getUser($$1x.player().id(), $$1x.player().name()).thenApply($$1xx -> $$1xx.map($$1x::toUserBan)))
         .toList();

      for (Optional<BanlistService.UserBan> $$4 : (List)Util.sequence($$3).join()) {
         if (!$$4.isEmpty()) {
            BanlistService.UserBan $$5 = $$4.get();
            $$0.banListService().addUserBan($$5.toBanEntry(), $$2);
            ServerPlayer $$6 = $$0.playerListService().getPlayer($$4.get().player().id());
            if ($$6 != null) {
               $$6.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            }
         }
      }

      return get($$0);
   }

   public static List<BanlistService.UserBanDto> clear(MinecraftApi $$0, ClientInfo $$1) {
      $$0.banListService().clearUserBans($$1);
      return get($$0);
   }

   public static List<BanlistService.UserBanDto> remove(MinecraftApi $$0, List<PlayerDto> $$1, ClientInfo $$2) {
      List<CompletableFuture<Optional<NameAndId>>> $$3 = $$1.stream().map($$1x -> $$0.playerListService().getUser($$1x.id(), $$1x.name())).toList();

      for (Optional<NameAndId> $$4 : (List)Util.sequence($$3).join()) {
         if (!$$4.isEmpty()) {
            $$0.banListService().removeUserBan($$4.get(), $$2);
         }
      }

      return get($$0);
   }

   public static List<BanlistService.UserBanDto> set(MinecraftApi $$0, List<BanlistService.UserBanDto> $$1, ClientInfo $$2) {
      List<CompletableFuture<Optional<BanlistService.UserBan>>> $$3 = $$1.stream()
         .map($$1x -> $$0.playerListService().getUser($$1x.player().id(), $$1x.player().name()).thenApply($$1xx -> $$1xx.map($$1x::toUserBan)))
         .toList();
      Set<BanlistService.UserBan> $$4 = ((List)Util.sequence($$3).join()).stream().flatMap(Optional::stream).collect(Collectors.toSet());
      Set<BanlistService.UserBan> $$5 = $$0.banListService()
         .getUserBanEntries()
         .stream()
         .filter($$0x -> $$0x.getUser() != null)
         .map(BanlistService.UserBan::from)
         .collect(Collectors.toSet());
      $$5.stream().filter($$1x -> !$$4.contains($$1x)).forEach($$2x -> $$0.banListService().removeUserBan($$2x.player(), $$2));
      $$4.stream().filter($$1x -> !$$5.contains($$1x)).forEach($$2x -> {
         $$0.banListService().addUserBan($$2x.toBanEntry(), $$2);
         ServerPlayer $$3x = $$0.playerListService().getPlayer($$2x.player().id());
         if ($$3x != null) {
            $$3x.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
         }
      });
      return get($$0);
   }

   record UserBan(NameAndId player, String reason, String source, Optional<Instant> expires) {
      static BanlistService.UserBan from(UserBanListEntry $$0) {
         return new BanlistService.UserBan(
            Objects.requireNonNull($$0.getUser()), $$0.getReason(), $$0.getSource(), Optional.ofNullable($$0.getExpires()).map(Date::toInstant)
         );
      }

      UserBanListEntry toBanEntry() {
         return new UserBanListEntry(
            new NameAndId(this.player().id(), this.player().name()), null, this.source(), this.expires().map(Date::from).orElse(null), this.reason()
         );
      }
   }

   public record UserBanDto(PlayerDto player, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
      public static final MapCodec<BanlistService.UserBanDto> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               PlayerDto.CODEC.codec().fieldOf("player").forGetter(BanlistService.UserBanDto::player),
               Codec.STRING.optionalFieldOf("reason").forGetter(BanlistService.UserBanDto::reason),
               Codec.STRING.optionalFieldOf("source").forGetter(BanlistService.UserBanDto::source),
               ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("expires").forGetter(BanlistService.UserBanDto::expires)
            )
            .apply($$0, BanlistService.UserBanDto::new)
      );

      private static BanlistService.UserBanDto from(BanlistService.UserBan $$0) {
         return new BanlistService.UserBanDto(PlayerDto.from($$0.player()), Optional.ofNullable($$0.reason()), Optional.of($$0.source()), $$0.expires());
      }

      public static BanlistService.UserBanDto from(UserBanListEntry $$0) {
         return from(BanlistService.UserBan.from($$0));
      }

      private BanlistService.UserBan toUserBan(NameAndId $$0) {
         return new BanlistService.UserBan($$0, this.reason().orElse(null), this.source().orElse("Management server"), this.expires());
      }
   }
}
