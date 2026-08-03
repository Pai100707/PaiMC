package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.server.level.ServerPlayer;

public class TransferCommand {
   private static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType(
      Component.translatable("commands.transfer.error.no_players")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("transfer").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("hostname", StringArgumentType.string())
                     .executes(
                        $$0x -> transfer(
                           (CommandSourceStack)$$0x.getSource(),
                           StringArgumentType.getString($$0x, "hostname"),
                           25565,
                           List.of(((CommandSourceStack)$$0x.getSource()).getPlayerOrException())
                        )
                     ))
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("port", IntegerArgumentType.integer(1, 65535))
                           .executes(
                              $$0x -> transfer(
                                 (CommandSourceStack)$$0x.getSource(),
                                 StringArgumentType.getString($$0x, "hostname"),
                                 IntegerArgumentType.getInteger($$0x, "port"),
                                 List.of(((CommandSourceStack)$$0x.getSource()).getPlayerOrException())
                              )
                           ))
                        .then(
                           Commands.argument("players", EntityArgument.players())
                              .executes(
                                 $$0x -> transfer(
                                    (CommandSourceStack)$$0x.getSource(),
                                    StringArgumentType.getString($$0x, "hostname"),
                                    IntegerArgumentType.getInteger($$0x, "port"),
                                    EntityArgument.getPlayers($$0x, "players")
                                 )
                              )
                        )
                  )
            )
      );
   }

   private static int transfer(CommandSourceStack $$0, String $$1, int $$2, Collection<ServerPlayer> $$3) throws CommandSyntaxException {
      if ($$3.isEmpty()) {
         throw ERROR_NO_PLAYERS.create();
      } else {
         for (ServerPlayer $$4 : $$3) {
            $$4.connection.send(new ClientboundTransferPacket($$1, $$2));
         }

         if ($$3.size() == 1) {
            $$0.sendSuccess(
               () -> Component.translatable("commands.transfer.success.single", new Object[]{$$3.iterator().next().getDisplayName(), $$1, $$2}), true
            );
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.transfer.success.multiple", new Object[]{$$3.size(), $$1, $$2}), true);
         }

         return $$3.size();
      }
   }
}
