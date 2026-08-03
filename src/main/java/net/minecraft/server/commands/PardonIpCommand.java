package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.IpBanList;

public class PardonIpCommand {
   private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("commands.pardonip.invalid"));
   private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.pardonip.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("pardon-ip").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
            .then(
               Commands.argument("target", StringArgumentType.word())
                  .suggests(
                     ($$0x, $$1) -> SharedSuggestionProvider.suggest(
                        ((CommandSourceStack)$$0x.getSource()).getServer().getPlayerList().getIpBans().getUserList(), $$1
                     )
                  )
                  .executes($$0x -> unban((CommandSourceStack)$$0x.getSource(), StringArgumentType.getString($$0x, "target")))
            )
      );
   }

   private static int unban(CommandSourceStack $$0, String $$1) throws CommandSyntaxException {
      if (!InetAddresses.isInetAddress($$1)) {
         throw ERROR_INVALID.create();
      } else {
         IpBanList $$2 = $$0.getServer().getPlayerList().getIpBans();
         if (!$$2.isBanned($$1)) {
            throw ERROR_NOT_BANNED.create();
         } else {
            $$2.remove($$1);
            $$0.sendSuccess(() -> Component.translatable("commands.pardonip.success", new Object[]{$$1}), true);
            return 1;
         }
      }
   }
}
