package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FillCommand {
   private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.fill.toobig", new Object[]{$$0, $$1})
   );
   static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), null);
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.fill.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fill").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               Commands.argument("from", BlockPosArgument.blockPos())
                  .then(
                     Commands.argument("to", BlockPosArgument.blockPos())
                        .then(
                           wrapWithMode(
                                 $$1,
                                 Commands.argument("block", BlockStateArgument.block($$1)),
                                 $$0x -> BlockPosArgument.getLoadedBlockPos($$0x, "from"),
                                 $$0x -> BlockPosArgument.getLoadedBlockPos($$0x, "to"),
                                 $$0x -> BlockStateArgument.getBlock($$0x, "block"),
                                 $$0x -> null
                              )
                              .then(
                                 ((LiteralArgumentBuilder)Commands.literal("replace")
                                       .executes(
                                          $$0x -> fillBlocks(
                                             (CommandSourceStack)$$0x.getSource(),
                                             BoundingBox.fromCorners(
                                                BlockPosArgument.getLoadedBlockPos($$0x, "from"), BlockPosArgument.getLoadedBlockPos($$0x, "to")
                                             ),
                                             BlockStateArgument.getBlock($$0x, "block"),
                                             FillCommand.Mode.REPLACE,
                                             null,
                                             false
                                          )
                                       ))
                                    .then(
                                       wrapWithMode(
                                          $$1,
                                          Commands.argument("filter", BlockPredicateArgument.blockPredicate($$1)),
                                          $$0x -> BlockPosArgument.getLoadedBlockPos($$0x, "from"),
                                          $$0x -> BlockPosArgument.getLoadedBlockPos($$0x, "to"),
                                          $$0x -> BlockStateArgument.getBlock($$0x, "block"),
                                          $$0x -> BlockPredicateArgument.getBlockPredicate($$0x, "filter")
                                       )
                                    )
                              )
                              .then(
                                 Commands.literal("keep")
                                    .executes(
                                       $$0x -> fillBlocks(
                                          (CommandSourceStack)$$0x.getSource(),
                                          BoundingBox.fromCorners(
                                             BlockPosArgument.getLoadedBlockPos($$0x, "from"), BlockPosArgument.getLoadedBlockPos($$0x, "to")
                                          ),
                                          BlockStateArgument.getBlock($$0x, "block"),
                                          FillCommand.Mode.REPLACE,
                                          $$0xx -> $$0xx.getLevel().isEmptyBlock($$0xx.getPos()),
                                          false
                                       )
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> wrapWithMode(
      CommandBuildContext $$0,
      ArgumentBuilder<CommandSourceStack, ?> $$1,
      InCommandFunction<CommandContext<CommandSourceStack>, BlockPos> $$2,
      InCommandFunction<CommandContext<CommandSourceStack>, BlockPos> $$3,
      InCommandFunction<CommandContext<CommandSourceStack>, BlockInput> $$4,
      FillCommand.NullableCommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> $$5
   ) {
      return $$1.executes(
            $$4x -> fillBlocks(
               (CommandSourceStack)$$4x.getSource(),
               BoundingBox.fromCorners((Vec3i)$$2.apply($$4x), (Vec3i)$$3.apply($$4x)),
               $$4.apply($$4x),
               FillCommand.Mode.REPLACE,
               $$5.apply($$4x),
               false
            )
         )
         .then(
            Commands.literal("outline")
               .executes(
                  $$4x -> fillBlocks(
                     (CommandSourceStack)$$4x.getSource(),
                     BoundingBox.fromCorners((Vec3i)$$2.apply($$4x), (Vec3i)$$3.apply($$4x)),
                     $$4.apply($$4x),
                     FillCommand.Mode.OUTLINE,
                     $$5.apply($$4x),
                     false
                  )
               )
         )
         .then(
            Commands.literal("hollow")
               .executes(
                  $$4x -> fillBlocks(
                     (CommandSourceStack)$$4x.getSource(),
                     BoundingBox.fromCorners((Vec3i)$$2.apply($$4x), (Vec3i)$$3.apply($$4x)),
                     $$4.apply($$4x),
                     FillCommand.Mode.HOLLOW,
                     $$5.apply($$4x),
                     false
                  )
               )
         )
         .then(
            Commands.literal("destroy")
               .executes(
                  $$4x -> fillBlocks(
                     (CommandSourceStack)$$4x.getSource(),
                     BoundingBox.fromCorners((Vec3i)$$2.apply($$4x), (Vec3i)$$3.apply($$4x)),
                     $$4.apply($$4x),
                     FillCommand.Mode.DESTROY,
                     $$5.apply($$4x),
                     false
                  )
               )
         )
         .then(
            Commands.literal("strict")
               .executes(
                  $$4x -> fillBlocks(
                     (CommandSourceStack)$$4x.getSource(),
                     BoundingBox.fromCorners((Vec3i)$$2.apply($$4x), (Vec3i)$$3.apply($$4x)),
                     $$4.apply($$4x),
                     FillCommand.Mode.REPLACE,
                     $$5.apply($$4x),
                     true
                  )
               )
         );
   }

   private static int fillBlocks(
      CommandSourceStack $$0, BoundingBox $$1, BlockInput $$2, FillCommand.Mode $$3, Predicate<BlockInWorld> $$4, boolean $$5
   ) throws CommandSyntaxException {
      int $$6 = $$1.getXSpan() * $$1.getYSpan() * $$1.getZSpan();
      int $$7 = (Integer)$$0.getLevel().getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS);
      if ($$6 > $$7) {
         throw ERROR_AREA_TOO_LARGE.create($$7, $$6);
      } else {
         record UpdatedPosition(BlockPos pos, BlockState oldState) {
         }

         List<UpdatedPosition> $$8 = Lists.newArrayList();
         ServerLevel $$9 = $$0.getLevel();
         if ($$9.isDebug()) {
            throw ERROR_FAILED.create();
         } else {
            int $$10 = 0;

            for (BlockPos $$11 : BlockPos.betweenClosed($$1.minX(), $$1.minY(), $$1.minZ(), $$1.maxX(), $$1.maxY(), $$1.maxZ())) {
               if ($$4 == null || $$4.test(new BlockInWorld($$9, $$11, true))) {
                  BlockState $$12 = $$9.getBlockState($$11);
                  boolean $$13 = false;
                  if ($$3.affector.affect($$9, $$11)) {
                     $$13 = true;
                  }

                  BlockInput $$14 = $$3.filter.filter($$1, $$11, $$2, $$9);
                  if ($$14 == null) {
                     if ($$13) {
                        $$10++;
                     }
                  } else if (!$$14.place($$9, $$11, 2 | ($$5 ? 816 : 256))) {
                     if ($$13) {
                        $$10++;
                     }
                  } else {
                     if (!$$5) {
                        $$8.add(new UpdatedPosition($$11.immutable(), $$12));
                     }

                     $$10++;
                  }
               }
            }

            for (UpdatedPosition $$15 : $$8) {
               $$9.updateNeighboursOnBlockSet($$15.pos, $$15.oldState);
            }

            if ($$10 == 0) {
               throw ERROR_FAILED.create();
            } else {
               int $$16 = $$10;
               $$0.sendSuccess(() -> Component.translatable("commands.fill.success", new Object[]{$$16}), true);
               return $$10;
            }
         }
      }
   }

   @FunctionalInterface
   public interface Affector {
      FillCommand.Affector NOOP = ($$0, $$1) -> false;

      boolean affect(ServerLevel var1, BlockPos var2);
   }

   @FunctionalInterface
   public interface Filter {
      FillCommand.Filter NOOP = ($$0, $$1, $$2, $$3) -> $$2;

      
      BlockInput filter(BoundingBox var1, BlockPos var2, BlockInput var3, ServerLevel var4);
   }

   static enum Mode {
      REPLACE(FillCommand.Affector.NOOP, FillCommand.Filter.NOOP),
      OUTLINE(
         FillCommand.Affector.NOOP,
         ($$0, $$1, $$2, $$3) -> $$1.getX() != $$0.minX()
               && $$1.getX() != $$0.maxX()
               && $$1.getY() != $$0.minY()
               && $$1.getY() != $$0.maxY()
               && $$1.getZ() != $$0.minZ()
               && $$1.getZ() != $$0.maxZ()
            ? null
            : $$2
      ),
      HOLLOW(
         FillCommand.Affector.NOOP,
         ($$0, $$1, $$2, $$3) -> $$1.getX() != $$0.minX()
               && $$1.getX() != $$0.maxX()
               && $$1.getY() != $$0.minY()
               && $$1.getY() != $$0.maxY()
               && $$1.getZ() != $$0.minZ()
               && $$1.getZ() != $$0.maxZ()
            ? FillCommand.HOLLOW_CORE
            : $$2
      ),
      DESTROY(($$0, $$1) -> $$0.destroyBlock($$1, true), FillCommand.Filter.NOOP);

      public final FillCommand.Filter filter;
      public final FillCommand.Affector affector;

      private Mode(final FillCommand.Affector $$0, final FillCommand.Filter $$1) {
         this.affector = $$0;
         this.filter = $$1;
      }
   }

   @FunctionalInterface
   interface NullableCommandFunction<T, R> {
      
      R apply(T var1) throws CommandSyntaxException;
   }
}
