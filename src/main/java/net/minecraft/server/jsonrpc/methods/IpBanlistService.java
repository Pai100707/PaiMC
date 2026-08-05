package net.minecraft.server.jsonrpc.methods;

import com.google.common.net.InetAddresses;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.util.ExtraCodecs;

public class IpBanlistService {
   private static final String BAN_SOURCE = "Management server";

   public static List<IpBanlistService.IpBanDto> get(MinecraftApi $$0) {
      return $$0.banListService().getIpBanEntries().stream().map(IpBanlistService.IpBan::from).map(IpBanlistService.IpBanDto::from).toList();
   }

   public static List<IpBanlistService.IpBanDto> add(MinecraftApi $$0, List<IpBanlistService.IncomingIpBanDto> $$1, ClientInfo $$2) {
      $$1.stream()
         .map($$2x -> banIp($$0, $$2x, $$2))
         .flatMap(Collection::stream)
         .forEach($$0x -> $$0x.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned")));
      return get($$0);
   }

   private static List<ServerPlayer> banIp(MinecraftApi $$0, IpBanlistService.IncomingIpBanDto $$1, ClientInfo $$2) {
      IpBanlistService.IpBan $$3 = $$1.toIpBan();
      if ($$3 != null) {
         return banIp($$0, $$3, $$2);
      } else {
         if ($$1.player().isPresent()) {
            Optional<ServerPlayer> $$4 = $$0.playerListService().getPlayer($$1.player().get().id(), $$1.player().get().name());
            if ($$4.isPresent()) {
               return banIp($$0, $$1.toIpBan($$4.get()), $$2);
            }
         }

         return List.of();
      }
   }

   private static List<ServerPlayer> banIp(MinecraftApi $$0, IpBanlistService.IpBan $$1, ClientInfo $$2) {
      $$0.banListService().addIpBan($$1.toIpBanEntry(), $$2);
      return $$0.playerListService().getPlayersWithAddress($$1.ip());
   }

   public static List<IpBanlistService.IpBanDto> clear(MinecraftApi $$0, ClientInfo $$1) {
      $$0.banListService().clearIpBans($$1);
      return get($$0);
   }

   public static List<IpBanlistService.IpBanDto> remove(MinecraftApi $$0, List<String> $$1, ClientInfo $$2) {
      $$1.forEach($$2x -> $$0.banListService().removeIpBan($$2x, $$2));
      return get($$0);
   }

   public static List<IpBanlistService.IpBanDto> set(MinecraftApi $$0, List<IpBanlistService.IpBanDto> $$1, ClientInfo $$2) {
      Set<IpBanlistService.IpBan> $$3 = $$1.stream()
         .filter($$0x -> InetAddresses.isInetAddress($$0x.ip()))
         .map(IpBanlistService.IpBanDto::toIpBan)
         .collect(Collectors.toSet());
      Set<IpBanlistService.IpBan> $$4 = $$0.banListService().getIpBanEntries().stream().map(IpBanlistService.IpBan::from).collect(Collectors.toSet());
      $$4.stream().filter($$1x -> !$$3.contains($$1x)).forEach($$2x -> $$0.banListService().removeIpBan($$2x.ip(), $$2));
      $$3.stream().filter($$1x -> !$$4.contains($$1x)).forEach($$2x -> $$0.banListService().addIpBan($$2x.toIpBanEntry(), $$2));
      $$3.stream()
         .filter($$1x -> !$$4.contains($$1x))
         .flatMap($$1x -> $$0.playerListService().getPlayersWithAddress($$1x.ip()).stream())
         .forEach($$0x -> $$0x.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned")));
      return get($$0);
   }

   public record IncomingIpBanDto(Optional<PlayerDto> player, Optional<String> ip, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
      public static final MapCodec<IpBanlistService.IncomingIpBanDto> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               PlayerDto.CODEC.codec().optionalFieldOf("player").forGetter(IpBanlistService.IncomingIpBanDto::player),
               Codec.STRING.optionalFieldOf("ip").forGetter(IpBanlistService.IncomingIpBanDto::ip),
               Codec.STRING.optionalFieldOf("reason").forGetter(IpBanlistService.IncomingIpBanDto::reason),
               Codec.STRING.optionalFieldOf("source").forGetter(IpBanlistService.IncomingIpBanDto::source),
               ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("expires").forGetter(IpBanlistService.IncomingIpBanDto::expires)
            )
            .apply($$0, IpBanlistService.IncomingIpBanDto::new)
      );

      IpBanlistService.IpBan toIpBan(ServerPlayer $$0) {
         return new IpBanlistService.IpBan($$0.getIpAddress(), this.reason().orElse(null), this.source().orElse("Management server"), this.expires());
      }

      
      IpBanlistService.IpBan toIpBan() {
         return !this.ip().isEmpty() && InetAddresses.isInetAddress(this.ip().get())
            ? new IpBanlistService.IpBan(this.ip().get(), this.reason().orElse(null), this.source().orElse("Management server"), this.expires())
            : null;
      }
   }

   record IpBan(String ip, String reason, String source, Optional<Instant> expires) {
      static IpBanlistService.IpBan from(IpBanListEntry $$0) {
         return new IpBanlistService.IpBan(
            Objects.requireNonNull($$0.getUser()), $$0.getReason(), $$0.getSource(), Optional.ofNullable($$0.getExpires()).map(Date::toInstant)
         );
      }

      IpBanListEntry toIpBanEntry() {
         return new IpBanListEntry(this.ip(), null, this.source(), this.expires().map(Date::from).orElse(null), this.reason());
      }
   }

   public record IpBanDto(String ip, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
      public static final MapCodec<IpBanlistService.IpBanDto> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.STRING.fieldOf("ip").forGetter(IpBanlistService.IpBanDto::ip),
               Codec.STRING.optionalFieldOf("reason").forGetter(IpBanlistService.IpBanDto::reason),
               Codec.STRING.optionalFieldOf("source").forGetter(IpBanlistService.IpBanDto::source),
               ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("expires").forGetter(IpBanlistService.IpBanDto::expires)
            )
            .apply($$0, IpBanlistService.IpBanDto::new)
      );

      private static IpBanlistService.IpBanDto from(IpBanlistService.IpBan $$0) {
         return new IpBanlistService.IpBanDto($$0.ip(), Optional.ofNullable($$0.reason()), Optional.of($$0.source()), $$0.expires());
      }

      public static IpBanlistService.IpBanDto from(IpBanListEntry $$0) {
         return from(IpBanlistService.IpBan.from($$0));
      }

      private IpBanlistService.IpBan toIpBan() {
         return new IpBanlistService.IpBan(this.ip(), this.reason().orElse(null), this.source().orElse("Management server"), this.expires());
      }
   }
}
