package net.minecraft.server.jsonrpc.methods;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.util.Util;

public class OperatorService {
   public static List<OperatorService.OperatorDto> get(MinecraftApi $$0) {
      return $$0.operatorListService().getEntries().stream().filter($$0x -> $$0x.getUser() != null).map(OperatorService.OperatorDto::from).toList();
   }

   public static List<OperatorService.OperatorDto> clear(MinecraftApi $$0, ClientInfo $$1) {
      $$0.operatorListService().clear($$1);
      return get($$0);
   }

   public static List<OperatorService.OperatorDto> remove(MinecraftApi $$0, List<PlayerDto> $$1, ClientInfo $$2) {
      List<CompletableFuture<Optional<NameAndId>>> $$3 = $$1.stream().map($$1x -> $$0.playerListService().getUser($$1x.id(), $$1x.name())).toList();

      for (Optional<NameAndId> $$4 : (List)Util.sequence($$3).join()) {
         $$4.ifPresent($$2x -> $$0.operatorListService().deop($$2x, $$2));
      }

      return get($$0);
   }

   public static List<OperatorService.OperatorDto> add(MinecraftApi $$0, List<OperatorService.OperatorDto> $$1, ClientInfo $$2) {
      List<CompletableFuture<Optional<OperatorService.Op>>> $$3 = $$1.stream()
         .map(
            $$1x -> $$0.playerListService()
               .getUser($$1x.player().id(), $$1x.player().name())
               .thenApply($$1xx -> $$1xx.map($$1xxx -> new OperatorService.Op($$1xxx, $$1x.permissionLevel(), $$1x.bypassesPlayerLimit())))
         )
         .toList();

      for (Optional<OperatorService.Op> $$4 : (List)Util.sequence($$3).join()) {
         $$4.ifPresent($$2x -> $$0.operatorListService().op($$2x.user(), $$2x.permissionLevel(), $$2x.bypassesPlayerLimit(), $$2));
      }

      return get($$0);
   }

   public static List<OperatorService.OperatorDto> set(MinecraftApi $$0, List<OperatorService.OperatorDto> $$1, ClientInfo $$2) {
      List<CompletableFuture<Optional<OperatorService.Op>>> $$3 = $$1.stream()
         .map(
            $$1x -> $$0.playerListService()
               .getUser($$1x.player().id(), $$1x.player().name())
               .thenApply($$1xx -> $$1xx.map($$1xxx -> new OperatorService.Op($$1xxx, $$1x.permissionLevel(), $$1x.bypassesPlayerLimit())))
         )
         .toList();
      Set<OperatorService.Op> $$4 = ((List)Util.sequence($$3).join()).stream().flatMap(Optional::stream).collect(Collectors.toSet());
      Set<OperatorService.Op> $$5 = $$0.operatorListService()
         .getEntries()
         .stream()
         .filter($$0x -> $$0x.getUser() != null)
         .map($$0x -> new OperatorService.Op($$0x.getUser(), Optional.of($$0x.permissions().level()), Optional.of($$0x.getBypassesPlayerLimit())))
         .collect(Collectors.toSet());
      $$5.stream().filter($$1x -> !$$4.contains($$1x)).forEach($$2x -> $$0.operatorListService().deop($$2x.user(), $$2));
      $$4.stream()
         .filter($$1x -> !$$5.contains($$1x))
         .forEach($$2x -> $$0.operatorListService().op($$2x.user(), $$2x.permissionLevel(), $$2x.bypassesPlayerLimit(), $$2));
      return get($$0);
   }

   record Op(NameAndId user, Optional<PermissionLevel> permissionLevel, Optional<Boolean> bypassesPlayerLimit) {
   }

   public record OperatorDto(PlayerDto player, Optional<PermissionLevel> permissionLevel, Optional<Boolean> bypassesPlayerLimit) {
      public static final MapCodec<OperatorService.OperatorDto> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               PlayerDto.CODEC.codec().fieldOf("player").forGetter(OperatorService.OperatorDto::player),
               PermissionLevel.INT_CODEC.optionalFieldOf("permissionLevel").forGetter(OperatorService.OperatorDto::permissionLevel),
               Codec.BOOL.optionalFieldOf("bypassesPlayerLimit").forGetter(OperatorService.OperatorDto::bypassesPlayerLimit)
            )
            .apply($$0, OperatorService.OperatorDto::new)
      );

      public static OperatorService.OperatorDto from(ServerOpListEntry $$0) {
         return new OperatorService.OperatorDto(
            PlayerDto.from(Objects.requireNonNull($$0.getUser())), Optional.of($$0.permissions().level()), Optional.of($$0.getBypassesPlayerLimit())
         );
      }
   }
}
