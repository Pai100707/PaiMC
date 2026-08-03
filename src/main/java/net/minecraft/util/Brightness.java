package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Brightness(int block, int sky) {
   public static final Codec<Integer> LIGHT_VALUE_CODEC = net.minecraft.util.ExtraCodecs.intRange(0, 15);
   public static final Codec<net.minecraft.util.Brightness> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            LIGHT_VALUE_CODEC.fieldOf("block").forGetter(net.minecraft.util.Brightness::block),
            LIGHT_VALUE_CODEC.fieldOf("sky").forGetter(net.minecraft.util.Brightness::sky)
         )
         .apply($$0, net.minecraft.util.Brightness::new)
   );
   public static final net.minecraft.util.Brightness FULL_BRIGHT = new net.minecraft.util.Brightness(15, 15);

   public static int pack(int $$0, int $$1) {
      return $$0 << 4 | $$1 << 20;
   }

   public int pack() {
      return pack(this.block, this.sky);
   }

   public static int block(int $$0) {
      return $$0 >> 4 & 65535;
   }

   public static int sky(int $$0) {
      return $$0 >> 20 & 65535;
   }

   public static net.minecraft.util.Brightness unpack(int $$0) {
      return new net.minecraft.util.Brightness(block($$0), sky($$0));
   }
}
