package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelData.RespawnData;
import net.minecraft.world.phys.Vec2;

public class SetWorldSpawnCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setworldspawn")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .executes(
                  $$0x -> setSpawn(
                     (CommandSourceStack)$$0x.getSource(),
                     BlockPos.containing(((CommandSourceStack)$$0x.getSource()).getPosition()),
                     WorldCoordinates.ZERO_ROTATION
                  )
               ))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("pos", BlockPosArgument.blockPos())
                     .executes(
                        $$0x -> setSpawn((CommandSourceStack)$$0x.getSource(), BlockPosArgument.getSpawnablePos($$0x, "pos"), WorldCoordinates.ZERO_ROTATION)
                     ))
                  .then(
                     Commands.argument("rotation", RotationArgument.rotation())
                        .executes(
                           $$0x -> setSpawn(
                              (CommandSourceStack)$$0x.getSource(),
                              BlockPosArgument.getSpawnablePos($$0x, "pos"),
                              RotationArgument.getRotation($$0x, "rotation")
                           )
                        )
                  )
            )
      );
   }

   private static int setSpawn(CommandSourceStack $$0, BlockPos $$1, Coordinates $$2) {
      ServerLevel $$3 = $$0.getLevel();
      Vec2 $$4 = $$2.getRotation($$0);
      float $$5 = $$4.y;
      float $$6 = $$4.x;
      RespawnData $$7 = RespawnData.of($$3.dimension(), $$1, $$5, $$6);
      $$3.setRespawnData($$7);
      $$0.sendSuccess(
         () -> Component.translatable(
            "commands.setworldspawn.success", new Object[]{$$1.getX(), $$1.getY(), $$1.getZ(), $$7.yaw(), $$7.pitch(), $$3.dimension().identifier().toString()}
         ),
         true
      );
      return 1;
   }
}
