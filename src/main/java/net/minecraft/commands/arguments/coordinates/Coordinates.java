package net.minecraft.commands.arguments.coordinates;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface Coordinates {
   Vec3 getPosition(net.minecraft.commands.CommandSourceStack var1);

   Vec2 getRotation(net.minecraft.commands.CommandSourceStack var1);

   default BlockPos getBlockPos(net.minecraft.commands.CommandSourceStack $$0) {
      return BlockPos.containing(this.getPosition($$0));
   }

   boolean isXRelative();

   boolean isYRelative();

   boolean isZRelative();
}
