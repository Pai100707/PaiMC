package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetPlayerIdleTimeoutCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setidletimeout").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
            .then(
               Commands.argument("minutes", IntegerArgumentType.integer(0))
                  .executes($$0x -> setIdleTimeout((CommandSourceStack)$$0x.getSource(), IntegerArgumentType.getInteger($$0x, "minutes")))
            )
      );
   }

   private static int setIdleTimeout(CommandSourceStack $$0, int $$1) {
      $$0.getServer().setPlayerIdleTimeout($$1);
      if ($$1 > 0) {
         $$0.sendSuccess(() -> Component.translatable("commands.setidletimeout.success", new Object[]{$$1}), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.setidletimeout.success.disabled"), true);
      }

      return $$1;
   }
}
