package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jspecify.annotations.Nullable;

public class DebugConfigCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("debugconfig")
                     .requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
                  .then(
                     Commands.literal("config")
                        .then(
                           Commands.argument("target", EntityArgument.player())
                              .executes($$0x -> config((CommandSourceStack)$$0x.getSource(), EntityArgument.getPlayer($$0x, "target")))
                        )
                  ))
               .then(
                  Commands.literal("unconfig")
                     .then(
                        Commands.argument("target", UuidArgument.uuid())
                           .suggests(
                              ($$0x, $$1x) -> SharedSuggestionProvider.suggest(getUuidsInConfig(((CommandSourceStack)$$0x.getSource()).getServer()), $$1x)
                           )
                           .executes($$0x -> unconfig((CommandSourceStack)$$0x.getSource(), UuidArgument.getUuid($$0x, "target")))
                     )
               ))
            .then(
               Commands.literal("dialog")
                  .then(
                     Commands.argument("target", UuidArgument.uuid())
                        .suggests(($$0x, $$1x) -> SharedSuggestionProvider.suggest(getUuidsInConfig(((CommandSourceStack)$$0x.getSource()).getServer()), $$1x))
                        .then(
                           Commands.argument("dialog", ResourceOrIdArgument.dialog($$1))
                              .executes(
                                 $$0x -> showDialog(
                                    (CommandSourceStack)$$0x.getSource(), UuidArgument.getUuid($$0x, "target"), ResourceOrIdArgument.getDialog($$0x, "dialog")
                                 )
                              )
                        )
                  )
            )
      );
   }

   private static Iterable<String> getUuidsInConfig(net.minecraft.server.MinecraftServer $$0) {
      Set<String> $$1 = new HashSet<>();

      for (Connection $$2 : $$0.getConnection().getConnections()) {
         if ($$2.getPacketListener() instanceof ServerConfigurationPacketListenerImpl $$3) {
            $$1.add($$3.getOwner().id().toString());
         }
      }

      return $$1;
   }

   private static int config(CommandSourceStack $$0, ServerPlayer $$1) {
      GameProfile $$2 = $$1.getGameProfile();
      $$1.connection.switchToConfig();
      $$0.sendSuccess(() -> Component.literal("Switched player " + $$2.name() + "(" + $$2.id() + ") to config mode"), false);
      return 1;
   }

   @Nullable
   private static ServerConfigurationPacketListenerImpl findConfigPlayer(net.minecraft.server.MinecraftServer $$0, UUID $$1) {
      for (Connection $$2 : $$0.getConnection().getConnections()) {
         if ($$2.getPacketListener() instanceof ServerConfigurationPacketListenerImpl $$3 && $$3.getOwner().id().equals($$1)) {
            return $$3;
         }
      }

      return null;
   }

   private static int unconfig(CommandSourceStack $$0, UUID $$1) {
      ServerConfigurationPacketListenerImpl $$2 = findConfigPlayer($$0.getServer(), $$1);
      if ($$2 != null) {
         $$2.returnToWorld();
         return 1;
      } else {
         $$0.sendFailure(Component.literal("Can't find player to unconfig"));
         return 0;
      }
   }

   private static int showDialog(CommandSourceStack $$0, UUID $$1, Holder<Dialog> $$2) {
      ServerConfigurationPacketListenerImpl $$3 = findConfigPlayer($$0.getServer(), $$1);
      if ($$3 != null) {
         $$3.send(new ClientboundShowDialogPacket($$2));
         return 1;
      } else {
         $$0.sendFailure(Component.literal("Can't find player to talk to"));
         return 0;
      }
   }
}
