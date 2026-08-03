package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetTitlesAnimationPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetTitlesAnimationPacket> STREAM_CODEC = Packet.codec(
      ClientboundSetTitlesAnimationPacket::write, ClientboundSetTitlesAnimationPacket::new
   );
   private final int fadeIn;
   private final int stay;
   private final int fadeOut;

   public ClientboundSetTitlesAnimationPacket(int $$0, int $$1, int $$2) {
      this.fadeIn = $$0;
      this.stay = $$1;
      this.fadeOut = $$2;
   }

   private ClientboundSetTitlesAnimationPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.fadeIn = $$0.readInt();
      this.stay = $$0.readInt();
      this.fadeOut = $$0.readInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeInt(this.fadeIn);
      $$0.writeInt(this.stay);
      $$0.writeInt(this.fadeOut);
   }

   @Override
   public PacketType<ClientboundSetTitlesAnimationPacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_TITLES_ANIMATION;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.setTitlesAnimation(this);
   }

   public int getFadeIn() {
      return this.fadeIn;
   }

   public int getStay() {
      return this.stay;
   }

   public int getFadeOut() {
      return this.fadeOut;
   }
}
