package net.minecraft.server.rcon;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class RconConsoleSource implements CommandSource {
   private static final String RCON = "Rcon";
   private static final Component RCON_COMPONENT = Component.literal("Rcon");
   private final StringBuffer buffer = new StringBuffer();
   private final net.minecraft.server.MinecraftServer server;

   public RconConsoleSource(net.minecraft.server.MinecraftServer $$0) {
      this.server = $$0;
   }

   public void prepareForCommand() {
      this.buffer.setLength(0);
   }

   public String getCommandResponse() {
      return this.buffer.toString();
   }

   public CommandSourceStack createCommandSourceStack() {
      ServerLevel $$0 = this.server.overworld();
      return new CommandSourceStack(
         this, Vec3.atLowerCornerOf($$0.getRespawnData().pos()), Vec2.ZERO, $$0, LevelBasedPermissionSet.OWNER, "Rcon", RCON_COMPONENT, this.server, null
      );
   }

   public void sendSystemMessage(Component $$0) {
      this.buffer.append($$0.getString());
   }

   public boolean acceptsSuccess() {
      return true;
   }

   public boolean acceptsFailure() {
      return true;
   }

   public boolean shouldInformAdmins() {
      return this.server.shouldRconBroadcast();
   }
}
