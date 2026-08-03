package net.minecraft.server.chase;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.ChaseCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChaseClient {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int RECONNECT_INTERVAL_SECONDS = 5;
   private final String serverHost;
   private final int serverPort;
   private final net.minecraft.server.MinecraftServer server;
   private volatile boolean wantsToRun;
   @Nullable
   private Socket socket;
   @Nullable
   private Thread thread;

   public ChaseClient(String $$0, int $$1, net.minecraft.server.MinecraftServer $$2) {
      this.serverHost = $$0;
      this.serverPort = $$1;
      this.server = $$2;
   }

   public void start() {
      if (this.thread != null && this.thread.isAlive()) {
         LOGGER.warn("Remote control client was asked to start, but it is already running. Will ignore.");
      }

      this.wantsToRun = true;
      this.thread = new Thread(this::run, "chase-client");
      this.thread.setDaemon(true);
      this.thread.start();
   }

   public void stop() {
      this.wantsToRun = false;
      IOUtils.closeQuietly(this.socket);
      this.socket = null;
      this.thread = null;
   }

   public void run() {
      String $$0 = this.serverHost + ":" + this.serverPort;

      while (this.wantsToRun) {
         try {
            LOGGER.info("Connecting to remote control server {}", $$0);
            this.socket = new Socket(this.serverHost, this.serverPort);
            LOGGER.info("Connected to remote control server! Will continuously execute the command broadcasted by that server.");

            try (BufferedReader $$1 = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.US_ASCII))) {
               while (this.wantsToRun) {
                  String $$2 = $$1.readLine();
                  if ($$2 == null) {
                     LOGGER.warn("Lost connection to remote control server {}. Will retry in {}s.", $$0, 5);
                     break;
                  }

                  this.handleMessage($$2);
               }
            } catch (IOException var8) {
               LOGGER.warn("Lost connection to remote control server {}. Will retry in {}s.", $$0, 5);
            }
         } catch (IOException var9) {
            LOGGER.warn("Failed to connect to remote control server {}. Will retry in {}s.", $$0, 5);
         }

         if (this.wantsToRun) {
            try {
               Thread.sleep(5000L);
            } catch (InterruptedException var5) {
            }
         }
      }
   }

   private void handleMessage(String $$0) {
      try (Scanner $$1 = new Scanner(new StringReader($$0))) {
         $$1.useLocale(Locale.ROOT);
         String $$2 = $$1.next();
         if ("t".equals($$2)) {
            this.handleTeleport($$1);
         } else {
            LOGGER.warn("Unknown message type '{}'", $$2);
         }
      } catch (NoSuchElementException var7) {
         LOGGER.warn("Could not parse message '{}', ignoring", $$0);
      }
   }

   private void handleTeleport(Scanner $$0) {
      this.parseTarget($$0)
         .ifPresent(
            $$0x -> this.executeCommand(
               String.format(
                  Locale.ROOT,
                  "execute in %s run tp @s %.3f %.3f %.3f %.3f %.3f",
                  $$0x.level.identifier(),
                  $$0x.pos.x,
                  $$0x.pos.y,
                  $$0x.pos.z,
                  $$0x.rot.y,
                  $$0x.rot.x
               )
            )
         );
   }

   private Optional<ChaseClient.TeleportTarget> parseTarget(Scanner $$0) {
      ResourceKey<Level> $$1 = (ResourceKey<Level>)ChaseCommand.DIMENSION_NAMES.get($$0.next());
      if ($$1 == null) {
         return Optional.empty();
      } else {
         float $$2 = $$0.nextFloat();
         float $$3 = $$0.nextFloat();
         float $$4 = $$0.nextFloat();
         float $$5 = $$0.nextFloat();
         float $$6 = $$0.nextFloat();
         return Optional.of(new ChaseClient.TeleportTarget($$1, new Vec3($$2, $$3, $$4), new Vec2($$6, $$5)));
      }
   }

   private void executeCommand(String $$0) {
      this.server
         .execute(
            () -> {
               List<ServerPlayer> $$1 = this.server.getPlayerList().getPlayers();
               if (!$$1.isEmpty()) {
                  ServerPlayer $$2 = $$1.get(0);
                  ServerLevel $$3 = this.server.overworld();
                  CommandSourceStack $$4 = new CommandSourceStack(
                     $$2.commandSource(),
                     Vec3.atLowerCornerOf($$3.getRespawnData().pos()),
                     Vec2.ZERO,
                     $$3,
                     LevelBasedPermissionSet.OWNER,
                     "",
                     CommonComponents.EMPTY,
                     this.server,
                     $$2
                  );
                  Commands $$5 = this.server.getCommands();
                  $$5.performPrefixedCommand($$4, $$0);
               }
            }
         );
   }

   record TeleportTarget(ResourceKey<Level> level, Vec3 pos, Vec2 rot) {
   }
}
