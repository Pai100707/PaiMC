package net.minecraft.server.commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.chase.ChaseClient;
import net.minecraft.server.chase.ChaseServer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChaseCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String DEFAULT_CONNECT_HOST = "localhost";
   private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
   private static final int DEFAULT_PORT = 10000;
   private static final int BROADCAST_INTERVAL_MS = 100;
   public static BiMap<String, ResourceKey<Level>> DIMENSION_NAMES = ImmutableBiMap.of("o", Level.OVERWORLD, "n", Level.NETHER, "e", Level.END);
   @Nullable
   private static ChaseServer chaseServer;
   @Nullable
   private static ChaseClient chaseClient;

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("chase")
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("follow")
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("host", StringArgumentType.string())
                                    .executes($$0x -> follow((CommandSourceStack)$$0x.getSource(), StringArgumentType.getString($$0x, "host"), 10000)))
                                 .then(
                                    Commands.argument("port", IntegerArgumentType.integer(1, 65535))
                                       .executes(
                                          $$0x -> follow(
                                             (CommandSourceStack)$$0x.getSource(),
                                             StringArgumentType.getString($$0x, "host"),
                                             IntegerArgumentType.getInteger($$0x, "port")
                                          )
                                       )
                                 )
                           ))
                        .executes($$0x -> follow((CommandSourceStack)$$0x.getSource(), "localhost", 10000))
                  ))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("lead")
                        .then(
                           ((RequiredArgumentBuilder)Commands.argument("bind_address", StringArgumentType.string())
                                 .executes($$0x -> lead((CommandSourceStack)$$0x.getSource(), StringArgumentType.getString($$0x, "bind_address"), 10000)))
                              .then(
                                 Commands.argument("port", IntegerArgumentType.integer(1024, 65535))
                                    .executes(
                                       $$0x -> lead(
                                          (CommandSourceStack)$$0x.getSource(),
                                          StringArgumentType.getString($$0x, "bind_address"),
                                          IntegerArgumentType.getInteger($$0x, "port")
                                       )
                                    )
                              )
                        ))
                     .executes($$0x -> lead((CommandSourceStack)$$0x.getSource(), "0.0.0.0", 10000))
               ))
            .then(Commands.literal("stop").executes($$0x -> stop((CommandSourceStack)$$0x.getSource())))
      );
   }

   private static int stop(CommandSourceStack $$0) {
      if (chaseClient != null) {
         chaseClient.stop();
         $$0.sendSuccess(() -> Component.literal("You have now stopped chasing"), false);
         chaseClient = null;
      }

      if (chaseServer != null) {
         chaseServer.stop();
         $$0.sendSuccess(() -> Component.literal("You are no longer being chased"), false);
         chaseServer = null;
      }

      return 0;
   }

   private static boolean alreadyRunning(CommandSourceStack $$0) {
      if (chaseServer != null) {
         $$0.sendFailure(Component.literal("Chase server is already running. Stop it using /chase stop"));
         return true;
      } else if (chaseClient != null) {
         $$0.sendFailure(Component.literal("You are already chasing someone. Stop it using /chase stop"));
         return true;
      } else {
         return false;
      }
   }

   private static int lead(CommandSourceStack $$0, String $$1, int $$2) {
      if (alreadyRunning($$0)) {
         return 0;
      } else {
         chaseServer = new ChaseServer($$1, $$2, $$0.getServer().getPlayerList(), 100);

         try {
            chaseServer.start();
            $$0.sendSuccess(
               () -> Component.literal("Chase server is now running on port " + $$2 + ". Clients can follow you using /chase follow <ip> <port>"), false
            );
         } catch (IOException var4) {
            LOGGER.error("Failed to start chase server", var4);
            $$0.sendFailure(Component.literal("Failed to start chase server on port " + $$2));
            chaseServer = null;
         }

         return 0;
      }
   }

   private static int follow(CommandSourceStack $$0, String $$1, int $$2) {
      if (alreadyRunning($$0)) {
         return 0;
      } else {
         chaseClient = new ChaseClient($$1, $$2, $$0.getServer());
         chaseClient.start();
         $$0.sendSuccess(
            () -> Component.literal(
               "You are now chasing "
                  + $$1
                  + ":"
                  + $$2
                  + ". If that server does '/chase lead' then you will automatically go to the same position. Use '/chase stop' to stop chasing."
            ),
            false
         );
         return 0;
      }
   }
}
