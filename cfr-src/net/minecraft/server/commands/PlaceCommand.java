/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Optional;
import net.minecraft.IdentifierException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class PlaceCommand {
    private static final SimpleCommandExceptionType ERROR_FEATURE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.place.feature.failed"));
    private static final SimpleCommandExceptionType ERROR_JIGSAW_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.place.jigsaw.failed"));
    private static final SimpleCommandExceptionType ERROR_STRUCTURE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.place.structure.failed"));
    private static final DynamicCommandExceptionType ERROR_TEMPLATE_INVALID = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.place.template.invalid", $$0));
    private static final SimpleCommandExceptionType ERROR_TEMPLATE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.place.template.failed"));
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_TEMPLATES = ($$0, $$1) -> {
        StructureTemplateManager $$2 = ((CommandSourceStack)$$0.getSource()).getLevel().getStructureManager();
        return SharedSuggestionProvider.suggestResource($$2.listTemplates(), $$1);
    };

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("place").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("feature").then(((RequiredArgumentBuilder)Commands.argument("feature", ResourceKeyArgument.key(Registries.CONFIGURED_FEATURE)).executes($$0 -> PlaceCommand.placeFeature((CommandSourceStack)$$0.getSource(), ResourceKeyArgument.getConfiguredFeature((CommandContext<CommandSourceStack>)$$0, "feature"), BlockPos.containing(((CommandSourceStack)$$0.getSource()).getPosition())))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes($$0 -> PlaceCommand.placeFeature((CommandSourceStack)$$0.getSource(), ResourceKeyArgument.getConfiguredFeature((CommandContext<CommandSourceStack>)$$0, "feature"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"))))))).then(Commands.literal("jigsaw").then(Commands.argument("pool", ResourceKeyArgument.key(Registries.TEMPLATE_POOL)).then(Commands.argument("target", IdentifierArgument.id()).then(((RequiredArgumentBuilder)Commands.argument("max_depth", IntegerArgumentType.integer((int)1, (int)20)).executes($$0 -> PlaceCommand.placeJigsaw((CommandSourceStack)$$0.getSource(), ResourceKeyArgument.getStructureTemplatePool((CommandContext<CommandSourceStack>)$$0, "pool"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "target"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"max_depth"), BlockPos.containing(((CommandSourceStack)$$0.getSource()).getPosition())))).then(Commands.argument("position", BlockPosArgument.blockPos()).executes($$0 -> PlaceCommand.placeJigsaw((CommandSourceStack)$$0.getSource(), ResourceKeyArgument.getStructureTemplatePool((CommandContext<CommandSourceStack>)$$0, "pool"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "target"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"max_depth"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "position"))))))))).then(Commands.literal("structure").then(((RequiredArgumentBuilder)Commands.argument("structure", ResourceKeyArgument.key(Registries.STRUCTURE)).executes($$0 -> PlaceCommand.placeStructure((CommandSourceStack)$$0.getSource(), ResourceKeyArgument.getStructure((CommandContext<CommandSourceStack>)$$0, "structure"), BlockPos.containing(((CommandSourceStack)$$0.getSource()).getPosition())))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes($$0 -> PlaceCommand.placeStructure((CommandSourceStack)$$0.getSource(), ResourceKeyArgument.getStructure((CommandContext<CommandSourceStack>)$$0, "structure"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"))))))).then(Commands.literal("template").then(((RequiredArgumentBuilder)Commands.argument("template", IdentifierArgument.id()).suggests(SUGGEST_TEMPLATES).executes($$0 -> PlaceCommand.placeTemplate((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "template"), BlockPos.containing(((CommandSourceStack)$$0.getSource()).getPosition()), Rotation.NONE, Mirror.NONE, 1.0f, 0, false))).then(((RequiredArgumentBuilder)Commands.argument("pos", BlockPosArgument.blockPos()).executes($$0 -> PlaceCommand.placeTemplate((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), Rotation.NONE, Mirror.NONE, 1.0f, 0, false))).then(((RequiredArgumentBuilder)Commands.argument("rotation", TemplateRotationArgument.templateRotation()).executes($$0 -> PlaceCommand.placeTemplate((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), TemplateRotationArgument.getRotation((CommandContext<CommandSourceStack>)$$0, "rotation"), Mirror.NONE, 1.0f, 0, false))).then(((RequiredArgumentBuilder)Commands.argument("mirror", TemplateMirrorArgument.templateMirror()).executes($$0 -> PlaceCommand.placeTemplate((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), TemplateRotationArgument.getRotation((CommandContext<CommandSourceStack>)$$0, "rotation"), TemplateMirrorArgument.getMirror((CommandContext<CommandSourceStack>)$$0, "mirror"), 1.0f, 0, false))).then(((RequiredArgumentBuilder)Commands.argument("integrity", FloatArgumentType.floatArg((float)0.0f, (float)1.0f)).executes($$0 -> PlaceCommand.placeTemplate((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), TemplateRotationArgument.getRotation((CommandContext<CommandSourceStack>)$$0, "rotation"), TemplateMirrorArgument.getMirror((CommandContext<CommandSourceStack>)$$0, "mirror"), FloatArgumentType.getFloat((CommandContext)$$0, (String)"integrity"), 0, false))).then(((RequiredArgumentBuilder)Commands.argument("seed", IntegerArgumentType.integer()).executes($$0 -> PlaceCommand.placeTemplate((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), TemplateRotationArgument.getRotation((CommandContext<CommandSourceStack>)$$0, "rotation"), TemplateMirrorArgument.getMirror((CommandContext<CommandSourceStack>)$$0, "mirror"), FloatArgumentType.getFloat((CommandContext)$$0, (String)"integrity"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"seed"), false))).then(Commands.literal("strict").executes($$0 -> PlaceCommand.placeTemplate((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "template"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), TemplateRotationArgument.getRotation((CommandContext<CommandSourceStack>)$$0, "rotation"), TemplateMirrorArgument.getMirror((CommandContext<CommandSourceStack>)$$0, "mirror"), FloatArgumentType.getFloat((CommandContext)$$0, (String)"integrity"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"seed"), true)))))))))));
    }

    public static int placeFeature(CommandSourceStack $$0, Holder.Reference<ConfiguredFeature<?, ?>> $$1, BlockPos $$2) throws CommandSyntaxException {
        ServerLevel $$3 = $$0.getLevel();
        ConfiguredFeature<?, ?> $$4 = $$1.value();
        ChunkPos $$5 = new ChunkPos($$2);
        PlaceCommand.checkLoaded($$3, new ChunkPos($$5.x - 1, $$5.z - 1), new ChunkPos($$5.x + 1, $$5.z + 1));
        if (!$$4.place($$3, $$3.getChunkSource().getGenerator(), $$3.getRandom(), $$2)) {
            throw ERROR_FEATURE_FAILED.create();
        }
        String $$6 = $$1.key().identifier().toString();
        $$0.sendSuccess(() -> Component.translatable("commands.place.feature.success", $$6, $$2.getX(), $$2.getY(), $$2.getZ()), true);
        return 1;
    }

    public static int placeJigsaw(CommandSourceStack $$0, Holder<StructureTemplatePool> $$1, Identifier $$2, int $$3, BlockPos $$4) throws CommandSyntaxException {
        ServerLevel $$5 = $$0.getLevel();
        ChunkPos $$6 = new ChunkPos($$4);
        PlaceCommand.checkLoaded($$5, $$6, $$6);
        if (!JigsawPlacement.generateJigsaw($$5, $$1, $$2, $$3, $$4, false)) {
            throw ERROR_JIGSAW_FAILED.create();
        }
        $$0.sendSuccess(() -> Component.translatable("commands.place.jigsaw.success", $$4.getX(), $$4.getY(), $$4.getZ()), true);
        return 1;
    }

    public static int placeStructure(CommandSourceStack $$02, Holder.Reference<Structure> $$1, BlockPos $$2) throws CommandSyntaxException {
        ServerLevel $$32 = $$02.getLevel();
        Structure $$4 = $$1.value();
        ChunkGenerator $$5 = $$32.getChunkSource().getGenerator();
        StructureStart $$6 = $$4.generate($$1, $$32.dimension(), $$02.registryAccess(), $$5, $$5.getBiomeSource(), $$32.getChunkSource().randomState(), $$32.getStructureManager(), $$32.getSeed(), new ChunkPos($$2), 0, $$32, $$0 -> true);
        if (!$$6.isValid()) {
            throw ERROR_STRUCTURE_FAILED.create();
        }
        BoundingBox $$7 = $$6.getBoundingBox();
        ChunkPos $$8 = new ChunkPos(SectionPos.blockToSectionCoord($$7.minX()), SectionPos.blockToSectionCoord($$7.minZ()));
        ChunkPos $$9 = new ChunkPos(SectionPos.blockToSectionCoord($$7.maxX()), SectionPos.blockToSectionCoord($$7.maxZ()));
        PlaceCommand.checkLoaded($$32, $$8, $$9);
        ChunkPos.rangeClosed($$8, $$9).forEach($$3 -> $$6.placeInChunk($$32, $$32.structureManager(), $$5, $$32.getRandom(), new BoundingBox($$3.getMinBlockX(), $$32.getMinY(), $$3.getMinBlockZ(), $$3.getMaxBlockX(), $$32.getMaxY() + 1, $$3.getMaxBlockZ()), (ChunkPos)$$3));
        String $$10 = $$1.key().identifier().toString();
        $$02.sendSuccess(() -> Component.translatable("commands.place.structure.success", $$10, $$2.getX(), $$2.getY(), $$2.getZ()), true);
        return 1;
    }

    /*
     * WARNING - void declaration
     */
    public static int placeTemplate(CommandSourceStack $$0, Identifier $$1, BlockPos $$2, Rotation $$3, Mirror $$4, float $$5, int $$6, boolean $$7) throws CommandSyntaxException {
        boolean $$15;
        void $$12;
        ServerLevel $$8 = $$0.getLevel();
        StructureTemplateManager $$9 = $$8.getStructureManager();
        try {
            Optional<StructureTemplate> $$10 = $$9.get($$1);
        }
        catch (IdentifierException $$11) {
            throw ERROR_TEMPLATE_INVALID.create((Object)$$1);
        }
        if ($$12.isEmpty()) {
            throw ERROR_TEMPLATE_INVALID.create((Object)$$1);
        }
        StructureTemplate $$13 = (StructureTemplate)$$12.get();
        PlaceCommand.checkLoaded($$8, new ChunkPos($$2), new ChunkPos($$2.offset($$13.getSize())));
        StructurePlaceSettings $$14 = new StructurePlaceSettings().setMirror($$4).setRotation($$3).setKnownShape($$7);
        if ($$5 < 1.0f) {
            $$14.clearProcessors().addProcessor(new BlockRotProcessor($$5)).setRandom(StructureBlockEntity.createRandom($$6));
        }
        if (!($$15 = $$13.placeInWorld($$8, $$2, $$2, $$14, StructureBlockEntity.createRandom($$6), 2 | ($$7 ? 816 : 0)))) {
            throw ERROR_TEMPLATE_FAILED.create();
        }
        $$0.sendSuccess(() -> Component.translatable("commands.place.template.success", Component.translationArg($$1), $$2.getX(), $$2.getY(), $$2.getZ()), true);
        return 1;
    }

    private static void checkLoaded(ServerLevel $$0, ChunkPos $$12, ChunkPos $$2) throws CommandSyntaxException {
        if (ChunkPos.rangeClosed($$12, $$2).filter($$1 -> !$$0.isLoaded($$1.getWorldPosition())).findAny().isPresent()) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
    }
}

