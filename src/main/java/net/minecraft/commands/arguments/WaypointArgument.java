package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.waypoints.WaypointTransmitter;

public class WaypointArgument {
   public static final SimpleCommandExceptionType ERROR_NOT_A_WAYPOINT = new SimpleCommandExceptionType(Component.translatable("argument.waypoint.invalid"));

   public static WaypointTransmitter getWaypoint(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      if (((EntitySelector)$$0.getArgument($$1, EntitySelector.class)).findSingleEntity((net.minecraft.commands.CommandSourceStack)$$0.getSource()) instanceof WaypointTransmitter $$3
         )
       {
         return $$3;
      } else {
         throw ERROR_NOT_A_WAYPOINT.create();
      }
   }
}
