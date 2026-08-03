package net.minecraft.server.rcon;

import java.nio.charset.StandardCharsets;

public class PktUtils {
   public static final int MAX_PACKET_SIZE = 1460;
   public static final char[] HEX_CHAR = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

   public static String stringFromByteArray(byte[] $$0, int $$1, int $$2) {
      int $$3 = $$2 - 1;
      int $$4 = $$1 > $$3 ? $$3 : $$1;

      while (0 != $$0[$$4] && $$4 < $$3) {
         $$4++;
      }

      return new String($$0, $$1, $$4 - $$1, StandardCharsets.UTF_8);
   }

   public static int intFromByteArray(byte[] $$0, int $$1) {
      return intFromByteArray($$0, $$1, $$0.length);
   }

   public static int intFromByteArray(byte[] $$0, int $$1, int $$2) {
      return 0 > $$2 - $$1 - 4 ? 0 : $$0[$$1 + 3] << 24 | ($$0[$$1 + 2] & 0xFF) << 16 | ($$0[$$1 + 1] & 0xFF) << 8 | $$0[$$1] & 0xFF;
   }

   public static int intFromNetworkByteArray(byte[] $$0, int $$1, int $$2) {
      return 0 > $$2 - $$1 - 4 ? 0 : $$0[$$1] << 24 | ($$0[$$1 + 1] & 0xFF) << 16 | ($$0[$$1 + 2] & 0xFF) << 8 | $$0[$$1 + 3] & 0xFF;
   }

   public static String toHexString(byte $$0) {
      return "" + HEX_CHAR[($$0 & 240) >>> 4] + HEX_CHAR[$$0 & 15];
   }
}
