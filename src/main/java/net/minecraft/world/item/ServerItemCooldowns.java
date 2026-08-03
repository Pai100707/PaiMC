package net.minecraft.world.item;

import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class ServerItemCooldowns extends net.minecraft.world.item.ItemCooldowns {
   private final ServerPlayer player;

   public ServerItemCooldowns(ServerPlayer $$0) {
      this.player = $$0;
   }

   @Override
   protected void onCooldownStarted(Identifier $$0, int $$1) {
      super.onCooldownStarted($$0, $$1);
      this.player.connection.send(new ClientboundCooldownPacket($$0, $$1));
   }

   @Override
   protected void onCooldownEnded(Identifier $$0) {
      super.onCooldownEnded($$0);
      this.player.connection.send(new ClientboundCooldownPacket($$0, 0));
   }
}
