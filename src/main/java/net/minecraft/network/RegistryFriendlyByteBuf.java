package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.core.RegistryAccess;

public class RegistryFriendlyByteBuf extends net.minecraft.network.FriendlyByteBuf {
   private final RegistryAccess registryAccess;

   public RegistryFriendlyByteBuf(ByteBuf $$0, RegistryAccess $$1) {
      super($$0);
      this.registryAccess = $$1;
   }

   public RegistryAccess registryAccess() {
      return this.registryAccess;
   }

   public static Function<ByteBuf, net.minecraft.network.RegistryFriendlyByteBuf> decorator(RegistryAccess $$0) {
      return $$1 -> new net.minecraft.network.RegistryFriendlyByteBuf($$1, $$0);
   }
}
