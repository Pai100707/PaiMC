package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientboundMerchantOffersPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundMerchantOffersPacket> STREAM_CODEC = Packet.codec(
      ClientboundMerchantOffersPacket::write, ClientboundMerchantOffersPacket::new
   );
   private final int containerId;
   private final MerchantOffers offers;
   private final int villagerLevel;
   private final int villagerXp;
   private final boolean showProgress;
   private final boolean canRestock;

   public ClientboundMerchantOffersPacket(int $$0, MerchantOffers $$1, int $$2, int $$3, boolean $$4, boolean $$5) {
      this.containerId = $$0;
      this.offers = $$1.copy();
      this.villagerLevel = $$2;
      this.villagerXp = $$3;
      this.showProgress = $$4;
      this.canRestock = $$5;
   }

   private ClientboundMerchantOffersPacket(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this.containerId = $$0.readContainerId();
      this.offers = (MerchantOffers)MerchantOffers.STREAM_CODEC.decode($$0);
      this.villagerLevel = $$0.readVarInt();
      this.villagerXp = $$0.readVarInt();
      this.showProgress = $$0.readBoolean();
      this.canRestock = $$0.readBoolean();
   }

   private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      $$0.writeContainerId(this.containerId);
      MerchantOffers.STREAM_CODEC.encode($$0, this.offers);
      $$0.writeVarInt(this.villagerLevel);
      $$0.writeVarInt(this.villagerXp);
      $$0.writeBoolean(this.showProgress);
      $$0.writeBoolean(this.canRestock);
   }

   @Override
   public PacketType<ClientboundMerchantOffersPacket> type() {
      return GamePacketTypes.CLIENTBOUND_MERCHANT_OFFERS;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleMerchantOffers(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public MerchantOffers getOffers() {
      return this.offers;
   }

   public int getVillagerLevel() {
      return this.villagerLevel;
   }

   public int getVillagerXp() {
      return this.villagerXp;
   }

   public boolean showProgress() {
      return this.showProgress;
   }

   public boolean canRestock() {
      return this.canRestock;
   }
}
