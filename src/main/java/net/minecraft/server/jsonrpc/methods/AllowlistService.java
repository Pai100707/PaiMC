package net.minecraft.server.jsonrpc.methods;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.util.Util;

public class AllowlistService {
   public static List<PlayerDto> get(MinecraftApi $$0) {
      return $$0.allowListService().getEntries().stream().filter($$0x -> $$0x.getUser() != null).map($$0x -> PlayerDto.from($$0x.getUser())).toList();
   }

   public static List<PlayerDto> add(MinecraftApi $$0, List<PlayerDto> $$1, ClientInfo $$2) {
      List<CompletableFuture<Optional<NameAndId>>> $$3 = $$1.stream().map($$1x -> $$0.playerListService().getUser($$1x.id(), $$1x.name())).toList();

      for (Optional<NameAndId> $$4 : (List)Util.sequence($$3).join()) {
         $$4.ifPresent($$2x -> $$0.allowListService().add(new UserWhiteListEntry($$2x), $$2));
      }

      return get($$0);
   }

   public static List<PlayerDto> clear(MinecraftApi $$0, ClientInfo $$1) {
      $$0.allowListService().clear($$1);
      return get($$0);
   }

   public static List<PlayerDto> remove(MinecraftApi $$0, List<PlayerDto> $$1, ClientInfo $$2) {
      List<CompletableFuture<Optional<NameAndId>>> $$3 = $$1.stream().map($$1x -> $$0.playerListService().getUser($$1x.id(), $$1x.name())).toList();

      for (Optional<NameAndId> $$4 : (List)Util.sequence($$3).join()) {
         $$4.ifPresent($$2x -> $$0.allowListService().remove($$2x, $$2));
      }

      $$0.allowListService().kickUnlistedPlayers($$2);
      return get($$0);
   }

   public static List<PlayerDto> set(MinecraftApi $$0, List<PlayerDto> $$1, ClientInfo $$2) {
      List<CompletableFuture<Optional<NameAndId>>> $$3 = $$1.stream().map($$1x -> $$0.playerListService().getUser($$1x.id(), $$1x.name())).toList();
      Set<NameAndId> $$4 = ((List)Util.sequence($$3).join()).stream().flatMap(Optional::stream).collect(Collectors.toSet());
      Set<NameAndId> $$5 = $$0.allowListService().getEntries().stream().map(StoredUserEntry::getUser).collect(Collectors.toSet());
      $$5.stream().filter($$1x -> !$$4.contains($$1x)).forEach($$2x -> $$0.allowListService().remove($$2x, $$2));
      $$4.stream().filter($$1x -> !$$5.contains($$1x)).forEach($$2x -> $$0.allowListService().add(new UserWhiteListEntry($$2x), $$2));
      $$0.allowListService().kickUnlistedPlayers($$2);
      return get($$0);
   }
}
