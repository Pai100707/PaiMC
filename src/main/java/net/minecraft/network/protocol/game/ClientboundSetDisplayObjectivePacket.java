package net.minecraft.network.protocol.game;

import java.util.Objects;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import org.jspecify.annotations.Nullable;

public class ClientboundSetDisplayObjectivePacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetDisplayObjectivePacket> STREAM_CODEC = Packet.codec(
      ClientboundSetDisplayObjectivePacket::write, ClientboundSetDisplayObjectivePacket::new
   );
   private final DisplaySlot slot;
   private final String objectiveName;

   public ClientboundSetDisplayObjectivePacket(DisplaySlot $$0, @Nullable Objective $$1) {
      this.slot = $$0;
      if ($$1 == null) {
         this.objectiveName = "";
      } else {
         this.objectiveName = $$1.getName();
      }
   }

   private ClientboundSetDisplayObjectivePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.slot = $$0.readById(DisplaySlot.BY_ID);
      this.objectiveName = $$0.readUtf();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeById(DisplaySlot::id, this.slot);
      $$0.writeUtf(this.objectiveName);
   }

   @Override
   public PacketType<ClientboundSetDisplayObjectivePacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_DISPLAY_OBJECTIVE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSetDisplayObjective(this);
   }

   public DisplaySlot getSlot() {
      return this.slot;
   }

   @Nullable
   public String getObjectiveName() {
      return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
   }
}
