package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SpectateCommand {
   private static final SimpleCommandExceptionType ERROR_SELF = new SimpleCommandExceptionType(Component.translatable("commands.spectate.self"));
   private static final DynamicCommandExceptionType ERROR_NOT_SPECTATOR = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.spectate.not_spectator", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_CANNOT_SPECTATE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.spectate.cannot_spectate", new Object[]{$$0})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spectate")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .executes($$0x -> spectate((CommandSourceStack)$$0x.getSource(), null, ((CommandSourceStack)$$0x.getSource()).getPlayerOrException())))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("target", EntityArgument.entity())
                     .executes(
                        $$0x -> spectate(
                           (CommandSourceStack)$$0x.getSource(),
                           EntityArgument.getEntity($$0x, "target"),
                           ((CommandSourceStack)$$0x.getSource()).getPlayerOrException()
                        )
                     ))
                  .then(
                     Commands.argument("player", EntityArgument.player())
                        .executes(
                           $$0x -> spectate(
                              (CommandSourceStack)$$0x.getSource(), EntityArgument.getEntity($$0x, "target"), EntityArgument.getPlayer($$0x, "player")
                           )
                        )
                  )
            )
      );
   }

   private static int spectate(CommandSourceStack $$0, Entity $$1, ServerPlayer $$2) throws CommandSyntaxException {
      if ($$2 == $$1) {
         throw ERROR_SELF.create();
      } else if (!$$2.isSpectator()) {
         throw ERROR_NOT_SPECTATOR.create($$2.getDisplayName());
      } else if ($$1 != null && $$1.getType().clientTrackingRange() == 0) {
         throw ERROR_CANNOT_SPECTATE.create($$1.getDisplayName());
      } else {
         $$2.setCamera($$1);
         if ($$1 != null) {
            $$0.sendSuccess(() -> Component.translatable("commands.spectate.success.started", new Object[]{$$1.getDisplayName()}), false);
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.spectate.success.stopped"), false);
         }

         return 1;
      }
   }
}
