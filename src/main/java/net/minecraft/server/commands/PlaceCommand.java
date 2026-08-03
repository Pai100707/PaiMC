package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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
import net.minecraft.core.Holder.Reference;
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
   private static final SimpleCommandExceptionType ERROR_FEATURE_FAILED = new SimpleCommandExceptionType(
      Component.translatable("commands.place.feature.failed")
   );
   private static final SimpleCommandExceptionType ERROR_JIGSAW_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.jigsaw.failed"));
   private static final SimpleCommandExceptionType ERROR_STRUCTURE_FAILED = new SimpleCommandExceptionType(
      Component.translatable("commands.place.structure.failed")
   );
   private static final DynamicCommandExceptionType ERROR_TEMPLATE_INVALID = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.place.template.invalid", new Object[]{$$0})
   );
   private static final SimpleCommandExceptionType ERROR_TEMPLATE_FAILED = new SimpleCommandExceptionType(
      Component.translatable("commands.place.template.failed")
   );
   private static final SuggestionProvider<CommandSourceStack> SUGGEST_TEMPLATES = ($$0, $$1) -> {
      StructureTemplateManager $$2 = ((CommandSourceStack)$$0.getSource()).getLevel().getStructureManager();
      return SharedSuggestionProvider.suggestResource($$2.listTemplates(), $$1);
   };

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("place")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                     .then(
                        Commands.literal("feature")
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("feature", ResourceKeyArgument.key(Registries.CONFIGURED_FEATURE))
                                    .executes(
                                       $$0x -> placeFeature(
                                          (CommandSourceStack)$$0x.getSource(),
                                          ResourceKeyArgument.getConfiguredFeature($$0x, "feature"),
                                          BlockPos.containing(((CommandSourceStack)$$0x.getSource()).getPosition())
                                       )
                                    ))
                                 .then(
                                    Commands.argument("pos", BlockPosArgument.blockPos())
                                       .executes(
                                          $$0x -> placeFeature(
                                             (CommandSourceStack)$$0x.getSource(),
                                             ResourceKeyArgument.getConfiguredFeature($$0x, "feature"),
                                             BlockPosArgument.getLoadedBlockPos($$0x, "pos")
                                          )
                                       )
                                 )
                           )
                     ))
                  .then(
                     Commands.literal("jigsaw")
                        .then(
                           Commands.argument("pool", ResourceKeyArgument.key(Registries.TEMPLATE_POOL))
                              .then(
                                 Commands.argument("target", IdentifierArgument.id())
                                    .then(
                                       ((RequiredArgumentBuilder)Commands.argument("max_depth", IntegerArgumentType.integer(1, 20))
                                             .executes(
                                                $$0x -> placeJigsaw(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   ResourceKeyArgument.getStructureTemplatePool($$0x, "pool"),
                                                   IdentifierArgument.getId($$0x, "target"),
                                                   IntegerArgumentType.getInteger($$0x, "max_depth"),
                                                   BlockPos.containing(((CommandSourceStack)$$0x.getSource()).getPosition())
                                                )
                                             ))
                                          .then(
                                             Commands.argument("position", BlockPosArgument.blockPos())
                                                .executes(
                                                   $$0x -> placeJigsaw(
                                                      (CommandSourceStack)$$0x.getSource(),
                                                      ResourceKeyArgument.getStructureTemplatePool($$0x, "pool"),
                                                      IdentifierArgument.getId($$0x, "target"),
                                                      IntegerArgumentType.getInteger($$0x, "max_depth"),
                                                      BlockPosArgument.getLoadedBlockPos($$0x, "position")
                                                   )
                                                )
                                          )
                                    )
                              )
                        )
                  ))
               .then(
                  Commands.literal("structure")
                     .then(
                        ((RequiredArgumentBuilder)Commands.argument("structure", ResourceKeyArgument.key(Registries.STRUCTURE))
                              .executes(
                                 $$0x -> placeStructure(
                                    (CommandSourceStack)$$0x.getSource(),
                                    ResourceKeyArgument.getStructure($$0x, "structure"),
                                    BlockPos.containing(((CommandSourceStack)$$0x.getSource()).getPosition())
                                 )
                              ))
                           .then(
                              Commands.argument("pos", BlockPosArgument.blockPos())
                                 .executes(
                                    $$0x -> placeStructure(
                                       (CommandSourceStack)$$0x.getSource(),
                                       ResourceKeyArgument.getStructure($$0x, "structure"),
                                       BlockPosArgument.getLoadedBlockPos($$0x, "pos")
                                    )
                                 )
                           )
                     )
               ))
            .then(
               Commands.literal("template")
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("template", IdentifierArgument.id())
                           .suggests(SUGGEST_TEMPLATES)
                           .executes(
                              $$0x -> placeTemplate(
                                 (CommandSourceStack)$$0x.getSource(),
                                 IdentifierArgument.getId($$0x, "template"),
                                 BlockPos.containing(((CommandSourceStack)$$0x.getSource()).getPosition()),
                                 Rotation.NONE,
                                 Mirror.NONE,
                                 1.0F,
                                 0,
                                 false
                              )
                           ))
                        .then(
                           ((RequiredArgumentBuilder)Commands.argument("pos", BlockPosArgument.blockPos())
                                 .executes(
                                    $$0x -> placeTemplate(
                                       (CommandSourceStack)$$0x.getSource(),
                                       IdentifierArgument.getId($$0x, "template"),
                                       BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                       Rotation.NONE,
                                       Mirror.NONE,
                                       1.0F,
                                       0,
                                       false
                                    )
                                 ))
                              .then(
                                 ((RequiredArgumentBuilder)Commands.argument("rotation", TemplateRotationArgument.templateRotation())
                                       .executes(
                                          $$0x -> placeTemplate(
                                             (CommandSourceStack)$$0x.getSource(),
                                             IdentifierArgument.getId($$0x, "template"),
                                             BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                             TemplateRotationArgument.getRotation($$0x, "rotation"),
                                             Mirror.NONE,
                                             1.0F,
                                             0,
                                             false
                                          )
                                       ))
                                    .then(
                                       ((RequiredArgumentBuilder)Commands.argument("mirror", TemplateMirrorArgument.templateMirror())
                                             .executes(
                                                $$0x -> placeTemplate(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   IdentifierArgument.getId($$0x, "template"),
                                                   BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                   TemplateRotationArgument.getRotation($$0x, "rotation"),
                                                   TemplateMirrorArgument.getMirror($$0x, "mirror"),
                                                   1.0F,
                                                   0,
                                                   false
                                                )
                                             ))
                                          .then(
                                             ((RequiredArgumentBuilder)Commands.argument("integrity", FloatArgumentType.floatArg(0.0F, 1.0F))
                                                   .executes(
                                                      $$0x -> placeTemplate(
                                                         (CommandSourceStack)$$0x.getSource(),
                                                         IdentifierArgument.getId($$0x, "template"),
                                                         BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                         TemplateRotationArgument.getRotation($$0x, "rotation"),
                                                         TemplateMirrorArgument.getMirror($$0x, "mirror"),
                                                         FloatArgumentType.getFloat($$0x, "integrity"),
                                                         0,
                                                         false
                                                      )
                                                   ))
                                                .then(
                                                   ((RequiredArgumentBuilder)Commands.argument("seed", IntegerArgumentType.integer())
                                                         .executes(
                                                            $$0x -> placeTemplate(
                                                               (CommandSourceStack)$$0x.getSource(),
                                                               IdentifierArgument.getId($$0x, "template"),
                                                               BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                               TemplateRotationArgument.getRotation($$0x, "rotation"),
                                                               TemplateMirrorArgument.getMirror($$0x, "mirror"),
                                                               FloatArgumentType.getFloat($$0x, "integrity"),
                                                               IntegerArgumentType.getInteger($$0x, "seed"),
                                                               false
                                                            )
                                                         ))
                                                      .then(
                                                         Commands.literal("strict")
                                                            .executes(
                                                               $$0x -> placeTemplate(
                                                                  (CommandSourceStack)$$0x.getSource(),
                                                                  IdentifierArgument.getId($$0x, "template"),
                                                                  BlockPosArgument.getLoadedBlockPos($$0x, "pos"),
                                                                  TemplateRotationArgument.getRotation($$0x, "rotation"),
                                                                  TemplateMirrorArgument.getMirror($$0x, "mirror"),
                                                                  FloatArgumentType.getFloat($$0x, "integrity"),
                                                                  IntegerArgumentType.getInteger($$0x, "seed"),
                                                                  true
                                                               )
                                                            )
                                                      )
                                                )
                                          )
                                    )
                              )
                        )
                  )
            )
      );
   }

   public static int placeFeature(CommandSourceStack $$0, Reference<ConfiguredFeature<?, ?>> $$1, BlockPos $$2) throws CommandSyntaxException {
      ServerLevel $$3 = $$0.getLevel();
      ConfiguredFeature<?, ?> $$4 = (ConfiguredFeature<?, ?>)$$1.value();
      ChunkPos $$5 = new ChunkPos($$2);
      checkLoaded($$3, new ChunkPos($$5.x - 1, $$5.z - 1), new ChunkPos($$5.x + 1, $$5.z + 1));
      if (!$$4.place($$3, $$3.getChunkSource().getGenerator(), $$3.getRandom(), $$2)) {
         throw ERROR_FEATURE_FAILED.create();
      } else {
         String $$6 = $$1.key().identifier().toString();
         $$0.sendSuccess(() -> Component.translatable("commands.place.feature.success", new Object[]{$$6, $$2.getX(), $$2.getY(), $$2.getZ()}), true);
         return 1;
      }
   }

   public static int placeJigsaw(CommandSourceStack $$0, Holder<StructureTemplatePool> $$1, Identifier $$2, int $$3, BlockPos $$4) throws CommandSyntaxException {
      ServerLevel $$5 = $$0.getLevel();
      ChunkPos $$6 = new ChunkPos($$4);
      checkLoaded($$5, $$6, $$6);
      if (!JigsawPlacement.generateJigsaw($$5, $$1, $$2, $$3, $$4, false)) {
         throw ERROR_JIGSAW_FAILED.create();
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.place.jigsaw.success", new Object[]{$$4.getX(), $$4.getY(), $$4.getZ()}), true);
         return 1;
      }
   }

   public static int placeStructure(CommandSourceStack $$0, Reference<Structure> $$1, BlockPos $$2) throws CommandSyntaxException {
      ServerLevel $$3 = $$0.getLevel();
      Structure $$4 = (Structure)$$1.value();
      ChunkGenerator $$5 = $$3.getChunkSource().getGenerator();
      StructureStart $$6 = $$4.generate(
         $$1,
         $$3.dimension(),
         $$0.registryAccess(),
         $$5,
         $$5.getBiomeSource(),
         $$3.getChunkSource().randomState(),
         $$3.getStructureManager(),
         $$3.getSeed(),
         new ChunkPos($$2),
         0,
         $$3,
         $$0x -> true
      );
      if (!$$6.isValid()) {
         throw ERROR_STRUCTURE_FAILED.create();
      } else {
         BoundingBox $$7 = $$6.getBoundingBox();
         ChunkPos $$8 = new ChunkPos(SectionPos.blockToSectionCoord($$7.minX()), SectionPos.blockToSectionCoord($$7.minZ()));
         ChunkPos $$9 = new ChunkPos(SectionPos.blockToSectionCoord($$7.maxX()), SectionPos.blockToSectionCoord($$7.maxZ()));
         checkLoaded($$3, $$8, $$9);
         ChunkPos.rangeClosed($$8, $$9)
            .forEach(
               $$3x -> $$6.placeInChunk(
                  $$3,
                  $$3.structureManager(),
                  $$5,
                  $$3.getRandom(),
                  new BoundingBox($$3x.getMinBlockX(), $$3.getMinY(), $$3x.getMinBlockZ(), $$3x.getMaxBlockX(), $$3.getMaxY() + 1, $$3x.getMaxBlockZ()),
                  $$3x
               )
            );
         String $$10 = $$1.key().identifier().toString();
         $$0.sendSuccess(() -> Component.translatable("commands.place.structure.success", new Object[]{$$10, $$2.getX(), $$2.getY(), $$2.getZ()}), true);
         return 1;
      }
   }

   public static int placeTemplate(CommandSourceStack $$0, Identifier $$1, BlockPos $$2, Rotation $$3, Mirror $$4, float $$5, int $$6, boolean $$7) throws CommandSyntaxException {
      ServerLevel $$8 = $$0.getLevel();
      StructureTemplateManager $$9 = $$8.getStructureManager();

      Optional<StructureTemplate> $$10;
      try {
         $$10 = $$9.get($$1);
      } catch (IdentifierException var14) {
         throw ERROR_TEMPLATE_INVALID.create($$1);
      }

      if ($$10.isEmpty()) {
         throw ERROR_TEMPLATE_INVALID.create($$1);
      } else {
         StructureTemplate $$13 = $$10.get();
         checkLoaded($$8, new ChunkPos($$2), new ChunkPos($$2.offset($$13.getSize())));
         StructurePlaceSettings $$14 = new StructurePlaceSettings().setMirror($$4).setRotation($$3).setKnownShape($$7);
         if ($$5 < 1.0F) {
            $$14.clearProcessors().addProcessor(new BlockRotProcessor($$5)).setRandom(StructureBlockEntity.createRandom($$6));
         }

         boolean $$15 = $$13.placeInWorld($$8, $$2, $$2, $$14, StructureBlockEntity.createRandom($$6), 2 | ($$7 ? 816 : 0));
         if (!$$15) {
            throw ERROR_TEMPLATE_FAILED.create();
         } else {
            $$0.sendSuccess(
               () -> Component.translatable("commands.place.template.success", new Object[]{Component.translationArg($$1), $$2.getX(), $$2.getY(), $$2.getZ()}),
               true
            );
            return 1;
         }
      }
   }

   private static void checkLoaded(ServerLevel $$0, ChunkPos $$1, ChunkPos $$2) throws CommandSyntaxException {
      if (ChunkPos.rangeClosed($$1, $$2).filter($$1x -> !$$0.isLoaded($$1x.getWorldPosition())).findAny().isPresent()) {
         throw BlockPosArgument.ERROR_NOT_LOADED.create();
      }
   }
}
