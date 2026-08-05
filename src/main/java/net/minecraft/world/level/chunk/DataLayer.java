package net.minecraft.world.level.chunk;

import java.util.Arrays;
import net.minecraft.util.Util;
import net.minecraft.util.VisibleForDebug;

public class DataLayer {
   public static final int LAYER_COUNT = 16;
   public static final int LAYER_SIZE = 128;
   public static final int SIZE = 2048;
   private static final int NIBBLE_SIZE = 4;
   
   protected byte[] data;
   private int defaultValue;

   public DataLayer() {
      this(0);
   }

   public DataLayer(int $$0) {
      this.defaultValue = $$0;
   }

   public DataLayer(byte[] $$0) {
      this.data = $$0;
      this.defaultValue = 0;
      if ($$0.length != 2048) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("DataLayer should be 2048 bytes not: " + $$0.length));
      }
   }

   public int get(int $$0, int $$1, int $$2) {
      return this.get(getIndex($$0, $$1, $$2));
   }

   public void set(int $$0, int $$1, int $$2, int $$3) {
      this.set(getIndex($$0, $$1, $$2), $$3);
   }

   private static int getIndex(int $$0, int $$1, int $$2) {
      return $$1 << 8 | $$2 << 4 | $$0;
   }

   private int get(int $$0) {
      if (this.data == null) {
         return this.defaultValue;
      } else {
         int $$1 = getByteIndex($$0);
         int $$2 = getNibbleIndex($$0);
         return this.data[$$1] >> 4 * $$2 & 15;
      }
   }

   private void set(int $$0, int $$1) {
      byte[] $$2 = this.getData();
      int $$3 = getByteIndex($$0);
      int $$4 = getNibbleIndex($$0);
      int $$5 = ~(15 << 4 * $$4);
      int $$6 = ($$1 & 15) << 4 * $$4;
      $$2[$$3] = (byte)($$2[$$3] & $$5 | $$6);
   }

   private static int getNibbleIndex(int $$0) {
      return $$0 & 1;
   }

   private static int getByteIndex(int $$0) {
      return $$0 >> 1;
   }

   public void fill(int $$0) {
      this.defaultValue = $$0;
      this.data = null;
   }

   private static byte packFilled(int $$0) {
      byte $$1 = (byte)$$0;

      for (int $$2 = 4; $$2 < 8; $$2 += 4) {
         $$1 = (byte)($$1 | $$0 << $$2);
      }

      return $$1;
   }

   public byte[] getData() {
      if (this.data == null) {
         this.data = new byte[2048];
         if (this.defaultValue != 0) {
            Arrays.fill(this.data, packFilled(this.defaultValue));
         }
      }

      return this.data;
   }

   public DataLayer copy() {
      return this.data == null ? new DataLayer(this.defaultValue) : new DataLayer((byte[])this.data.clone());
   }

   @Override
   public String toString() {
      StringBuilder $$0 = new StringBuilder();

      for (int $$1 = 0; $$1 < 4096; $$1++) {
         $$0.append(Integer.toHexString(this.get($$1)));
         if (($$1 & 15) == 15) {
            $$0.append("\n");
         }

         if (($$1 & 0xFF) == 255) {
            $$0.append("\n");
         }
      }

      return $$0.toString();
   }

   @VisibleForDebug
   public String layerToString(int $$0) {
      StringBuilder $$1 = new StringBuilder();

      for (int $$2 = 0; $$2 < 256; $$2++) {
         $$1.append(Integer.toHexString(this.get($$2)));
         if (($$2 & 15) == 15) {
            $$1.append("\n");
         }
      }

      return $$1.toString();
   }

   public boolean isDefinitelyHomogenous() {
      return this.data == null;
   }

   public boolean isDefinitelyFilledWith(int $$0) {
      return this.data == null && this.defaultValue == $$0;
   }

   public boolean isEmpty() {
      return this.data == null && this.defaultValue == 0;
   }
}
