package net.minecraft.network.protocol;

public abstract class BundleDelimiterPacket<T extends net.minecraft.network.PacketListener> implements Packet<T> {
   @Override
   public final void handle(T $$0) {
      throw new AssertionError("This packet should be handled by pipeline");
   }

   @Override
   public abstract PacketType<? extends BundleDelimiterPacket<T>> type();
}
