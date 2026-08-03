package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;

public class ServerPackCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("serverpack")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .then(
                  Commands.literal("push")
                     .then(
                        ((RequiredArgumentBuilder)Commands.argument("url", StringArgumentType.string())
                              .then(
                                 ((RequiredArgumentBuilder)Commands.argument("uuid", UuidArgument.uuid())
                                       .then(
                                          Commands.argument("hash", StringArgumentType.word())
                                             .executes(
                                                $$0x -> pushPack(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   StringArgumentType.getString($$0x, "url"),
                                                   Optional.of(UuidArgument.getUuid($$0x, "uuid")),
                                                   Optional.of(StringArgumentType.getString($$0x, "hash"))
                                                )
                                             )
                                       ))
                                    .executes(
                                       $$0x -> pushPack(
                                          (CommandSourceStack)$$0x.getSource(),
                                          StringArgumentType.getString($$0x, "url"),
                                          Optional.of(UuidArgument.getUuid($$0x, "uuid")),
                                          Optional.empty()
                                       )
                                    )
                              ))
                           .executes(
                              $$0x -> pushPack(
                                 (CommandSourceStack)$$0x.getSource(), StringArgumentType.getString($$0x, "url"), Optional.empty(), Optional.empty()
                              )
                           )
                     )
               ))
            .then(
               Commands.literal("pop")
                  .then(
                     Commands.argument("uuid", UuidArgument.uuid())
                        .executes($$0x -> popPack((CommandSourceStack)$$0x.getSource(), UuidArgument.getUuid($$0x, "uuid")))
                  )
            )
      );
   }

   private static void sendToAllConnections(CommandSourceStack $$0, Packet<?> $$1) {
      $$0.getServer().getConnection().getConnections().forEach($$1x -> $$1x.send($$1));
   }

   private static int pushPack(CommandSourceStack $$0, String $$1, Optional<UUID> $$2, Optional<String> $$3) {
      UUID $$4 = $$2.orElseGet(() -> UUID.nameUUIDFromBytes($$1.getBytes(StandardCharsets.UTF_8)));
      String $$5 = $$3.orElse("");
      ClientboundResourcePackPushPacket $$6 = new ClientboundResourcePackPushPacket($$4, $$1, $$5, false, null);
      sendToAllConnections($$0, $$6);
      return 0;
   }

   private static int popPack(CommandSourceStack $$0, UUID $$1) {
      ClientboundResourcePackPopPacket $$2 = new ClientboundResourcePackPopPacket(Optional.of($$1));
      sendToAllConnections($$0, $$2);
      return 0;
   }
}
