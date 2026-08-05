package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

public class CloneCommands {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(Component.translatable("commands.clone.overlap"));
   private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.clone.toobig", new Object[]{$$0, $$1})
   );
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.clone.failed"));
   public static final Predicate<BlockInWorld> FILTER_AIR = $$0 -> !$$0.getState().isAir();

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clone")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .then(beginEndDestinationAndModeSuffix($$1, $$0x -> ((CommandSourceStack)$$0x.getSource()).getLevel())))
            .then(
               Commands.literal("from")
                  .then(
                     Commands.argument("sourceDimension", DimensionArgument.dimension())
                        .then(beginEndDestinationAndModeSuffix($$1, $$0x -> DimensionArgument.getDimension($$0x, "sourceDimension")))
                  )
            )
      );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> beginEndDestinationAndModeSuffix(
      CommandBuildContext $$0, InCommandFunction<CommandContext<CommandSourceStack>, ServerLevel> $$1
   ) {
      return Commands.argument("begin", BlockPosArgument.blockPos())
         .then(
            ((RequiredArgumentBuilder)Commands.argument("end", BlockPosArgument.blockPos())
                  .then(destinationAndStrictSuffix($$0, $$1, $$0x -> ((CommandSourceStack)$$0x.getSource()).getLevel())))
               .then(
                  Commands.literal("to")
                     .then(
                        Commands.argument("targetDimension", DimensionArgument.dimension())
                           .then(destinationAndStrictSuffix($$0, $$1, $$0x -> DimensionArgument.getDimension($$0x, "targetDimension")))
                     )
               )
         );
   }

   private static CloneCommands.DimensionAndPosition getLoadedDimensionAndPosition(CommandContext<CommandSourceStack> $$0, ServerLevel $$1, String $$2) throws CommandSyntaxException {
      BlockPos $$3 = BlockPosArgument.getLoadedBlockPos($$0, $$1, $$2);
      return new CloneCommands.DimensionAndPosition($$1, $$3);
   }

   private static ArgumentBuilder<CommandSourceStack, ?> destinationAndStrictSuffix(
      CommandBuildContext $$0,
      InCommandFunction<CommandContext<CommandSourceStack>, ServerLevel> $$1,
      InCommandFunction<CommandContext<CommandSourceStack>, ServerLevel> $$2
   ) {
      InCommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> $$3 = $$1x -> getLoadedDimensionAndPosition(
         $$1x, $$1.apply($$1x), "begin"
      );
      InCommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> $$4 = $$1x -> getLoadedDimensionAndPosition(
         $$1x, $$1.apply($$1x), "end"
      );
      InCommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> $$5 = $$1x -> getLoadedDimensionAndPosition(
         $$1x, $$2.apply($$1x), "destination"
      );
      return modeSuffix($$0, $$3, $$4, $$5, false, Commands.argument("destination", BlockPosArgument.blockPos()))
         .then(modeSuffix($$0, $$3, $$4, $$5, true, Commands.literal("strict")));
   }

   private static ArgumentBuilder<CommandSourceStack, ?> modeSuffix(
      CommandBuildContext $$0,
      InCommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> $$1,
      InCommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> $$2,
      InCommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> $$3,
      boolean $$4,
      ArgumentBuilder<CommandSourceStack, ?> $$5
   ) {
      return $$5.executes(
            $$4x -> clone(
               (CommandSourceStack)$$4x.getSource(), $$1.apply($$4x), $$2.apply($$4x), $$3.apply($$4x), $$0xx -> true, CloneCommands.Mode.NORMAL, $$4
            )
         )
         .then(wrapWithCloneMode($$1, $$2, $$3, $$0x -> $$0xx -> true, $$4, Commands.literal("replace")))
         .then(wrapWithCloneMode($$1, $$2, $$3, $$0x -> FILTER_AIR, $$4, Commands.literal("masked")))
         .then(
            Commands.literal("filtered")
               .then(
                  wrapWithCloneMode(
                     $$1,
                     $$2,
                     $$3,
                     $$0x -> BlockPredicateArgument.getBlockPredicate($$0x, "filter"),
                     $$4,
                     Commands.argument("filter", BlockPredicateArgument.blockPredicate($$0))
                  )
               )
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> wrapWithCloneMode(
      InCommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> $$0,
      InCommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> $$1,
      InCommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> $$2,
      InCommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> $$3,
      boolean $$4,
      ArgumentBuilder<CommandSourceStack, ?> $$5
   ) {
      return $$5.executes(
            $$5x -> clone(
               (CommandSourceStack)$$5x.getSource(), $$0.apply($$5x), $$1.apply($$5x), $$2.apply($$5x), $$3.apply($$5x), CloneCommands.Mode.NORMAL, $$4
            )
         )
         .then(
            Commands.literal("force")
               .executes(
                  $$5x -> clone(
                     (CommandSourceStack)$$5x.getSource(), $$0.apply($$5x), $$1.apply($$5x), $$2.apply($$5x), $$3.apply($$5x), CloneCommands.Mode.FORCE, $$4
                  )
               )
         )
         .then(
            Commands.literal("move")
               .executes(
                  $$5x -> clone(
                     (CommandSourceStack)$$5x.getSource(), $$0.apply($$5x), $$1.apply($$5x), $$2.apply($$5x), $$3.apply($$5x), CloneCommands.Mode.MOVE, $$4
                  )
               )
         )
         .then(
            Commands.literal("normal")
               .executes(
                  $$5x -> clone(
                     (CommandSourceStack)$$5x.getSource(), $$0.apply($$5x), $$1.apply($$5x), $$2.apply($$5x), $$3.apply($$5x), CloneCommands.Mode.NORMAL, $$4
                  )
               )
         );
   }

   private static int clone(
      CommandSourceStack $$0,
      CloneCommands.DimensionAndPosition $$1,
      CloneCommands.DimensionAndPosition $$2,
      CloneCommands.DimensionAndPosition $$3,
      Predicate<BlockInWorld> $$4,
      CloneCommands.Mode $$5,
      boolean $$6
   ) throws CommandSyntaxException {
      BlockPos $$7 = $$1.position();
      BlockPos $$8 = $$2.position();
      BoundingBox $$9 = BoundingBox.fromCorners($$7, $$8);
      BlockPos $$10 = $$3.position();
      BlockPos $$11 = $$10.offset($$9.getLength());
      BoundingBox $$12 = BoundingBox.fromCorners($$10, $$11);
      ServerLevel $$13 = $$1.dimension();
      ServerLevel $$14 = $$3.dimension();
      if (!$$5.canOverlap() && $$13 == $$14 && $$12.intersects($$9)) {
         throw ERROR_OVERLAP.create();
      } else {
         int $$15 = $$9.getXSpan() * $$9.getYSpan() * $$9.getZSpan();
         int $$16 = (Integer)$$0.getLevel().getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS);
         if ($$15 > $$16) {
            throw ERROR_AREA_TOO_LARGE.create($$16, $$15);
         } else if (!$$13.hasChunksAt($$7, $$8) || !$$14.hasChunksAt($$10, $$11)) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
         } else if ($$14.isDebug()) {
            throw ERROR_FAILED.create();
         } else {
            List<CloneCommands.CloneBlockInfo> $$17 = Lists.newArrayList();
            List<CloneCommands.CloneBlockInfo> $$18 = Lists.newArrayList();
            List<CloneCommands.CloneBlockInfo> $$19 = Lists.newArrayList();
            Deque<BlockPos> $$20 = Lists.newLinkedList();
            int $$21 = 0;
            ScopedCollector $$22 = new ScopedCollector(LOGGER);

            try {
               BlockPos $$23 = new BlockPos($$12.minX() - $$9.minX(), $$12.minY() - $$9.minY(), $$12.minZ() - $$9.minZ());

               for (int $$24 = $$9.minZ(); $$24 <= $$9.maxZ(); $$24++) {
                  for (int $$25 = $$9.minY(); $$25 <= $$9.maxY(); $$25++) {
                     for (int $$26 = $$9.minX(); $$26 <= $$9.maxX(); $$26++) {
                        BlockPos $$27 = new BlockPos($$26, $$25, $$24);
                        BlockPos $$28 = $$27.offset($$23);
                        BlockInWorld $$29 = new BlockInWorld($$13, $$27, false);
                        BlockState $$30 = $$29.getState();
                        if ($$4.test($$29)) {
                           BlockEntity $$31 = $$13.getBlockEntity($$27);
                           if ($$31 != null) {
                              TagValueOutput $$32 = TagValueOutput.createWithContext($$22.forChild($$31.problemPath()), $$0.registryAccess());
                              $$31.saveCustomOnly($$32);
                              CloneCommands.CloneBlockEntityInfo $$33 = new CloneCommands.CloneBlockEntityInfo($$32.buildResult(), $$31.components());
                              $$18.add(new CloneCommands.CloneBlockInfo($$28, $$30, $$33, $$14.getBlockState($$28)));
                              $$20.addLast($$27);
                           } else if (!$$30.isSolidRender() && !$$30.isCollisionShapeFullBlock($$13, $$27)) {
                              $$19.add(new CloneCommands.CloneBlockInfo($$28, $$30, null, $$14.getBlockState($$28)));
                              $$20.addFirst($$27);
                           } else {
                              $$17.add(new CloneCommands.CloneBlockInfo($$28, $$30, null, $$14.getBlockState($$28)));
                              $$20.addLast($$27);
                           }
                        }
                     }
                  }
               }

               int $$34 = 2 | ($$6 ? 816 : 0);
               if ($$5 == CloneCommands.Mode.MOVE) {
                  for (BlockPos $$35 : $$20) {
                     $$13.setBlock($$35, Blocks.BARRIER.defaultBlockState(), $$34 | 816);
                  }

                  int $$36 = $$6 ? $$34 : 3;

                  for (BlockPos $$37 : $$20) {
                     $$13.setBlock($$37, Blocks.AIR.defaultBlockState(), $$36);
                  }
               }

               List<CloneCommands.CloneBlockInfo> $$38 = Lists.newArrayList();
               $$38.addAll($$17);
               $$38.addAll($$18);
               $$38.addAll($$19);
               List<CloneCommands.CloneBlockInfo> $$39 = Lists.reverse($$38);

               for (CloneCommands.CloneBlockInfo $$40 : $$39) {
                  $$14.setBlock($$40.pos, Blocks.BARRIER.defaultBlockState(), $$34 | 816);
               }

               for (CloneCommands.CloneBlockInfo $$41 : $$38) {
                  if ($$14.setBlock($$41.pos, $$41.state, $$34)) {
                     $$21++;
                  }
               }

               for (CloneCommands.CloneBlockInfo $$42 : $$18) {
                  BlockEntity $$43 = $$14.getBlockEntity($$42.pos);
                  if ($$42.blockEntityInfo != null && $$43 != null) {
                     $$43.loadCustomOnly(TagValueInput.create($$22.forChild($$43.problemPath()), $$14.registryAccess(), $$42.blockEntityInfo.tag));
                     $$43.setComponents($$42.blockEntityInfo.components);
                     $$43.setChanged();
                  }

                  $$14.setBlock($$42.pos, $$42.state, $$34);
               }

               if (!$$6) {
                  for (CloneCommands.CloneBlockInfo $$44 : $$39) {
                     $$14.updateNeighboursOnBlockSet($$44.pos, $$44.previousStateAtDestination);
                  }
               }

               $$14.getBlockTicks().copyAreaFrom($$13.getBlockTicks(), $$9, $$23);
            } catch (Throwable var35) {
               try {
                  $$22.close();
               } catch (Throwable var34) {
                  var35.addSuppressed(var34);
               }

               throw var35;
            }

            $$22.close();
            if ($$21 == 0) {
               throw ERROR_FAILED.create();
            } else {
               int $$45 = $$21;
               $$0.sendSuccess(() -> Component.translatable("commands.clone.success", new Object[]{$$45}), true);
               return $$21;
            }
         }
      }
   }

   record CloneBlockEntityInfo(CompoundTag tag, DataComponentMap components) {
   }

   record CloneBlockInfo(BlockPos pos, BlockState state, CloneCommands.CloneBlockEntityInfo blockEntityInfo, BlockState previousStateAtDestination) {
   }

   record DimensionAndPosition(ServerLevel dimension, BlockPos position) {
   }

   static enum Mode {
      FORCE(true),
      MOVE(true),
      NORMAL(false);

      private final boolean canOverlap;

      private Mode(final boolean $$0) {
         this.canOverlap = $$0;
      }

      public boolean canOverlap() {
         return this.canOverlap;
      }
   }
}
