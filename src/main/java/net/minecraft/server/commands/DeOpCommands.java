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
import net.minecraft.server.players.PlayerList;

public class DeOpCommands {
   private static final SimpleCommandExceptionType ERROR_NOT_OP = new SimpleCommandExceptionType(Component.translatable("commands.deop.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("deop").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
            .then(
               Commands.argument("targets", GameProfileArgument.gameProfile())
                  .suggests(
                     ($$0x, $$1) -> SharedSuggestionProvider.suggest(((CommandSourceStack)$$0x.getSource()).getServer().getPlayerList().getOpNames(), $$1)
                  )
                  .executes($$0x -> deopPlayers((CommandSourceStack)$$0x.getSource(), GameProfileArgument.getGameProfiles($$0x, "targets")))
            )
      );
   }

   private static int deopPlayers(CommandSourceStack $$0, Collection<NameAndId> $$1) throws CommandSyntaxException {
      PlayerList $$2 = $$0.getServer().getPlayerList();
      int $$3 = 0;

      for (NameAndId $$4 : $$1) {
         if ($$2.isOp($$4)) {
            $$2.deop($$4);
            $$3++;
            $$0.sendSuccess(() -> Component.translatable("commands.deop.success", new Object[]{$$1.iterator().next().name()}), true);
         }
      }

      if ($$3 == 0) {
         throw ERROR_NOT_OP.create();
      } else {
         $$0.getServer().kickUnlistedPlayers();
         return $$3;
      }
   }
}
