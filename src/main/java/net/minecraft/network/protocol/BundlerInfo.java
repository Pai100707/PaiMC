package net.minecraft.network.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface BundlerInfo {
   int BUNDLE_SIZE_LIMIT = 4096;

   static <T extends net.minecraft.network.PacketListener, P extends BundlePacket<? super T>> BundlerInfo createForPacket(
      final PacketType<P> $$0, final Function<Iterable<Packet<? super T>>, P> $$1, final BundleDelimiterPacket<? super T> $$2
   ) {
      return new BundlerInfo() {
         @Override
         public void unbundlePacket(Packet<?> $$0x, Consumer<Packet<?>> $$1x) {
            if ($$0.type() == $$0) {
               P $$2x = (P)$$0;
               $$1.accept($$2);
               $$2x.subPackets().forEach($$1);
               $$1.accept($$2);
            } else {
               $$1.accept($$0);
            }
         }

         
         @Override
         public BundlerInfo.Bundler startPacketBundling(Packet<?> $$0x) {
            return $$0 == $$2 ? new BundlerInfo.Bundler() {
               private final List<Packet<? super T>> bundlePackets = new ArrayList<>();

               
               @Override
               public Packet<?> addPacket(Packet<?> $$0x) {
                  if ($$0 == $$2) {
                     return $$1.apply(this.bundlePackets);
                  } else if (this.bundlePackets.size() >= 4096) {
                     throw new IllegalStateException("Too many packets in a bundle");
                  } else {
                     this.bundlePackets.add((Packet<? super T>)$$0);
                     return null;
                  }
               }
            } : null;
         }
      };
   }

   void unbundlePacket(Packet<?> var1, Consumer<Packet<?>> var2);

   
   BundlerInfo.Bundler startPacketBundling(Packet<?> var1);

   public interface Bundler {
      
      Packet<?> addPacket(Packet<?> var1);
   }
}
