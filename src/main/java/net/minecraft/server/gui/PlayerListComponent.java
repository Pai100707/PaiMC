package net.minecraft.server.gui;

import java.util.Vector;
import javax.swing.JList;

public class PlayerListComponent extends JList<String> {
   private final net.minecraft.server.MinecraftServer server;
   private int tickCount;

   public PlayerListComponent(net.minecraft.server.MinecraftServer $$0) {
      this.server = $$0;
      $$0.addTickable(this::tick);
   }

   public void tick() {
      if (this.tickCount++ % 20 == 0) {
         Vector<String> $$0 = new Vector<>();

         for (int $$1 = 0; $$1 < this.server.getPlayerList().getPlayers().size(); $$1++) {
            $$0.add(this.server.getPlayerList().getPlayers().get($$1).getGameProfile().name());
         }

         this.setListData($$0);
      }
   }
}
