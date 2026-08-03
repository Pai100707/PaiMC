package net.minecraft.server.jsonrpc.methods;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus.Version;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.level.ServerPlayer;

public class ServerStateService {
   public static ServerStateService.ServerState status(MinecraftApi $$0) {
      return !$$0.serverStateService().isReady()
         ? ServerStateService.ServerState.NOT_STARTED
         : new ServerStateService.ServerState(true, PlayerService.get($$0), Version.current());
   }

   public static boolean save(MinecraftApi $$0, boolean $$1, ClientInfo $$2) {
      return $$0.serverStateService().saveEverything(true, $$1, true, $$2);
   }

   public static boolean stop(MinecraftApi $$0, ClientInfo $$1) {
      $$0.submit(() -> $$0.serverStateService().halt(false, $$1));
      return true;
   }

   public static boolean systemMessage(MinecraftApi $$0, ServerStateService.SystemMessage $$1, ClientInfo $$2) {
      Component $$3 = $$1.message().asComponent().orElse(null);
      if ($$3 == null) {
         return false;
      } else {
         if ($$1.receivingPlayers().isPresent()) {
            if ($$1.receivingPlayers().get().isEmpty()) {
               return false;
            }

            for (PlayerDto $$4 : $$1.receivingPlayers().get()) {
               ServerPlayer $$5;
               if ($$4.id().isPresent()) {
                  $$5 = $$0.playerListService().getPlayer($$4.id().get());
               } else {
                  if (!$$4.name().isPresent()) {
                     continue;
                  }

                  $$5 = $$0.playerListService().getPlayerByName($$4.name().get());
               }

               if ($$5 != null) {
                  $$5.sendSystemMessage($$3, $$1.overlay());
               }
            }
         } else {
            $$0.serverStateService().broadcastSystemMessage($$3, $$1.overlay(), $$2);
         }

         return true;
      }
   }

   public record ServerState(boolean started, List<PlayerDto> players, Version version) {
      public static final Codec<ServerStateService.ServerState> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.BOOL.fieldOf("started").forGetter(ServerStateService.ServerState::started),
               PlayerDto.CODEC.codec().listOf().lenientOptionalFieldOf("players", List.of()).forGetter(ServerStateService.ServerState::players),
               Version.CODEC.fieldOf("version").forGetter(ServerStateService.ServerState::version)
            )
            .apply($$0, ServerStateService.ServerState::new)
      );
      public static final ServerStateService.ServerState NOT_STARTED = new ServerStateService.ServerState(false, List.of(), Version.current());
   }

   public record SystemMessage(Message message, boolean overlay, Optional<List<PlayerDto>> receivingPlayers) {
      public static final Codec<ServerStateService.SystemMessage> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Message.CODEC.fieldOf("message").forGetter(ServerStateService.SystemMessage::message),
               Codec.BOOL.fieldOf("overlay").forGetter(ServerStateService.SystemMessage::overlay),
               PlayerDto.CODEC.codec().listOf().lenientOptionalFieldOf("receivingPlayers").forGetter(ServerStateService.SystemMessage::receivingPlayers)
            )
            .apply($$0, ServerStateService.SystemMessage::new)
      );
   }
}
