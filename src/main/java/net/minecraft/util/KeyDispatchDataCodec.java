package net.minecraft.util;

import com.mojang.serialization.MapCodec;

public record KeyDispatchDataCodec<A>(MapCodec<A> codec) {
   public static <A> net.minecraft.util.KeyDispatchDataCodec<A> of(MapCodec<A> $$0) {
      return new net.minecraft.util.KeyDispatchDataCodec<>($$0);
   }
}
