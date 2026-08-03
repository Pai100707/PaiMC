package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import org.jspecify.annotations.Nullable;

public class ProtocolInfoBuilder<T extends net.minecraft.network.PacketListener, B extends ByteBuf, C> {
   final net.minecraft.network.ConnectionProtocol protocol;
   final PacketFlow flow;
   private final List<ProtocolInfoBuilder.CodecEntry<T, ?, B, C>> codecs = new ArrayList<>();
   @Nullable
   private BundlerInfo bundlerInfo;

   public ProtocolInfoBuilder(net.minecraft.network.ConnectionProtocol $$0, PacketFlow $$1) {
      this.protocol = $$0;
      this.flow = $$1;
   }

   public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B, C> addPacket(PacketType<P> $$0, StreamCodec<? super B, P> $$1) {
      this.codecs.add(new ProtocolInfoBuilder.CodecEntry<>($$0, $$1, null));
      return this;
   }

   public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B, C> addPacket(PacketType<P> $$0, StreamCodec<? super B, P> $$1, CodecModifier<B, P, C> $$2) {
      this.codecs.add(new ProtocolInfoBuilder.CodecEntry<>($$0, $$1, $$2));
      return this;
   }

   public <P extends BundlePacket<? super T>, D extends BundleDelimiterPacket<? super T>> ProtocolInfoBuilder<T, B, C> withBundlePacket(
      PacketType<P> $$0, Function<Iterable<Packet<? super T>>, P> $$1, D $$2
   ) {
      StreamCodec<ByteBuf, D> $$3 = StreamCodec.unit($$2);
      PacketType<D> $$4 = (PacketType<D>)$$2.type();
      this.codecs.add(new ProtocolInfoBuilder.CodecEntry<>($$4, $$3, null));
      this.bundlerInfo = BundlerInfo.createForPacket($$0, $$1, $$2);
      return this;
   }

   StreamCodec<ByteBuf, Packet<? super T>> buildPacketCodec(Function<ByteBuf, B> $$0, List<ProtocolInfoBuilder.CodecEntry<T, ?, B, C>> $$1, C $$2) {
      ProtocolCodecBuilder<ByteBuf, T> $$3 = new ProtocolCodecBuilder<>(this.flow);

      for (ProtocolInfoBuilder.CodecEntry<T, ?, B, C> $$4 : $$1) {
         $$4.addToBuilder($$3, $$0, $$2);
      }

      return $$3.build();
   }

   private static net.minecraft.network.ProtocolInfo.Details buildDetails(
      final net.minecraft.network.ConnectionProtocol $$0, final PacketFlow $$1, final List<? extends ProtocolInfoBuilder.CodecEntry<?, ?, ?, ?>> $$2
   ) {
      return new net.minecraft.network.ProtocolInfo.Details() {
         @Override
         public net.minecraft.network.ConnectionProtocol id() {
            return $$0;
         }

         @Override
         public PacketFlow flow() {
            return $$1;
         }

         @Override
         public void listPackets(net.minecraft.network.ProtocolInfo.Details.PacketVisitor $$0x) {
            for (int $$1x = 0; $$1x < $$2.size(); $$1x++) {
               ProtocolInfoBuilder.CodecEntry<?, ?, ?, ?> $$2x = (ProtocolInfoBuilder.CodecEntry<?, ?, ?, ?>)$$2.get($$1x);
               $$0.accept($$2x.type, $$1x);
            }
         }
      };
   }

   public SimpleUnboundProtocol<T, B> buildUnbound(final C $$0) {
      final List<ProtocolInfoBuilder.CodecEntry<T, ?, B, C>> $$1 = List.copyOf(this.codecs);
      final BundlerInfo $$2 = this.bundlerInfo;
      final net.minecraft.network.ProtocolInfo.Details $$3 = buildDetails(this.protocol, this.flow, $$1);
      return new SimpleUnboundProtocol<T, B>() {
         @Override
         public net.minecraft.network.ProtocolInfo<T> bind(Function<ByteBuf, B> $$0x) {
            return new ProtocolInfoBuilder.Implementation<>(
               ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec($$0, $$1, $$0), $$2
            );
         }

         @Override
         public net.minecraft.network.ProtocolInfo.Details details() {
            return $$3;
         }
      };
   }

