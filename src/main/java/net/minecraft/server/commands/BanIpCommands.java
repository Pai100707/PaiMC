package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

public class BanIpCommands {
   private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType(Component.translatable("commands.banip.invalid"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.banip.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("ban-ip").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("target", StringArgumentType.word())
                     .executes($$0x -> banIpOrName((CommandSourceStack)$$0x.getSource(), StringArgumentType.getString($$0x, "target"), null)))
                  .then(
                     Commands.argument("reason", MessageArgument.message())
                        .executes(
                           $$0x -> banIpOrName(
                              (CommandSourceStack)$$0x.getSource(), StringArgumentType.getString($$0x, "target"), MessageArgument.getMessage($$0x, "reason")
                           )
                        )
                  )
            )
      );
   }

   private static int banIpOrName(CommandSourceStack $$0, String $$1, Component $$2) throws CommandSyntaxException {
      if (InetAddresses.isInetAddress($$1)) {
         return banIp($$0, $$1, $$2);
      } else {
         ServerPlayer $$3 = $$0.getServer().getPlayerList().getPlayerByName($$1);
         if ($$3 != null) {
            return banIp($$0, $$3.getIpAddress(), $$2);
         } else {
            throw ERROR_INVALID_IP.create();
         }
      }
   }

   private static int banIp(CommandSourceStack $$0, String $$1, Component $$2) throws CommandSyntaxException {
      IpBanList $$3 = $$0.getServer().getPlayerList().getIpBans();
      if ($$3.isBanned($$1)) {
         throw ERROR_ALREADY_BANNED.create();
      } else {
         List<ServerPlayer> $$4 = $$0.getServer().getPlayerList().getPlayersWithAddress($$1);
         IpBanListEntry $$5 = new IpBanListEntry($$1, null, $$0.getTextName(), null, $$2 == null ? null : $$2.getString());
         $$3.add($$5);
         $$0.sendSuccess(() -> Component.translatable("commands.banip.success", new Object[]{$$1, $$5.getReasonMessage()}), true);
         if (!$$4.isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.banip.info", new Object[]{$$4.size(), EntitySelector.joinNames($$4)}), true);
         }

         for (ServerPlayer $$6 : $$4) {
            $$6.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned"));
         }

         return $$4.size();
      }
   }
}
