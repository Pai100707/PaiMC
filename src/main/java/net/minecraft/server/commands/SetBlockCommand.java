package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class SetBlockCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.setblock.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      Predicate<BlockInWorld> $$2 = $$0x -> $$0x.getLevel().isEmptyBlock($$0x.getPos());
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setblock").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               Commands.argument("pos", BlockPosArgument.blockPos())
                  .then(
                     ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                       "block", BlockStateArgument.block($$1)
                                    )
                                    .executes(
                                       $$0x -> setBlock(
                                          (CommandSourceStack)$$0x.getSource(),
                                          BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                          BlockStateArgument.getBlock($$0x, "block"),
                                          SetBlockCommand.Mode.REPLACE,
                                          null,
                                          false
                                       )
                                    ))
                                 .then(
                                    Commands.literal("destroy")
                                       .executes(
                                          $$0x -> setBlock(
                                             (CommandSourceStack)$$0x.getSource(),
                                             BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                             BlockStateArgument.getBlock($$0x, "block"),
                                             SetBlockCommand.Mode.DESTROY,
                                             null,
                                             false
                                          )
                                       )
                                 ))
                              .then(
                                 Commands.literal("keep")
                                    .executes(
                                       $$1x -> setBlock(
                                          (CommandSourceStack)$$1x.getSource(),
                                          BlockPosArgument.getLoadedBlockPos($$1x, "pos"),
                                          BlockStateArgument.getBlock($$1x, "block"),
                                          SetBlockCommand.Mode.REPLACE,
                                          $$2,
                                          false
                                       )
                                    )
                              ))
                           .then(
                              Commands.literal("replace")
                                 .executes(
                                    $$0x -> setBlock(
                                       (CommandSourceStack)$$0x.getSource(),
                                       BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                       BlockStateArgument.getBlock($$0x, "block"),
                                       SetBlockCommand.Mode.REPLACE,
                                       null,
                                       false
                                    )
                                 )
                           ))
                        .then(
                           Commands.literal("strict")
                              .executes(
                                 $$0x -> setBlock(
                                    (CommandSourceStack)$$0x.getSource(),
                                    BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                    BlockStateArgument.getBlock($$0x, "block"),
                                    SetBlockCommand.Mode.REPLACE,
                                    null,
                                    true
                                 )
                              )
                        )
                  )
            )
      );
   }

   private static int setBlock(
      CommandSourceStack $$0, BlockPos $$1, BlockInput $$2, SetBlockCommand.Mode $$3, Predicate<BlockInWorld> $$4, boolean $$5
   ) throws CommandSyntaxException {
      ServerLevel $$6 = $$0.getLevel();
      if ($$6.isDebug()) {
         throw ERROR_FAILED.create();
      } else if ($$4 != null && !$$4.test(new BlockInWorld($$6, $$1, true))) {
         throw ERROR_FAILED.create();
      } else {
         boolean $$7;
         if ($$3 == SetBlockCommand.Mode.DESTROY) {
            $$6.destroyBlock($$1, true);
            $$7 = !$$2.getState().isAir() || !$$6.getBlockState($$1).isAir();
         } else {
            $$7 = true;
         }

         BlockState $$9 = $$6.getBlockState($$1);
         if ($$7 && !$$2.place($$6, $$1, 2 | ($$5 ? 816 : 256))) {
            throw ERROR_FAILED.create();
         } else {
            if (!$$5) {
               $$6.updateNeighboursOnBlockSet($$1, $$9);
            }

            $$0.sendSuccess(() -> Component.translatable("commands.setblock.success", new Object[]{$$1.getX(), $$1.getY(), $$1.getZ()}), true);
            return 1;
         }
      }
   }

   public static enum Mode {
      REPLACE,
      DESTROY;
   }
}
