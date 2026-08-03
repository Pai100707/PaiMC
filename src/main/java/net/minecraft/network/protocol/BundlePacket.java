package net.minecraft.network.protocol;

public abstract class BundlePacket<T extends net.minecraft.network.PacketListener> implements Packet<T> {
   private final Iterable<Packet<? super T>> packets;

   protected BundlePacket(Iterable<Packet<? super T>> $$0) {
      this.packets = $$0;
   }

   public final Iterable<Packet<? super T>> subPackets() {
      return this.packets;
   }

   @Override
   public abstract PacketType<? extends BundlePacket<T>> type();
}
