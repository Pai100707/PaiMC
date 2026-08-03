package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.stream.LongStream;
import net.minecraft.util.Util;

public class Xoroshiro128PlusPlus {
   private long seedLo;
   private long seedHi;
   public static final Codec<Xoroshiro128PlusPlus> CODEC = Codec.LONG_STREAM
      .comapFlatMap($$0 -> Util.fixedSize($$0, 2).map($$0x -> new Xoroshiro128PlusPlus($$0x[0], $$0x[1])), $$0 -> LongStream.of($$0.seedLo, $$0.seedHi));

   public Xoroshiro128PlusPlus(RandomSupport.Seed128bit $$0) {
      this($$0.seedLo(), $$0.seedHi());
   }

   public Xoroshiro128PlusPlus(long $$0, long $$1) {
      this.seedLo = $$0;
      this.seedHi = $$1;
      if ((this.seedLo | this.seedHi) == 0L) {
         this.seedLo = -7046029254386353131L;
         this.seedHi = 7640891576956012809L;
      }
   }

   public long nextLong() {
      long $$0 = this.seedLo;
      long $$1 = this.seedHi;
      long $$2 = Long.rotateLeft($$0 + $$1, 17) + $$0;
      $$1 ^= $$0;
      this.seedLo = Long.rotateLeft($$0, 49) ^ $$1 ^ $$1 << 21;
      this.seedHi = Long.rotateLeft($$1, 28);
      return $$2;
   }
}
