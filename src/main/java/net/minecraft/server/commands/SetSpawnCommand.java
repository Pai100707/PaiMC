package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData.RespawnData;
import net.minecraft.world.phys.Vec2;

public class SetSpawnCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spawnpoint")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .executes(
                  $$0x -> setSpawn(
                     (CommandSourceStack)$$0x.getSource(),
                     Collections.singleton(((CommandSourceStack)$$0x.getSource()).getPlayerOrException()),
                     BlockPos.containing(((CommandSourceStack)$$0x.getSource()).getPosition()),
                     WorldCoordinates.ZERO_ROTATION
                  )
               ))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players())
                     .executes(
                        $$0x -> setSpawn(
                           (CommandSourceStack)$$0x.getSource(),
                           EntityArgument.getPlayers($$0x, "targets"),
                           BlockPos.containing(((CommandSourceStack)$$0x.getSource()).getPosition()),
                           WorldCoordinates.ZERO_ROTATION
                        )
                     ))
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("pos", BlockPosArgument.blockPos())
                           .executes(
                              $$0x -> setSpawn(
                                 (CommandSourceStack)$$0x.getSource(),
                                 EntityArgument.getPlayers($$0x, "targets"),
                                 BlockPosArgument.getSpawnablePos($$0x, "pos"),
                                 WorldCoordinates.ZERO_ROTATION
                              )
                           ))
                        .then(
                           Commands.argument("rotation", RotationArgument.rotation())
                              .executes(
                                 $$0x -> setSpawn(
                                    (CommandSourceStack)$$0x.getSource(),
                                    EntityArgument.getPlayers($$0x, "targets"),
                                    BlockPosArgument.getSpawnablePos($$0x, "pos"),
                                    RotationArgument.getRotation($$0x, "rotation")
                                 )
                              )
                        )
                  )
            )
      );
   }

   private static int setSpawn(CommandSourceStack $$0, Collection<ServerPlayer> $$1, BlockPos $$2, Coordinates $$3) {
      ResourceKey<Level> $$4 = $$0.getLevel().dimension();
      Vec2 $$5 = $$3.getRotation($$0);
      float $$6 = Mth.wrapDegrees($$5.y);
      float $$7 = Mth.clamp($$5.x, -90.0F, 90.0F);

      for (ServerPlayer $$8 : $$1) {
         $$8.setRespawnPosition(new ServerPlayer.RespawnConfig(RespawnData.of($$4, $$2, $$6, $$7), true), false);
      }

      String $$9 = $$4.identifier().toString();
      if ($$1.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.spawnpoint.success.single", new Object[]{$$2.getX(), $$2.getY(), $$2.getZ(), $$6, $$7, $$9, $$1.iterator().next().getDisplayName()}
            ),
            true
         );
      } else {
         $$0.sendSuccess(
            () -> Component.translatable("commands.spawnpoint.success.multiple", new Object[]{$$2.getX(), $$2.getY(), $$2.getZ(), $$6, $$7, $$9, $$1.size()}),
            true
         );
      }

      return $$1.size();
   }
}
