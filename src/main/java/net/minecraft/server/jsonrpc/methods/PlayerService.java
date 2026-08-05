package net.minecraft.server.jsonrpc.methods;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.level.ServerPlayer;

public class PlayerService {
   private static final Component DEFAULT_KICK_MESSAGE = Component.translatable("multiplayer.disconnect.kicked");

   public static List<PlayerDto> get(MinecraftApi $$0) {
      return $$0.playerListService().getPlayers().stream().map(PlayerDto::from).toList();
   }

   public static List<PlayerDto> kick(MinecraftApi $$0, List<PlayerService.KickDto> $$1, ClientInfo $$2) {
      List<PlayerDto> $$3 = new ArrayList<>();

      for (PlayerService.KickDto $$4 : $$1) {
         ServerPlayer $$5 = getServerPlayer($$0, $$4.player());
         if ($$5 != null) {
            $$0.playerListService().remove($$5, $$2);
            $$5.connection.disconnect($$4.message.flatMap(Message::asComponent).orElse(DEFAULT_KICK_MESSAGE));
            $$3.add($$4.player());
         }
      }

      return $$3;
   }

   
   private static ServerPlayer getServerPlayer(MinecraftApi $$0, PlayerDto $$1) {
      if ($$1.id().isPresent()) {
         return $$0.playerListService().getPlayer($$1.id().get());
      } else {
         return $$1.name().isPresent() ? $$0.playerListService().getPlayerByName($$1.name().get()) : null;
      }
   }

   public record KickDto(PlayerDto player, Optional<Message> message) {
      public static final MapCodec<PlayerService.KickDto> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               PlayerDto.CODEC.codec().fieldOf("player").forGetter(PlayerService.KickDto::player),
               Message.CODEC.optionalFieldOf("message").forGetter(PlayerService.KickDto::message)
            )
            .apply($$0, PlayerService.KickDto::new)
      );
   }
}
