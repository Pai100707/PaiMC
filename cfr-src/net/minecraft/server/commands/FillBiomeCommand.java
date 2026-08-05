/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.datafixers.util.Either
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
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
    public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType((Message)Component.translatable("argument.pos.unloaded"));
    private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.fillbiome.toobig", $$0, $$1));

    public static void register(CommandDispatcher<CommandSourceStack> $$03, CommandBuildContext $$1) {
        $$03.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fillbiome").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("biome", ResourceArgument.resource($$1, Registries.BIOME)).executes($$02 -> FillBiomeCommand.fill((CommandSourceStack)$$02.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$02, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$02, "to"), ResourceArgument.getResource((CommandContext<CommandSourceStack>)$$02, "biome", Registries.BIOME), $$0 -> true))).then(Commands.literal("replace").then(Commands.argument("filter", ResourceOrTagArgument.resourceOrTag($$1, Registries.BIOME)).executes($$0 -> FillBiomeCommand.fill((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "to"), ResourceArgument.getResource((CommandContext<CommandSourceStack>)$$0, "biome", Registries.BIOME), ResourceOrTagArgument.getResourceOrTag((CommandContext<CommandSourceStack>)$$0, "filter", Registries.BIOME)))))))));
    }

    private static int quantize(int $$0) {
        return QuartPos.toBlock(QuartPos.fromBlock($$0));
    }

    private static BlockPos quantize(BlockPos $$0) {
        return new BlockPos(FillBiomeCommand.quantize($$0.getX()), FillBiomeCommand.quantize($$0.getY()), FillBiomeCommand.quantize($$0.getZ()));
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
            }
            return $$12;
        };
    }

    public static Either<Integer, CommandSyntaxException> fill(ServerLevel $$02, BlockPos $$1, BlockPos $$2, Holder<Biome> $$3) {
        return FillBiomeCommand.fill($$02, $$1, $$2, $$3, $$0 -> true, $$0 -> {});
    }

    public static Either<Integer, CommandSyntaxException> fill(ServerLevel $$0, BlockPos $$1, BlockPos $$2, Holder<Biome> $$3, Predicate<Holder<Biome>> $$4, Consumer<Supplier<Component>> $$5) {
        int $$10;
        BlockPos $$7;
        BlockPos $$6 = FillBiomeCommand.quantize($$1);
        BoundingBox $$8 = BoundingBox.fromCorners($$6, $$7 = FillBiomeCommand.quantize($$2));
        int $$9 = $$8.getXSpan() * $$8.getYSpan() * $$8.getZSpan();
        if ($$9 > ($$10 = $$0.getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS).intValue())) {
            return Either.right((Object)((Object)ERROR_VOLUME_TOO_LARGE.create((Object)$$10, (Object)$$9)));
        }
        ArrayList<ChunkAccess> $$11 = new ArrayList<ChunkAccess>();
        for (int $$12 = SectionPos.blockToSectionCoord($$8.minZ()); $$12 <= SectionPos.blockToSectionCoord($$8.maxZ()); ++$$12) {
            for (int $$13 = SectionPos.blockToSectionCoord($$8.minX()); $$13 <= SectionPos.blockToSectionCoord($$8.maxX()); ++$$13) {
                ChunkAccess $$14 = $$0.getChunk($$13, $$12, ChunkStatus.FULL, false);
                if ($$14 == null) {
                    return Either.right((Object)((Object)ERROR_NOT_LOADED.create()));
                }
                $$11.add($$14);
            }
        }
        MutableInt $$15 = new MutableInt(0);
        for (ChunkAccess $$16 : $$11) {
            $$16.fillBiomesFromNoise(FillBiomeCommand.makeResolver($$15, $$16, $$8, $$3, $$4), $$0.getChunkSource().randomState().sampler());
            $$16.markUnsaved();
        }
        $$0.getChunkSource().chunkMap.resendBiomesForChunks($$11);
        $$5.accept(() -> Component.translatable("commands.fillbiome.success.count", $$15.intValue(), $$8.minX(), $$8.minY(), $$8.minZ(), $$8.maxX(), $$8.maxY(), $$8.maxZ()));
        return Either.left((Object)$$15.intValue());
    }

    private static int fill(CommandSourceStack $$0, BlockPos $$12, BlockPos $$2, Holder.Reference<Biome> $$3, Predicate<Holder<Biome>> $$4) throws CommandSyntaxException {
        Either<Integer, CommandSyntaxException> $$5 = FillBiomeCommand.fill($$0.getLevel(), $$12, $$2, $$3, $$4, $$1 -> $$0.sendSuccess((Supplier<Component>)$$1, true));
        Optional $$6 = $$5.right();
        if ($$6.isPresent()) {
            throw (CommandSyntaxException)((Object)$$6.get());
        }
        return (Integer)$$5.left().get();
    }
}

