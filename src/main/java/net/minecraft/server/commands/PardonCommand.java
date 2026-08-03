package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;

public class PardonCommand {
   private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.pardon.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("pardon").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
            .then(
               Commands.argument("targets", GameProfileArgument.gameProfile())
                  .suggests(
                     ($$0x, $$1) -> SharedSuggestionProvider.suggest(
                        ((CommandSourceStack)$$0x.getSource()).getServer().getPlayerList().getBans().getUserList(), $$1
                     )
                  )
                  .executes($$0x -> pardonPlayers((CommandSourceStack)$$0x.getSource(), GameProfileArgument.getGameProfiles($$0x, "targets")))
            )
      );
   }

   private static int pardonPlayers(CommandSourceStack $$0, Collection<NameAndId> $$1) throws CommandSyntaxException {
      UserBanList $$2 = $$0.getServer().getPlayerList().getBans();
      int $$3 = 0;

      for (NameAndId $$4 : $$1) {
         if ($$2.isBanned($$4)) {
            $$2.remove($$4);
            $$3++;
            $$0.sendSuccess(() -> Component.translatable("commands.pardon.success", new Object[]{Component.literal($$4.name())}), true);
         }
      }

      if ($$3 == 0) {
         throw ERROR_NOT_BANNED.create();
      } else {
         return $$3;
      }
   }
}
