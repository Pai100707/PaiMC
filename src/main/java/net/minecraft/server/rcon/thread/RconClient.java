package net.minecraft.server.rcon.thread;

import com.mojang.logging.LogUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import net.minecraft.server.rcon.PktUtils;
import org.slf4j.Logger;

public class RconClient extends GenericThread {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SERVERDATA_AUTH = 3;
   private static final int SERVERDATA_EXECCOMMAND = 2;
   private static final int SERVERDATA_RESPONSE_VALUE = 0;
   private static final int SERVERDATA_AUTH_RESPONSE = 2;
   private static final int SERVERDATA_AUTH_FAILURE = -1;
   private boolean authed;
   private final Socket client;
   private final byte[] buf = new byte[1460];
   private final String rconPassword;
   private final net.minecraft.server.ServerInterface serverInterface;

   RconClient(net.minecraft.server.ServerInterface $$0, String $$1, Socket $$2) {
      super("RCON Client " + $$2.getInetAddress());
      this.serverInterface = $$0;
      this.client = $$2;

      try {
         this.client.setSoTimeout(0);
      } catch (Exception var5) {
         this.running = false;
      }

      this.rconPassword = $$1;
   }

   @Override
   public void run() {
      try {
         try {
            while (this.running) {
               BufferedInputStream $$0 = new BufferedInputStream(this.client.getInputStream());
               int $$1 = $$0.read(this.buf, 0, 1460);
               if (10 > $$1) {
                  return;
               }

               int $$2 = 0;
               int $$3 = PktUtils.intFromByteArray(this.buf, 0, $$1);
               if ($$3 != $$1 - 4) {
                  return;
               }

               $$2 += 4;
               int $$4 = PktUtils.intFromByteArray(this.buf, $$2, $$1);
               $$2 += 4;
               int $$5 = PktUtils.intFromByteArray(this.buf, $$2);
               $$2 += 4;
               switch ($$5) {
                  case 2:
                     if (this.authed) {
                        String $$7 = PktUtils.stringFromByteArray(this.buf, $$2, $$1);

                        try {
                           this.sendCmdResponse($$4, this.serverInterface.runCommand($$7));
                        } catch (Exception var15) {
                           this.sendCmdResponse($$4, "Error executing: " + $$7 + " (" + var15.getMessage() + ")");
                        }
                        break;
                     }

                     this.sendAuthFailure();
                     break;
                  case 3:
                     String $$6 = PktUtils.stringFromByteArray(this.buf, $$2, $$1);
                     $$2 += $$6.length();
                     if (!$$6.isEmpty() && $$6.equals(this.rconPassword)) {
                        this.authed = true;
                        this.send($$4, 2, "");
                        break;
                     }

                     this.authed = false;
                     this.sendAuthFailure();
                     break;
                  default:
                     this.sendCmdResponse($$4, String.format(Locale.ROOT, "Unknown request %s", Integer.toHexString($$5)));
               }
            }

            return;
         } catch (IOException var16) {
         } catch (Exception var17) {
            LOGGER.error("Exception whilst parsing RCON input", var17);
         }
      } finally {
         this.closeSocket();
         LOGGER.info("Thread {} shutting down", this.name);
         this.running = false;
      }
   }

   private void send(int $$0, int $$1, String $$2) throws IOException {
      ByteArrayOutputStream $$3 = new ByteArrayOutputStream(1248);
      DataOutputStream $$4 = new DataOutputStream($$3);
      byte[] $$5 = $$2.getBytes(StandardCharsets.UTF_8);
      $$4.writeInt(Integer.reverseBytes($$5.length + 10));
      $$4.writeInt(Integer.reverseBytes($$0));
      $$4.writeInt(Integer.reverseBytes($$1));
      $$4.write($$5);
      $$4.write(0);
      $$4.write(0);
      this.client.getOutputStream().write($$3.toByteArray());
   }

   private void sendAuthFailure() throws IOException {
      this.send(-1, 2, "");
   }

   private void sendCmdResponse(int $$0, String $$1) throws IOException {
      int $$2 = $$1.length();

      do {
         int $$3 = 4096 <= $$2 ? 4096 : $$2;
         this.send($$0, 0, $$1.substring(0, $$3));
         $$1 = $$1.substring($$3);
         $$2 = $$1.length();
      } while (0 != $$2);
   }

   @Override
   public void stop() {
      this.running = false;
      this.closeSocket();
      super.stop();
   }

   private void closeSocket() {
      try {
         this.client.close();
      } catch (IOException var2) {
         LOGGER.warn("Failed to close socket", var2);
      }
   }
}
