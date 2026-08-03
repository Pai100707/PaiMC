package net.minecraft.server.chase;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import net.minecraft.server.commands.ChaseCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChaseServer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String serverBindAddress;
   private final int serverPort;
   private final PlayerList playerList;
   private final int broadcastIntervalMs;
   private volatile boolean wantsToRun;
   @Nullable
   private ServerSocket serverSocket;
   private final CopyOnWriteArrayList<Socket> clientSockets = new CopyOnWriteArrayList<>();

   public ChaseServer(String $$0, int $$1, PlayerList $$2, int $$3) {
      this.serverBindAddress = $$0;
      this.serverPort = $$1;
      this.playerList = $$2;
      this.broadcastIntervalMs = $$3;
   }

   public void start() throws IOException {
      if (this.serverSocket != null && !this.serverSocket.isClosed()) {
         LOGGER.warn("Remote control server was asked to start, but it is already running. Will ignore.");
      } else {
         this.wantsToRun = true;
         this.serverSocket = new ServerSocket(this.serverPort, 50, InetAddress.getByName(this.serverBindAddress));
         Thread $$0 = new Thread(this::runAcceptor, "chase-server-acceptor");
         $$0.setDaemon(true);
         $$0.start();
         Thread $$1 = new Thread(this::runSender, "chase-server-sender");
         $$1.setDaemon(true);
         $$1.start();
      }
   }

   private void runSender() {
      ChaseServer.PlayerPosition $$0 = null;

      while (this.wantsToRun) {
         if (!this.clientSockets.isEmpty()) {
            ChaseServer.PlayerPosition $$1 = this.getPlayerPosition();
            if ($$1 != null && !$$1.equals($$0)) {
               $$0 = $$1;
               byte[] $$2 = $$1.format().getBytes(StandardCharsets.US_ASCII);

               for (Socket $$3 : this.clientSockets) {
                  if (!$$3.isClosed()) {
                     Util.ioPool().execute(() -> {
                        try {
                           OutputStream $$2x = $$3.getOutputStream();
                           $$2x.write($$2);
                           $$2x.flush();
                        } catch (IOException var3x) {
                           LOGGER.info("Remote control client socket got an IO exception and will be closed", var3x);
                           IOUtils.closeQuietly($$3);
                        }
                     });
                  }
               }
            }

            List<Socket> $$4 = this.clientSockets.stream().filter(Socket::isClosed).collect(Collectors.toList());
            this.clientSockets.removeAll($$4);
         }

         if (this.wantsToRun) {
            try {
               Thread.sleep(this.broadcastIntervalMs);
            } catch (InterruptedException var6) {
            }
         }
      }
   }

   public void stop() {
      this.wantsToRun = false;
      IOUtils.closeQuietly(this.serverSocket);
      this.serverSocket = null;
   }

   private void runAcceptor() {
      try {
         while (this.wantsToRun) {
            if (this.serverSocket != null) {
               LOGGER.info("Remote control server is listening for connections on port {}", this.serverPort);
               Socket $$0 = this.serverSocket.accept();
               LOGGER.info("Remote control server received client connection on port {}", $$0.getPort());
               this.clientSockets.add($$0);
            }
         }
      } catch (ClosedByInterruptException var6) {
         if (this.wantsToRun) {
            LOGGER.info("Remote control server closed by interrupt");
         }
      } catch (IOException var7) {
         if (this.wantsToRun) {
            LOGGER.error("Remote control server closed because of an IO exception", var7);
         }
      } finally {
         IOUtils.closeQuietly(this.serverSocket);
      }

      LOGGER.info("Remote control server is now stopped");
      this.wantsToRun = false;
   }

   @Nullable
   private ChaseServer.PlayerPosition getPlayerPosition() {
      List<ServerPlayer> $$0 = this.playerList.getPlayers();
      if ($$0.isEmpty()) {
         return null;
      } else {
         ServerPlayer $$1 = $$0.get(0);
         String $$2 = (String)ChaseCommand.DIMENSION_NAMES.inverse().get($$1.level().dimension());
         return $$2 == null ? null : new ChaseServer.PlayerPosition($$2, $$1.getX(), $$1.getY(), $$1.getZ(), $$1.getYRot(), $$1.getXRot());
      }
   }

   record PlayerPosition(String dimensionName, double x, double y, double z, float yRot, float xRot) {
      String format() {
         return String.format(Locale.ROOT, "t %s %.2f %.2f %.2f %.2f %.2f\n", this.dimensionName, this.x, this.y, this.z, this.yRot, this.xRot);
      }
   }
}
