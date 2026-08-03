package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;

public interface NumberFormatType<T extends NumberFormat> {
   MapCodec<T> mapCodec();

   StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T> streamCodec();
}