   public UnboundProtocol<T, B, C> buildUnbound() {
      final List<ProtocolInfoBuilder.CodecEntry<T, ?, B, C>> $$0 = List.copyOf(this.codecs);
      final BundlerInfo $$1 = this.bundlerInfo;
      final net.minecraft.network.ProtocolInfo.Details $$2 = buildDetails(this.protocol, this.flow, $$0);
      return new UnboundProtocol<T, B, C>() {
         @Override
         public net.minecraft.network.ProtocolInfo<T> bind(Function<ByteBuf, B> $$0x, C $$1x) {
            return new ProtocolInfoBuilder.Implementation<>(
               ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec($$0, $$0, $$1), $$1
            );
         }

         @Override
         public net.minecraft.network.ProtocolInfo.Details details() {
            return $$2;
         }
      };
   }

   private static <L extends net.minecraft.network.PacketListener, B extends ByteBuf> SimpleUnboundProtocol<L, B> protocol(
      net.minecraft.network.ConnectionProtocol $$0, PacketFlow $$1, Consumer<ProtocolInfoBuilder<L, B, Unit>> $$2
   ) {
      ProtocolInfoBuilder<L, B, Unit> $$3 = new ProtocolInfoBuilder<>($$0, $$1);
      $$2.accept($$3);
      return $$3.buildUnbound(Unit.INSTANCE);
   }

   public static <T extends net.minecraft.network.ServerboundPacketListener, B extends ByteBuf> SimpleUnboundProtocol<T, B> serverboundProtocol(
      net.minecraft.network.ConnectionProtocol $$0, Consumer<ProtocolInfoBuilder<T, B, Unit>> $$1
   ) {
      return protocol($$0, PacketFlow.SERVERBOUND, $$1);
   }

   public static <T extends net.minecraft.network.ClientboundPacketListener, B extends ByteBuf> SimpleUnboundProtocol<T, B> clientboundProtocol(
      net.minecraft.network.ConnectionProtocol $$0, Consumer<ProtocolInfoBuilder<T, B, Unit>> $$1
   ) {
      return protocol($$0, PacketFlow.CLIENTBOUND, $$1);
   }

   private static <L extends net.minecraft.network.PacketListener, B extends ByteBuf, C> UnboundProtocol<L, B, C> contextProtocol(
      net.minecraft.network.ConnectionProtocol $$0, PacketFlow $$1, Consumer<ProtocolInfoBuilder<L, B, C>> $$2
   ) {
      ProtocolInfoBuilder<L, B, C> $$3 = new ProtocolInfoBuilder<>($$0, $$1);
      $$2.accept($$3);
      return $$3.buildUnbound();
   }

   public static <T extends net.minecraft.network.ServerboundPacketListener, B extends ByteBuf, C> UnboundProtocol<T, B, C> contextServerboundProtocol(
      net.minecraft.network.ConnectionProtocol $$0, Consumer<ProtocolInfoBuilder<T, B, C>> $$1
   ) {
      return contextProtocol($$0, PacketFlow.SERVERBOUND, $$1);
   }

   public static <T extends net.minecraft.network.ClientboundPacketListener, B extends ByteBuf, C> UnboundProtocol<T, B, C> contextClientboundProtocol(
      net.minecraft.network.ConnectionProtocol $$0, Consumer<ProtocolInfoBuilder<T, B, C>> $$1
   ) {
      return contextProtocol($$0, PacketFlow.CLIENTBOUND, $$1);
   }

   record CodecEntry<T extends net.minecraft.network.PacketListener, P extends Packet<? super T>, B extends ByteBuf, C>(
      PacketType<P> type, StreamCodec<? super B, P> serializer, @Nullable CodecModifier<B, P, C> modifier
   ) {

      public void addToBuilder(ProtocolCodecBuilder<ByteBuf, T> $$0, Function<ByteBuf, B> $$1, C $$2) {
         StreamCodec<? super B, P> $$3;
         if (this.modifier != null) {
            $$3 = this.modifier.apply(this.serializer, $$2);
         } else {
            $$3 = this.serializer;
         }

         StreamCodec<ByteBuf, P> $$5 = $$3.mapStream($$1);
         $$0.add(this.type, $$5);
      }
   }

   record Implementation<L extends net.minecraft.network.PacketListener>(
      net.minecraft.network.ConnectionProtocol id, PacketFlow flow, StreamCodec<ByteBuf, Packet<? super L>> codec, @Nullable BundlerInfo bundlerInfo
   ) implements net.minecraft.network.ProtocolInfo<L> {
   }
}
