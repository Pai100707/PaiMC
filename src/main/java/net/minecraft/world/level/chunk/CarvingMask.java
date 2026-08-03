package net.minecraft.world.level.chunk;

import java.util.BitSet;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class CarvingMask {
   private final int minY;
   private final BitSet mask;
   private CarvingMask.Mask additionalMask = ($$0x, $$1x, $$2) -> false;

   public CarvingMask(int $$0, int $$1) {
      this.minY = $$1;
      this.mask = new BitSet(256 * $$0);
   }

   public void setAdditionalMask(CarvingMask.Mask $$0) {
      this.additionalMask = $$0;
   }

   public CarvingMask(long[] $$0, int $$1) {
      this.minY = $$1;
      this.mask = BitSet.valueOf($$0);
   }

   private int getIndex(int $$0, int $$1, int $$2) {
      return $$0 & 15 | ($$2 & 15) << 4 | $$1 - this.minY << 8;
   }

   public void set(int $$0, int $$1, int $$2) {
      this.mask.set(this.getIndex($$0, $$1, $$2));
   }

   public boolean get(int $$0, int $$1, int $$2) {
      return this.additionalMask.test($$0, $$1, $$2) || this.mask.get(this.getIndex($$0, $$1, $$2));
   }

   public Stream<BlockPos> stream(net.minecraft.world.level.ChunkPos $$0) {
      return this.mask.stream().mapToObj($$1 -> {
         int $$2 = $$1 & 15;
         int $$3 = $$1 >> 4 & 15;
         int $$4 = $$1 >> 8;
         return $$0.getBlockAt($$2, $$4 + this.minY, $$3);
      });
   }

   public long[] toArray() {
      return this.mask.toLongArray();
   }

   public interface Mask {
      boolean test(int var1, int var2, int var3);
   }
}
