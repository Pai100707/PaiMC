package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;

public class NbtAccounter {
   public static final int DEFAULT_NBT_QUOTA = 2097152;
   public static final int UNCOMPRESSED_NBT_QUOTA = 104857600;
   private static final int MAX_STACK_DEPTH = 512;
   private final long quota;
   private long usage;
   private final int maxDepth;
   private int depth;

   public NbtAccounter(long $$0, int $$1) {
      this.quota = $$0;
      this.maxDepth = $$1;
   }

   public static net.minecraft.nbt.NbtAccounter create(long $$0) {
      return new net.minecraft.nbt.NbtAccounter($$0, 512);
   }

   public static net.minecraft.nbt.NbtAccounter defaultQuota() {
      return new net.minecraft.nbt.NbtAccounter(2097152L, 512);
   }

   public static net.minecraft.nbt.NbtAccounter uncompressedQuota() {
      return new net.minecraft.nbt.NbtAccounter(104857600L, 512);
   }

   public static net.minecraft.nbt.NbtAccounter unlimitedHeap() {
      return new net.minecraft.nbt.NbtAccounter(Long.MAX_VALUE, 512);
   }

   public void accountBytes(long $$0, long $$1) {
      this.accountBytes($$0 * $$1);
   }

   public void accountBytes(long $$0) {
      if ($$0 < 0L) {
         throw new IllegalArgumentException("Tried to account NBT tag with negative size: " + $$0);
      } else if (this.usage + $$0 > this.quota) {
         throw new net.minecraft.nbt.NbtAccounterException(
            "Tried to read NBT tag that was too big; tried to allocate: " + this.usage + " + " + $$0 + " bytes where max allowed: " + this.quota
         );
      } else {
         this.usage += $$0;
      }
   }

   public void pushDepth() {
      if (this.depth >= this.maxDepth) {
         throw new net.minecraft.nbt.NbtAccounterException("Tried to read NBT tag with too high complexity, depth > " + this.maxDepth);
      } else {
         this.depth++;
      }
   }

   public void popDepth() {
      if (this.depth <= 0) {
         throw new net.minecraft.nbt.NbtAccounterException("NBT-Accounter tried to pop stack-depth at top-level");
      } else {
         this.depth--;
      }
   }

   @VisibleForTesting
   public long getUsage() {
      return this.usage;
   }

   @VisibleForTesting
   public int getDepth() {
      return this.depth;
   }
}
