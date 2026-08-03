package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

public class TitleCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("title").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                    "targets", EntityArgument.players()
                                 )
                                 .then(
                                    Commands.literal("clear")
                                       .executes($$0x -> clearTitle((CommandSourceStack)$$0x.getSource(), EntityArgument.getPlayers($$0x, "targets")))
                                 ))
                              .then(
                                 Commands.literal("reset")
                                    .executes($$0x -> resetTitle((CommandSourceStack)$$0x.getSource(), EntityArgument.getPlayers($$0x, "targets")))
                              ))
                           .then(
                              Commands.literal("title")
                                 .then(
                                    Commands.argument("title", ComponentArgument.textComponent($$1))
                                       .executes(
                                          $$0x -> showTitle(
                                             (CommandSourceStack)$$0x.getSource(),
                                             EntityArgument.getPlayers($$0x, "targets"),
                                             ComponentArgument.getRawComponent($$0x, "title"),
                                             "title",
                                             ClientboundSetTitleTextPacket::new
                                          )
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("subtitle")
                              .then(
                                 Commands.argument("title", ComponentArgument.textComponent($$1))
                                    .executes(
                                       $$0x -> showTitle(
                                          (CommandSourceStack)$$0x.getSource(),
                                          EntityArgument.getPlayers($$0x, "targets"),
                                          ComponentArgument.getRawComponent($$0x, "title"),
                                          "subtitle",
                                          ClientboundSetSubtitleTextPacket::new
                                       )
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("actionbar")
                           .then(
                              Commands.argument("title", ComponentArgument.textComponent($$1))
                                 .executes(
                                    $$0x -> showTitle(
                                       (CommandSourceStack)$$0x.getSource(),
                                       EntityArgument.getPlayers($$0x, "targets"),
                                       ComponentArgument.getRawComponent($$0x, "title"),
                                       "actionbar",
                                       ClientboundSetActionBarTextPacket::new
                                    )
                                 )
                           )
                     ))
                  .then(
                     Commands.literal("times")
                        .then(
                           Commands.argument("fadeIn", TimeArgument.time())
                              .then(
                                 Commands.argument("stay", TimeArgument.time())
                                    .then(
                                       Commands.argument("fadeOut", TimeArgument.time())
                                          .executes(
                                             $$0x -> setTimes(
                                                (CommandSourceStack)$$0x.getSource(),
                                                EntityArgument.getPlayers($$0x, "targets"),
                                                IntegerArgumentType.getInteger($$0x, "fadeIn"),
                                                IntegerArgumentType.getInteger($$0x, "stay"),
                                                IntegerArgumentType.getInteger($$0x, "fadeOut")
                                             )
                                          )
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static int clearTitle(CommandSourceStack $$0, Collection<ServerPlayer> $$1) {
      ClientboundClearTitlesPacket $$2 = new ClientboundClearTitlesPacket(false);

      for (ServerPlayer $$3 : $$1) {
         $$3.connection.send($$2);
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(() -> Component.translatable("commands.title.cleared.single", new Object[]{$$1.iterator().next().getDisplayName()}), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.title.cleared.multiple", new Object[]{$$1.size()}), true);
      }

      return $$1.size();
   }

   private static int resetTitle(CommandSourceStack $$0, Collection<ServerPlayer> $$1) {
      ClientboundClearTitlesPacket $$2 = new ClientboundClearTitlesPacket(true);

      for (ServerPlayer $$3 : $$1) {
         $$3.connection.send($$2);
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(() -> Component.translatable("commands.title.reset.single", new Object[]{$$1.iterator().next().getDisplayName()}), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.title.reset.multiple", new Object[]{$$1.size()}), true);
      }

      return $$1.size();
   }

   private static int showTitle(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Component $$2, String $$3, Function<Component, Packet<?>> $$4) throws CommandSyntaxException {
      for (ServerPlayer $$5 : $$1) {
         $$5.connection.send($$4.apply(ComponentUtils.updateForEntity($$0, $$2, $$5, 0)));
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(() -> Component.translatable("commands.title.show." + $$3 + ".single", new Object[]{$$1.iterator().next().getDisplayName()}), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.title.show." + $$3 + ".multiple", new Object[]{$$1.size()}), true);
      }

      return $$1.size();
   }

   private static int setTimes(CommandSourceStack $$0, Collection<ServerPlayer> $$1, int $$2, int $$3, int $$4) {
      ClientboundSetTitlesAnimationPacket $$5 = new ClientboundSetTitlesAnimationPacket($$2, $$3, $$4);

      for (ServerPlayer $$6 : $$1) {
         $$6.connection.send($$5);
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(() -> Component.translatable("commands.title.times.single", new Object[]{$$1.iterator().next().getDisplayName()}), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.title.times.multiple", new Object[]{$$1.size()}), true);
      }

      return $$1.size();
   }
}
