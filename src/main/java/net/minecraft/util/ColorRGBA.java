package net.minecraft.util;

import com.mojang.serialization.Codec;
import java.util.HexFormat;

public record ColorRGBA(int rgba) {
   public static final Codec<net.minecraft.util.ColorRGBA> CODEC = net.minecraft.util.ExtraCodecs.STRING_ARGB_COLOR
      .xmap(net.minecraft.util.ColorRGBA::new, net.minecraft.util.ColorRGBA::rgba);

   @Override
   public String toString() {
      return HexFormat.of().toHexDigits(this.rgba, 8);
   }
}
