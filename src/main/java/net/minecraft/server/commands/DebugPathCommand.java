package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;

public class DebugPathCommand {
   private static final SimpleCommandExceptionType ERROR_NOT_MOB = new SimpleCommandExceptionType(Component.literal("Source is not a mob"));
   private static final SimpleCommandExceptionType ERROR_NO_PATH = new SimpleCommandExceptionType(Component.literal("Path not found"));
   private static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.literal("Target not reached"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("debugpath").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               Commands.argument("to", BlockPosArgument.blockPos())
                  .executes($$0x -> fillBlocks((CommandSourceStack)$$0x.getSource(), BlockPosArgument.getLoadedBlockPos($$0x, "to")))
            )
      );
   }

   private static int fillBlocks(CommandSourceStack $$0, BlockPos $$1) throws CommandSyntaxException {
      if (!($$0.getEntity() instanceof Mob $$3)) {
         throw ERROR_NOT_MOB.create();
      } else {
         PathNavigation $$4 = new GroundPathNavigation($$3, $$0.getLevel());
         Path $$5 = $$4.createPath($$1, 0);
         if ($$5 == null) {
            throw ERROR_NO_PATH.create();
         } else if (!$$5.canReach()) {
            throw ERROR_NOT_COMPLETE.create();
         } else {
            $$0.sendSuccess(() -> Component.literal("Made path"), true);
            return 1;
         }
      }
   }
}
