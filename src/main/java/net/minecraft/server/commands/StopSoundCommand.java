package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

public class StopSoundCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      RequiredArgumentBuilder<CommandSourceStack, EntitySelector> $$1 = (RequiredArgumentBuilder<CommandSourceStack, EntitySelector>)((RequiredArgumentBuilder)Commands.argument(
               "targets", EntityArgument.players()
            )
            .executes($$0x -> stopSound((CommandSourceStack)$$0x.getSource(), EntityArgument.getPlayers($$0x, "targets"), null, null)))
         .then(
            Commands.literal("*")
               .then(
                  Commands.argument("sound", IdentifierArgument.id())
                     .suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS))
                     .executes(
                        $$0x -> stopSound(
                           (CommandSourceStack)$$0x.getSource(), EntityArgument.getPlayers($$0x, "targets"), null, IdentifierArgument.getId($$0x, "sound")
                        )
                     )
               )
         );

      for (SoundSource $$2 : SoundSource.values()) {
         $$1.then(
            ((LiteralArgumentBuilder)Commands.literal($$2.getName())
                  .executes($$1x -> stopSound((CommandSourceStack)$$1x.getSource(), EntityArgument.getPlayers($$1x, "targets"), $$2, null)))
               .then(
                  Commands.argument("sound", IdentifierArgument.id())
                     .suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS))
                     .executes(
                        $$1x -> stopSound(
                           (CommandSourceStack)$$1x.getSource(), EntityArgument.getPlayers($$1x, "targets"), $$2, IdentifierArgument.getId($$1x, "sound")
                        )
                     )
               )
         );
      }

      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("stopsound").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then($$1)
      );
   }

   private static int stopSound(CommandSourceStack $$0, Collection<ServerPlayer> $$1, SoundSource $$2, Identifier $$3) {
      ClientboundStopSoundPacket $$4 = new ClientboundStopSoundPacket($$3, $$2);

      for (ServerPlayer $$5 : $$1) {
         $$5.connection.send($$4);
      }

      if ($$2 != null) {
         if ($$3 != null) {
            $$0.sendSuccess(
               () -> Component.translatable("commands.stopsound.success.source.sound", new Object[]{Component.translationArg($$3), $$2.getName()}), true
            );
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.stopsound.success.source.any", new Object[]{$$2.getName()}), true);
         }
      } else if ($$3 != null) {
         $$0.sendSuccess(() -> Component.translatable("commands.stopsound.success.sourceless.sound", new Object[]{Component.translationArg($$3)}), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.stopsound.success.sourceless.any"), true);
      }

      return $$1.size();
   }
}
