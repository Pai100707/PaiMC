package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;

public class ClientboundUpdateAdvancementsPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundUpdateAdvancementsPacket> STREAM_CODEC = Packet.codec(
      ClientboundUpdateAdvancementsPacket::write, ClientboundUpdateAdvancementsPacket::new
   );
   private final boolean reset;
   private final List<AdvancementHolder> added;
   private final Set<Identifier> removed;
   private final Map<Identifier, AdvancementProgress> progress;
   private final boolean showAdvancements;

   public ClientboundUpdateAdvancementsPacket(
      boolean $$0, Collection<AdvancementHolder> $$1, Set<Identifier> $$2, Map<Identifier, AdvancementProgress> $$3, boolean $$4
   ) {
      this.reset = $$0;
      this.added = List.copyOf($$1);
      this.removed = Set.copyOf($$2);
      this.progress = Map.copyOf($$3);
      this.showAdvancements = $$4;
   }

   private ClientboundUpdateAdvancementsPacket(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this.reset = $$0.readBoolean();
      this.added = (List<AdvancementHolder>)AdvancementHolder.LIST_STREAM_CODEC.decode($$0);
      this.removed = $$0.readCollection(Sets::newLinkedHashSetWithExpectedSize, net.minecraft.network.FriendlyByteBuf::readIdentifier);
      this.progress = $$0.readMap(net.minecraft.network.FriendlyByteBuf::readIdentifier, AdvancementProgress::fromNetwork);
      this.showAdvancements = $$0.readBoolean();
   }

   private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      $$0.writeBoolean(this.reset);
      AdvancementHolder.LIST_STREAM_CODEC.encode($$0, this.added);
      $$0.writeCollection(this.removed, net.minecraft.network.FriendlyByteBuf::writeIdentifier);
      $$0.writeMap(this.progress, net.minecraft.network.FriendlyByteBuf::writeIdentifier, ($$0x, $$1) -> $$1.serializeToNetwork($$0x));
      $$0.writeBoolean(this.showAdvancements);
   }

   @Override
   public PacketType<ClientboundUpdateAdvancementsPacket> type() {
      return GamePacketTypes.CLIENTBOUND_UPDATE_ADVANCEMENTS;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleUpdateAdvancementsPacket(this);
   }

   public List<AdvancementHolder> getAdded() {
      return this.added;
   }

   public Set<Identifier> getRemoved() {
      return this.removed;
   }

   public Map<Identifier, AdvancementProgress> getProgress() {
      return this.progress;
   }

   public boolean shouldReset() {
      return this.reset;
   }

   public boolean shouldShowAdvancements() {
      return this.showAdvancements;
   }
}
