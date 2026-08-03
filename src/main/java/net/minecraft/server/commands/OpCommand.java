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

public class OpCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_OP = new SimpleCommandExceptionType(Component.translatable("commands.op.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("op").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
            .then(
               Commands.argument("targets", GameProfileArgument.gameProfile())
                  .suggests(
                     ($$0x, $$1) -> {
                        PlayerList $$2 = ((CommandSourceStack)$$0x.getSource()).getServer().getPlayerList();
                        return SharedSuggestionProvider.suggest(
                           $$2.getPlayers().stream().filter($$1x -> !$$2.isOp($$1x.nameAndId())).map($$0xx -> $$0xx.getGameProfile().name()), $$1
                        );
                     }
                  )
                  .executes($$0x -> opPlayers((CommandSourceStack)$$0x.getSource(), GameProfileArgument.getGameProfiles($$0x, "targets")))
            )
      );
   }

   private static int opPlayers(CommandSourceStack $$0, Collection<NameAndId> $$1) throws CommandSyntaxException {
      PlayerList $$2 = $$0.getServer().getPlayerList();
      int $$3 = 0;

      for (NameAndId $$4 : $$1) {
         if (!$$2.isOp($$4)) {
            $$2.op($$4);
            $$3++;
            $$0.sendSuccess(() -> Component.translatable("commands.op.success", new Object[]{$$4.name()}), true);
         }
      }

      if ($$3 == 0) {
         throw ERROR_ALREADY_OP.create();
      } else {
         return $$3;
      }
   }
}
