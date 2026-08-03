package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class TellRawCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tellraw").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               Commands.argument("targets", EntityArgument.players())
                  .then(Commands.argument("message", ComponentArgument.textComponent($$1)).executes($$0x -> {
                     int $$1x = 0;

                     for (ServerPlayer $$2 : EntityArgument.getPlayers($$0x, "targets")) {
                        $$2.sendSystemMessage(ComponentArgument.getResolvedComponent($$0x, "message", $$2), false);
                        $$1x++;
                     }

                     return $$1x;
                  }))
            )
      );
   }
}
