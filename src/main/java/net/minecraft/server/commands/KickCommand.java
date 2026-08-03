package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class KickCommand {
   private static final SimpleCommandExceptionType ERROR_KICKING_OWNER = new SimpleCommandExceptionType(Component.translatable("commands.kick.owner.failed"));
   private static final SimpleCommandExceptionType ERROR_SINGLEPLAYER = new SimpleCommandExceptionType(
      Component.translatable("commands.kick.singleplayer.failed")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("kick").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players())
                     .executes(
                        $$0x -> kickPlayers(
                           (CommandSourceStack)$$0x.getSource(),
                           EntityArgument.getPlayers($$0x, "targets"),
                           Component.translatable("multiplayer.disconnect.kicked")
                        )
                     ))
                  .then(
                     Commands.argument("reason", MessageArgument.message())
                        .executes(
                           $$0x -> kickPlayers(
                              (CommandSourceStack)$$0x.getSource(), EntityArgument.getPlayers($$0x, "targets"), MessageArgument.getMessage($$0x, "reason")
                           )
                        )
                  )
            )
      );
   }

   private static int kickPlayers(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Component $$2) throws CommandSyntaxException {
      if (!$$0.getServer().isPublished()) {
         throw ERROR_SINGLEPLAYER.create();
      } else {
         int $$3 = 0;

         for (ServerPlayer $$4 : $$1) {
            if (!$$0.getServer().isSingleplayerOwner($$4.nameAndId())) {
               $$4.connection.disconnect($$2);
               $$0.sendSuccess(() -> Component.translatable("commands.kick.success", new Object[]{$$4.getDisplayName(), $$2}), true);
               $$3++;
            }
         }

         if ($$3 == 0) {
            throw ERROR_KICKING_OWNER.create();
         } else {
            return $$3;
         }
      }
   }
}
