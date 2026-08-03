package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
   public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
   private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.fillbiome.toobig", new Object[]{$$0, $$1})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fillbiome").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               Commands.argument("from", BlockPosArgument.blockPos())
                  .then(
                     Commands.argument("to", BlockPosArgument.blockPos())
                        .then(
                           ((RequiredArgumentBuilder)Commands.argument("biome", ResourceArgument.resource($$1, Registries.BIOME))
                                 .executes(
                                    $$0x -> fill(
                                       (CommandSourceStack)$$0x.getSource(),
                                       BlockPosArgument.getLoadedBlockPos($$0x, "from"),
                                       BlockPosArgument.getLoadedBlockPos($$0x, "to"),
                                       ResourceArgument.getResource($$0x, "biome", Registries.BIOME),
                                       $$0xx -> true
                                    )
                                 ))
                              .then(
                                 Commands.literal("replace")
                                    .then(
                                       Commands.argument("filter", ResourceOrTagArgument.resourceOrTag($$1, Registries.BIOME))
                                          .executes(
                                             $$0x -> fill(
                                                (CommandSourceStack)$$0x.getSource(),
                                                BlockPosArgument.getLoadedBlockPos($$0x, "from"),
                                                BlockPosArgument.getLoadedBlockPos($$0x, "to"),
                                                ResourceArgument.getResource($$0x, "biome", Registries.BIOME),
                                                ResourceOrTagArgument.getResourceOrTag($$0x, "filter", Registries.BIOME)
                                             )
                                          )
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static int quantize(int $$0) {
      return QuartPos.toBlock(QuartPos.fromBlock($$0));
   }

   private static BlockPos quantize(BlockPos $$0) {
      return new BlockPos(quantize($$0.getX()), quantize($$0.getY()), quantize($$0.getZ()));
   }

   private static BiomeResolver makeResolver(MutableInt $$0, ChunkAccess $$1, BoundingBox $$2, Holder<Biome> $$3, Predicate<Holder<Biome>> $$4) {
      return ($$5, $$6, $$7, $$8) -> {
         int $$9 = QuartPos.toBlock($$5);
         int $$10 = QuartPos.toBlock($$6);
         int $$11 = QuartPos.toBlock($$7);
         Holder<Biome> $$12 = $$1.getNoiseBiome($$5, $$6, $$7);
         if ($$2.isInside($$9, $$10, $$11) && $$4.test($$12)) {
            $$0.increment();
            return $$3;
         } else {
            return $$12;
         }
      };
   }

   public static Either<Integer, CommandSyntaxException> fill(ServerLevel $$0, BlockPos $$1, BlockPos $$2, Holder<Biome> $$3) {
      return fill($$0, $$1, $$2, $$3, $$0x -> true, $$0x -> {});
   }

   public static Either<Integer, CommandSyntaxException> fill(
      ServerLevel $$0, BlockPos $$1, BlockPos $$2, Holder<Biome> $$3, Predicate<Holder<Biome>> $$4, Consumer<Supplier<Component>> $$5
   ) {
      BlockPos $$6 = quantize($$1);
      BlockPos $$7 = quantize($$2);
      BoundingBox $$8 = BoundingBox.fromCorners($$6, $$7);
      int $$9 = $$8.getXSpan() * $$8.getYSpan() * $$8.getZSpan();
      int $$10 = (Integer)$$0.getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS);
      if ($$9 > $$10) {
         return Either.right(ERROR_VOLUME_TOO_LARGE.create($$10, $$9));
      } else {
         List<ChunkAccess> $$11 = new ArrayList<>();

         for (int $$12 = SectionPos.blockToSectionCoord($$8.minZ()); $$12 <= SectionPos.blockToSectionCoord($$8.maxZ()); $$12++) {
            for (int $$13 = SectionPos.blockToSectionCoord($$8.minX()); $$13 <= SectionPos.blockToSectionCoord($$8.maxX()); $$13++) {
               ChunkAccess $$14 = $$0.getChunk($$13, $$12, ChunkStatus.FULL, false);
               if ($$14 == null) {
                  return Either.right(ERROR_NOT_LOADED.create());
               }

               $$11.add($$14);
            }
         }

         MutableInt $$15 = new MutableInt(0);

         for (ChunkAccess $$16 : $$11) {
            $$16.fillBiomesFromNoise(makeResolver($$15, $$16, $$8, $$3, $$4), $$0.getChunkSource().randomState().sampler());
            $$16.markUnsaved();
         }

         $$0.getChunkSource().chunkMap.resendBiomesForChunks($$11);
         $$5.accept(
            () -> Component.translatable(
               "commands.fillbiome.success.count", new Object[]{$$15.intValue(), $$8.minX(), $$8.minY(), $$8.minZ(), $$8.maxX(), $$8.maxY(), $$8.maxZ()}
            )
         );
         return Either.left($$15.intValue());
      }
   }

   private static int fill(CommandSourceStack $$0, BlockPos $$1, BlockPos $$2, Reference<Biome> $$3, Predicate<Holder<Biome>> $$4) throws CommandSyntaxException {
      Either<Integer, CommandSyntaxException> $$5 = fill($$0.getLevel(), $$1, $$2, $$3, $$4, $$1x -> $$0.sendSuccess($$1x, true));
      Optional<CommandSyntaxException> $$6 = $$5.right();
      if ($$6.isPresent()) {
         throw (CommandSyntaxException)$$6.get();
      } else {
         return (Integer)$$5.left().get();
      }
   }
}
