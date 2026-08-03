package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceSelectorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.CopyToClipboard;
import net.minecraft.network.chat.ClickEvent.SuggestCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.protocol.game.ClientboundGameTestHighlightPosPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.InCommandFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.mutable.MutableInt;

public class TestCommand {
   public static final int TEST_NEARBY_SEARCH_RADIUS = 15;
   public static final int TEST_FULL_SEARCH_RADIUS = 250;
   public static final int VERIFY_TEST_GRID_AXIS_SIZE = 10;
   public static final int VERIFY_TEST_BATCH_SIZE = 100;
   private static final int DEFAULT_CLEAR_RADIUS = 250;
   private static final int MAX_CLEAR_RADIUS = 1024;
   private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
   private static final int DEFAULT_X_SIZE = 5;
   private static final int DEFAULT_Y_SIZE = 5;
   private static final int DEFAULT_Z_SIZE = 5;
   private static final SimpleCommandExceptionType CLEAR_NO_TESTS = new SimpleCommandExceptionType(Component.translatable("commands.test.clear.error.no_tests"));
   private static final SimpleCommandExceptionType RESET_NO_TESTS = new SimpleCommandExceptionType(Component.translatable("commands.test.reset.error.no_tests"));
   private static final SimpleCommandExceptionType TEST_INSTANCE_COULD_NOT_BE_FOUND = new SimpleCommandExceptionType(
      Component.translatable("commands.test.error.test_instance_not_found")
   );
   private static final SimpleCommandExceptionType NO_STRUCTURES_TO_EXPORT = new SimpleCommandExceptionType(
      Component.literal("Could not find any structures to export")
   );
   private static final SimpleCommandExceptionType NO_TEST_INSTANCES = new SimpleCommandExceptionType(
      Component.translatable("commands.test.error.no_test_instances")
   );
   private static final Dynamic3CommandExceptionType NO_TEST_CONTAINING = new Dynamic3CommandExceptionType(
      ($$0, $$1, $$2) -> Component.translatableEscape("commands.test.error.no_test_containing_pos", new Object[]{$$0, $$1, $$2})
   );
   private static final DynamicCommandExceptionType TOO_LARGE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.test.error.too_large", new Object[]{$$0})
   );

   private static int reset(TestFinder $$0) throws CommandSyntaxException {
      stopTests();
      int $$1 = toGameTestInfos($$0.source(), RetryOptions.noRetries(), $$0).map($$1x -> resetGameTestInfo($$0.source(), $$1x)).toList().size();
      if ($$1 == 0) {
         throw CLEAR_NO_TESTS.create();
      } else {
         $$0.source().sendSuccess(() -> Component.translatable("commands.test.reset.success", new Object[]{$$1}), true);
         return $$1;
      }
   }

   private static int clear(TestFinder $$0) throws CommandSyntaxException {
      stopTests();
      CommandSourceStack $$1 = $$0.source();
      ServerLevel $$2 = $$1.getLevel();
      List<TestInstanceBlockEntity> $$3 = $$0.findTestPos().flatMap($$1x -> $$2.getBlockEntity($$1x, BlockEntityType.TEST_INSTANCE_BLOCK).stream()).toList();

      for (TestInstanceBlockEntity $$4 : $$3) {
         StructureUtils.clearSpaceForStructure($$4.getStructureBoundingBox(), $$2);
         $$4.removeBarriers();
         $$2.destroyBlock($$4.getBlockPos(), false);
      }

      if ($$3.isEmpty()) {
         throw CLEAR_NO_TESTS.create();
      } else {
         $$1.sendSuccess(() -> Component.translatable("commands.test.clear.success", new Object[]{$$3.size()}), true);
         return $$3.size();
      }
   }

   private static int export(TestFinder $$0) throws CommandSyntaxException {
      CommandSourceStack $$1 = $$0.source();
      ServerLevel $$2 = $$1.getLevel();
      int $$3 = 0;
      boolean $$4 = true;

      for (Iterator<BlockPos> $$5 = $$0.findTestPos().iterator(); $$5.hasNext(); $$3++) {
         BlockPos $$6 = $$5.next();
         if (!($$2.getBlockEntity($$6) instanceof TestInstanceBlockEntity $$7)) {
            throw TEST_INSTANCE_COULD_NOT_BE_FOUND.create();
         }

         if (!$$7.exportTest($$1::sendSystemMessage)) {
            $$4 = false;
         }
      }

      if ($$3 == 0) {
         throw NO_STRUCTURES_TO_EXPORT.create();
      } else {
         String $$8 = "Exported " + $$3 + " structures";
         $$0.source().sendSuccess(() -> Component.literal($$8), true);
         return $$4 ? 0 : 1;
      }
   }

   private static int verify(TestFinder $$0) {
      stopTests();
      CommandSourceStack $$1 = $$0.source();
      ServerLevel $$2 = $$1.getLevel();
      BlockPos $$3 = createTestPositionAround($$1);
      Collection<GameTestInfo> $$4 = Stream.concat(toGameTestInfos($$1, RetryOptions.noRetries(), $$0), toGameTestInfo($$1, RetryOptions.noRetries(), $$0, 0))
         .toList();
      FailedTestTracker.forgetFailedTests();
      Collection<GameTestBatch> $$5 = new ArrayList<>();

      for (GameTestInfo $$6 : $$4) {
         for (Rotation $$7 : Rotation.values()) {
            Collection<GameTestInfo> $$8 = new ArrayList<>();

            for (int $$9 = 0; $$9 < 100; $$9++) {
               GameTestInfo $$10 = new GameTestInfo($$6.getTestHolder(), $$7, $$2, new RetryOptions(1, true));
               $$10.setTestBlockPos($$6.getTestBlockPos());
               $$8.add($$10);
            }

            GameTestBatch $$11 = GameTestBatchFactory.toGameTestBatch($$8, $$6.getTest().batch(), $$7.ordinal());
            $$5.add($$11);
         }
      }

      StructureGridSpawner $$12 = new StructureGridSpawner($$3, 10, true);
      GameTestRunner $$13 = GameTestRunner.Builder.fromBatches($$5, $$2)
         .batcher(GameTestBatchFactory.fromGameTestInfo(100))
         .newStructureSpawner($$12)
         .existingStructureSpawner($$12)
         .haltOnError()
         .clearBetweenBatches()
         .build();
      return trackAndStartRunner($$1, $$13);
   }

   private static int run(TestFinder $$0, RetryOptions $$1, int $$2, int $$3) {
      stopTests();
      CommandSourceStack $$4 = $$0.source();
      ServerLevel $$5 = $$4.getLevel();
      BlockPos $$6 = createTestPositionAround($$4);
      Collection<GameTestInfo> $$7 = Stream.concat(toGameTestInfos($$4, $$1, $$0), toGameTestInfo($$4, $$1, $$0, $$2)).toList();
      if ($$7.isEmpty()) {
         $$4.sendSuccess(() -> Component.translatable("commands.test.no_tests"), false);
         return 0;
      } else {
         FailedTestTracker.forgetFailedTests();
         $$4.sendSuccess(() -> Component.translatable("commands.test.run.running", new Object[]{$$7.size()}), false);
         GameTestRunner $$8 = GameTestRunner.Builder.fromInfo($$7, $$5).newStructureSpawner(new StructureGridSpawner($$6, $$3, false)).build();
         return trackAndStartRunner($$4, $$8);
      }
   }

   private static int locate(TestFinder $$0) throws CommandSyntaxException {
      $$0.source().sendSystemMessage(Component.translatable("commands.test.locate.started"));
      MutableInt $$1 = new MutableInt(0);
      BlockPos $$2 = BlockPos.containing($$0.source().getPosition());
      $$0.findTestPos()
         .forEach(
            $$3x -> {
               if ($$0.source().getLevel().getBlockEntity($$3x) instanceof TestInstanceBlockEntity $$5) {
                  Direction var13 = $$5.getRotation().rotate(Direction.NORTH);
                  BlockPos $$8 = $$5.getBlockPos().relative(var13, 2);
                  int $$9 = (int)var13.getOpposite().toYRot();
                  String $$10 = String.format(Locale.ROOT, "/tp @s %d %d %d %d 0", $$8.getX(), $$8.getY(), $$8.getZ(), $$9);
                  int $$11 = $$2.getX() - $$3x.getX();
                  int $$12 = $$2.getZ() - $$3x.getZ();
                  int $$13 = Mth.floor(Mth.sqrt($$11 * $$11 + $$12 * $$12));
                  MutableComponent $$14 = ComponentUtils.wrapInSquareBrackets(
                        Component.translatable("chat.coordinates", new Object[]{$$3x.getX(), $$3x.getY(), $$3x.getZ()})
                     )
                     .withStyle(
                        $$1xx -> $$1xx.withColor(ChatFormatting.GREEN)
                           .withClickEvent(new SuggestCommand($$10))
                           .withHoverEvent(new ShowText(Component.translatable("chat.coordinates.tooltip")))
                     );
                  $$0.source().sendSuccess(() -> Component.translatable("commands.test.locate.found", new Object[]{$$14, $$13}), false);
                  $$1.increment();
               }
            }
         );
      int $$3 = $$1.intValue();
      if ($$3 == 0) {
         throw NO_TEST_INSTANCES.create();
      } else {
         $$0.source().sendSuccess(() -> Component.translatable("commands.test.locate.done", new Object[]{$$3}), true);
         return $$3;
      }
   }

   private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(
      ArgumentBuilder<CommandSourceStack, ?> $$0,
      InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> $$1,
      Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> $$2
   ) {
      return $$0.executes($$1x -> run((TestFinder)$$1.apply($$1x), RetryOptions.noRetries(), 0, 8))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("numberOfTimes", IntegerArgumentType.integer(0))
                  .executes($$1x -> run((TestFinder)$$1.apply($$1x), new RetryOptions(IntegerArgumentType.getInteger($$1x, "numberOfTimes"), false), 0, 8)))
               .then(
                  $$2.apply(
                     Commands.argument("untilFailed", BoolArgumentType.bool())
                        .executes(
                           $$1x -> run(
                              (TestFinder)$$1.apply($$1x),
                              new RetryOptions(IntegerArgumentType.getInteger($$1x, "numberOfTimes"), BoolArgumentType.getBool($$1x, "untilFailed")),
                              0,
                              8
                           )
                        )
                  )
               )
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(
      ArgumentBuilder<CommandSourceStack, ?> $$0, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> $$1
   ) {
      return runWithRetryOptions($$0, $$1, $$0x -> $$0x);
   }

   private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptionsAndBuildInfo(
      ArgumentBuilder<CommandSourceStack, ?> $$0, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> $$1
   ) {
      return runWithRetryOptions(
         $$0,
         $$1,
         $$1x -> $$1x.then(
            ((RequiredArgumentBuilder)Commands.argument("rotationSteps", IntegerArgumentType.integer())
                  .executes(
                     $$1xx -> run(
                        (TestFinder)$$1.apply($$1xx),
                        new RetryOptions(IntegerArgumentType.getInteger($$1xx, "numberOfTimes"), BoolArgumentType.getBool($$1xx, "untilFailed")),
                        IntegerArgumentType.getInteger($$1xx, "rotationSteps"),
                        8
                     )
                  ))
               .then(
                  Commands.argument("testsPerRow", IntegerArgumentType.integer())
                     .executes(
                        $$1xx -> run(
                           (TestFinder)$$1.apply($$1xx),
                           new RetryOptions(IntegerArgumentType.getInteger($$1xx, "numberOfTimes"), BoolArgumentType.getBool($$1xx, "untilFailed")),
                           IntegerArgumentType.getInteger($$1xx, "rotationSteps"),
                           IntegerArgumentType.getInteger($$1xx, "testsPerRow")
                        )
                     )
               )
         )
      );
   }

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      ArgumentBuilder<CommandSourceStack, ?> $$2 = runWithRetryOptionsAndBuildInfo(
         Commands.argument("onlyRequiredTests", BoolArgumentType.bool()),
         $$0x -> TestFinder.builder().failedTests($$0x, BoolArgumentType.getBool($$0x, "onlyRequiredTests"))
      );
      LiteralArgumentBuilder<CommandSourceStack> $$3 = (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                               "test"
                                                            )
                                                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                                                         .then(
                                                            Commands.literal("run")
                                                               .then(
                                                                  runWithRetryOptionsAndBuildInfo(
                                                                     Commands.argument(
                                                                        "tests", ResourceSelectorArgument.resourceSelector($$1, Registries.TEST_INSTANCE)
                                                                     ),
                                                                     $$0x -> TestFinder.builder()
                                                                        .byResourceSelection($$0x, ResourceSelectorArgument.getSelectedResources($$0x, "tests"))
                                                                  )
                                                               )
                                                         ))
                                                      .then(
                                                         Commands.literal("runmultiple")
                                                            .then(
                                                               ((RequiredArgumentBuilder)Commands.argument(
                                                                        "tests", ResourceSelectorArgument.resourceSelector($$1, Registries.TEST_INSTANCE)
                                                                     )
                                                                     .executes(
                                                                        $$0x -> run(
                                                                           TestFinder.builder()
                                                                              .byResourceSelection(
                                                                                 $$0x, ResourceSelectorArgument.getSelectedResources($$0x, "tests")
                                                                              ),
                                                                           RetryOptions.noRetries(),
                                                                           0,
                                                                           8
                                                                        )
                                                                     ))
                                                                  .then(
                                                                     Commands.argument("amount", IntegerArgumentType.integer())
                                                                        .executes(
                                                                           $$0x -> run(
                                                                              TestFinder.builder()
                                                                                 .createMultipleCopies(IntegerArgumentType.getInteger($$0x, "amount"))
                                                                                 .byResourceSelection(
                                                                                    $$0x, ResourceSelectorArgument.getSelectedResources($$0x, "tests")
                                                                                 ),
                                                                              RetryOptions.noRetries(),
                                                                              0,
                                                                              8
                                                                           )
                                                                        )
                                                                  )
                                                            )
                                                      ))
                                                   .then(runWithRetryOptions(Commands.literal("runthese"), TestFinder.builder()::allNearby)))
                                                .then(runWithRetryOptions(Commands.literal("runclosest"), TestFinder.builder()::nearest)))
                                             .then(runWithRetryOptions(Commands.literal("runthat"), TestFinder.builder()::lookedAt)))
                                          .then(runWithRetryOptionsAndBuildInfo(Commands.literal("runfailed").then($$2), TestFinder.builder()::failedTests)))
                                       .then(
                                          Commands.literal("verify")
                                             .then(
                                                Commands.argument("tests", ResourceSelectorArgument.resourceSelector($$1, Registries.TEST_INSTANCE))
                                                   .executes(
                                                      $$0x -> verify(
                                                         TestFinder.builder()
                                                            .byResourceSelection($$0x, ResourceSelectorArgument.getSelectedResources($$0x, "tests"))
                                                      )
                                                   )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("locate")
                                          .then(
                                             Commands.argument("tests", ResourceSelectorArgument.resourceSelector($$1, Registries.TEST_INSTANCE))
                                                .executes(
                                                   $$0x -> locate(
                                                      TestFinder.builder()
                                                         .byResourceSelection($$0x, ResourceSelectorArgument.getSelectedResources($$0x, "tests"))
                                                   )
                                                )
                                          )
                                    ))
                                 .then(Commands.literal("resetclosest").executes($$0x -> reset(TestFinder.builder().nearest($$0x)))))
                              .then(Commands.literal("resetthese").executes($$0x -> reset(TestFinder.builder().allNearby($$0x)))))
                           .then(Commands.literal("resetthat").executes($$0x -> reset(TestFinder.builder().lookedAt($$0x)))))
                        .then(Commands.literal("clearthat").executes($$0x -> clear(TestFinder.builder().lookedAt($$0x)))))
                     .then(Commands.literal("clearthese").executes($$0x -> clear(TestFinder.builder().allNearby($$0x)))))
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("clearall").executes($$0x -> clear(TestFinder.builder().radius($$0x, 250))))
                        .then(
                           Commands.argument("radius", IntegerArgumentType.integer())
                              .executes($$0x -> clear(TestFinder.builder().radius($$0x, Mth.clamp(IntegerArgumentType.getInteger($$0x, "radius"), 0, 1024))))
                        )
                  ))
               .then(Commands.literal("stop").executes($$0x -> stopTests())))
            .then(
               ((LiteralArgumentBuilder)Commands.literal("pos").executes($$0x -> showPos((CommandSourceStack)$$0x.getSource(), "pos")))
                  .then(
                     Commands.argument("var", StringArgumentType.word())
                        .executes($$0x -> showPos((CommandSourceStack)$$0x.getSource(), StringArgumentType.getString($$0x, "var")))
                  )
            ))
         .then(
            Commands.literal("create")
               .then(
                  ((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id())
                        .suggests(TestCommand::suggestTestFunction)
                        .executes($$0x -> createNewStructure((CommandSourceStack)$$0x.getSource(), IdentifierArgument.getId($$0x, "id"), 5, 5, 5)))
                     .then(
                        ((RequiredArgumentBuilder)Commands.argument("width", IntegerArgumentType.integer())
                              .executes(
                                 $$0x -> createNewStructure(
                                    (CommandSourceStack)$$0x.getSource(),
                                    IdentifierArgument.getId($$0x, "id"),
                                    IntegerArgumentType.getInteger($$0x, "width"),
                                    IntegerArgumentType.getInteger($$0x, "width"),
                                    IntegerArgumentType.getInteger($$0x, "width")
                                 )
                              ))
                           .then(
                              Commands.argument("height", IntegerArgumentType.integer())
                                 .then(
                                    Commands.argument("depth", IntegerArgumentType.integer())
                                       .executes(
                                          $$0x -> createNewStructure(
                                             (CommandSourceStack)$$0x.getSource(),
                                             IdentifierArgument.getId($$0x, "id"),
                                             IntegerArgumentType.getInteger($$0x, "width"),
                                             IntegerArgumentType.getInteger($$0x, "height"),
                                             IntegerArgumentType.getInteger($$0x, "depth")
                                          )
                                       )
                                 )
                           )
                     )
               )
         );
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         $$3 = (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)$$3.then(
                     Commands.literal("export")
                        .then(
                           Commands.argument("test", ResourceArgument.resource($$1, Registries.TEST_INSTANCE))
                              .executes(
                                 $$0x -> exportTestStructure(
                                    (CommandSourceStack)$$0x.getSource(), ResourceArgument.getResource($$0x, "test", Registries.TEST_INSTANCE)
                                 )
                              )
                        )
                  ))
                  .then(Commands.literal("exportclosest").executes($$0x -> export(TestFinder.builder().nearest($$0x)))))
               .then(Commands.literal("exportthese").executes($$0x -> export(TestFinder.builder().allNearby($$0x)))))
            .then(Commands.literal("exportthat").executes($$0x -> export(TestFinder.builder().lookedAt($$0x))));
      }

      $$0.register($$3);
   }

   public static CompletableFuture<Suggestions> suggestTestFunction(CommandContext<CommandSourceStack> $$0, SuggestionsBuilder $$1) {
      Stream<String> $$2 = ((CommandSourceStack)$$0.getSource())
         .registryAccess()
         .lookupOrThrow(Registries.TEST_FUNCTION)
         .listElements()
         .map(Holder::getRegisteredName);
      return SharedSuggestionProvider.suggest($$2, $$1);
   }

   private static int resetGameTestInfo(CommandSourceStack $$0, GameTestInfo $$1) {
      TestInstanceBlockEntity $$2 = $$1.getTestInstanceBlockEntity();
      $$2.resetTest($$0::sendSystemMessage);
      return 1;
   }

   private static Stream<GameTestInfo> toGameTestInfos(CommandSourceStack $$0, RetryOptions $$1, TestPosFinder $$2) {
      return $$2.findTestPos().map($$2x -> createGameTestInfo($$2x, $$0, $$1)).flatMap(Optional::stream);
   }

   private static Stream<GameTestInfo> toGameTestInfo(CommandSourceStack $$0, RetryOptions $$1, TestInstanceFinder $$2, int $$3) {
      return $$2.findTests()
         .filter($$1x -> verifyStructureExists($$0, ((GameTestInstance)$$1x.value()).structure()))
         .map($$3x -> new GameTestInfo($$3x, StructureUtils.getRotationForRotationSteps($$3), $$0.getLevel(), $$1));
   }

   private static Optional<GameTestInfo> createGameTestInfo(BlockPos $$0, CommandSourceStack $$1, RetryOptions $$2) {
      ServerLevel $$3 = $$1.getLevel();
      if ($$3.getBlockEntity($$0) instanceof TestInstanceBlockEntity $$4) {
         Optional<Reference<GameTestInstance>> $$6 = $$4.test().flatMap($$1.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE)::get);
         if ($$6.isEmpty()) {
            $$1.sendFailure(Component.translatable("commands.test.error.non_existant_test", new Object[]{$$4.getTestName()}));
            return Optional.empty();
         } else {
            Reference<GameTestInstance> $$7 = $$6.get();
            GameTestInfo $$8 = new GameTestInfo($$7, $$4.getRotation(), $$3, $$2);
            $$8.setTestBlockPos($$0);
            return !verifyStructureExists($$1, $$8.getStructure()) ? Optional.empty() : Optional.of($$8);
         }
      } else {
         $$1.sendFailure(Component.translatable("commands.test.error.test_instance_not_found.position", new Object[]{$$0.getX(), $$0.getY(), $$0.getZ()}));
         return Optional.empty();
      }
   }

   private static int createNewStructure(CommandSourceStack $$0, Identifier $$1, int $$2, int $$3, int $$4) throws CommandSyntaxException {
      if ($$2 <= 48 && $$3 <= 48 && $$4 <= 48) {
         ServerLevel $$5 = $$0.getLevel();
         BlockPos $$6 = createTestPositionAround($$0);
         TestInstanceBlockEntity $$7 = StructureUtils.createNewEmptyTest($$1, $$6, new Vec3i($$2, $$3, $$4), Rotation.NONE, $$5);
         BlockPos $$8 = $$7.getStructurePos();
         BlockPos $$9 = $$8.offset($$2 - 1, 0, $$4 - 1);
         BlockPos.betweenClosedStream($$8, $$9).forEach($$1x -> $$5.setBlockAndUpdate($$1x, Blocks.BEDROCK.defaultBlockState()));
         $$0.sendSuccess(() -> Component.translatable("commands.test.create.success", new Object[]{$$7.getTestName()}), true);
         return 1;
      } else {
         throw TOO_LARGE.create(48);
      }
   }

   private static int showPos(CommandSourceStack $$0, String $$1) throws CommandSyntaxException {
      ServerPlayer $$2 = $$0.getPlayerOrException();
      BlockHitResult $$3 = (BlockHitResult)$$2.pick(10.0, 1.0F, false);
      BlockPos $$4 = $$3.getBlockPos();
      ServerLevel $$5 = $$0.getLevel();
      Optional<BlockPos> $$6 = StructureUtils.findTestContainingPos($$4, 15, $$5);
      if ($$6.isEmpty()) {
         $$6 = StructureUtils.findTestContainingPos($$4, 250, $$5);
      }

      if ($$6.isEmpty()) {
         throw NO_TEST_CONTAINING.create($$4.getX(), $$4.getY(), $$4.getZ());
      } else if ($$5.getBlockEntity($$6.get()) instanceof TestInstanceBlockEntity $$7) {
         BlockPos var13 = $$7.getStructurePos();
         BlockPos $$10 = $$4.subtract(var13);
         String $$11 = $$10.getX() + ", " + $$10.getY() + ", " + $$10.getZ();
         String $$12 = $$7.getTestName().getString();
         MutableComponent $$13 = Component.translatable("commands.test.coordinates", new Object[]{$$10.getX(), $$10.getY(), $$10.getZ()})
            .setStyle(
               Style.EMPTY
                  .withBold(true)
                  .withColor(ChatFormatting.GREEN)
                  .withHoverEvent(new ShowText(Component.translatable("commands.test.coordinates.copy")))
                  .withClickEvent(new CopyToClipboard("final BlockPos " + $$1 + " = new BlockPos(" + $$11 + ");"))
            );
         $$0.sendSuccess(() -> Component.translatable("commands.test.relative_position", new Object[]{$$12, $$13}), false);
         $$2.connection.send(new ClientboundGameTestHighlightPosPacket($$4, $$10));
         return 1;
      } else {
         throw TEST_INSTANCE_COULD_NOT_BE_FOUND.create();
      }
   }

   private static int stopTests() {
      GameTestTicker.SINGLETON.clear();
      return 1;
   }

   public static int trackAndStartRunner(CommandSourceStack $$0, GameTestRunner $$1) {
      $$1.addListener(new TestCommand.TestBatchSummaryDisplayer($$0));
      MultipleTestTracker $$2 = new MultipleTestTracker($$1.getTestInfos());
      $$2.addListener(new TestCommand.TestSummaryDisplayer($$0, $$2));
      $$2.addFailureListener($$0x -> FailedTestTracker.rememberFailedTest($$0x.getTestHolder()));
      $$1.start();
      return 1;
   }

   private static int exportTestStructure(CommandSourceStack $$0, Holder<GameTestInstance> $$1) {
      return !TestInstanceBlockEntity.export($$0.getLevel(), ((GameTestInstance)$$1.value()).structure(), $$0::sendSystemMessage) ? 0 : 1;
   }

   private static boolean verifyStructureExists(CommandSourceStack $$0, Identifier $$1) {
      if ($$0.getLevel().getStructureManager().get($$1).isEmpty()) {
         $$0.sendFailure(Component.translatable("commands.test.error.structure_not_found", new Object[]{Component.translationArg($$1)}));
         return false;
      } else {
         return true;
      }
   }

   private static BlockPos createTestPositionAround(CommandSourceStack $$0) {
      BlockPos $$1 = BlockPos.containing($$0.getPosition());
      int $$2 = $$0.getLevel().getHeightmapPos(Types.WORLD_SURFACE, $$1).getY();
      return new BlockPos($$1.getX(), $$2, $$1.getZ() + 3);
   }

   record TestBatchSummaryDisplayer(CommandSourceStack source) implements GameTestBatchListener {
      @Override
      public void testBatchStarting(GameTestBatch $$0) {
         this.source
            .sendSuccess(() -> Component.translatable("commands.test.batch.starting", new Object[]{$$0.environment().getRegisteredName(), $$0.index()}), true);
      }

      @Override
      public void testBatchFinished(GameTestBatch $$0) {
      }
   }

   public record TestSummaryDisplayer(CommandSourceStack source, MultipleTestTracker tracker) implements GameTestListener {
      @Override
      public void testStructureLoaded(GameTestInfo $$0) {
      }

      @Override
      public void testPassed(GameTestInfo $$0, GameTestRunner $$1) {
         this.showTestSummaryIfAllDone();
      }

      @Override
      public void testFailed(GameTestInfo $$0, GameTestRunner $$1) {
         this.showTestSummaryIfAllDone();
      }

      @Override
      public void testAddedForRerun(GameTestInfo $$0, GameTestInfo $$1, GameTestRunner $$2) {
         this.tracker.addTestToTrack($$1);
      }

      private void showTestSummaryIfAllDone() {
         if (this.tracker.isDone()) {
            this.source
               .sendSuccess(
                  () -> Component.translatable("commands.test.summary", new Object[]{this.tracker.getTotalCount()}).withStyle(ChatFormatting.WHITE), true
               );
            if (this.tracker.hasFailedRequired()) {
               this.source.sendFailure(Component.translatable("commands.test.summary.failed", new Object[]{this.tracker.getFailedRequiredCount()}));
            } else {
               this.source.sendSuccess(() -> Component.translatable("commands.test.summary.all_required_passed").withStyle(ChatFormatting.GREEN), true);
            }

            if (this.tracker.hasFailedOptional()) {
               this.source
                  .sendSystemMessage(Component.translatable("commands.test.summary.optional_failed", new Object[]{this.tracker.getFailedOptionalCount()}));
            }
         }
      }
   }
}
