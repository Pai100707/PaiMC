package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;

public class DebugMobSpawningCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      LiteralArgumentBuilder<CommandSourceStack> $$1 = (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("debugmobspawning")
         .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));

      for (MobCategory $$2 : MobCategory.values()) {
         $$1.then(
            Commands.literal($$2.getName())
               .then(
                  Commands.argument("at", BlockPosArgument.blockPos())
                     .executes($$1x -> spawnMobs((CommandSourceStack)$$1x.getSource(), $$2, BlockPosArgument.getLoadedBlockPos($$1x, "at")))
               )
         );
      }

      $$0.register($$1);
   }

   private static int spawnMobs(CommandSourceStack $$0, MobCategory $$1, BlockPos $$2) {
      NaturalSpawner.spawnCategoryForPosition($$1, $$0.getLevel(), $$2);
      return 1;
   }
}
