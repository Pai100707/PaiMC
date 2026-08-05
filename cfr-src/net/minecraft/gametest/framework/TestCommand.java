/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
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
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FailedTestTracker;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchFactory;
import net.minecraft.gametest.framework.GameTestBatchListener;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.StructureGridSpawner;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFinder;
import net.minecraft.gametest.framework.TestInstanceFinder;
import net.minecraft.gametest.framework.TestPosFinder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundGameTestHighlightPosPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.InCommandFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
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
    private static final SimpleCommandExceptionType CLEAR_NO_TESTS = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.clear.error.no_tests"));
    private static final SimpleCommandExceptionType RESET_NO_TESTS = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.reset.error.no_tests"));
    private static final SimpleCommandExceptionType TEST_INSTANCE_COULD_NOT_BE_FOUND = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.error.test_instance_not_found"));
    private static final SimpleCommandExceptionType NO_STRUCTURES_TO_EXPORT = new SimpleCommandExceptionType((Message)Component.literal("Could not find any structures to export"));
    private static final SimpleCommandExceptionType NO_TEST_INSTANCES = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.error.no_test_instances"));
    private static final Dynamic3CommandExceptionType NO_TEST_CONTAINING = new Dynamic3CommandExceptionType(($$0, $$1, $$2) -> Component.translatableEscape("commands.test.error.no_test_containing_pos", $$0, $$1, $$2));
    private static final DynamicCommandExceptionType TOO_LARGE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.test.error.too_large", $$0));

    private static int reset(TestFinder $$0) throws CommandSyntaxException {
        TestCommand.stopTests();
        int $$12 = TestCommand.toGameTestInfos($$0.source(), RetryOptions.noRetries(), $$0).map($$1 -> TestCommand.resetGameTestInfo($$0.source(), $$1)).toList().size();
        if ($$12 == 0) {
            throw CLEAR_NO_TESTS.create();
        }
        $$0.source().sendSuccess(() -> Component.translatable("commands.test.reset.success", $$12), true);
        return $$12;
    }

    private static int clear(TestFinder $$0) throws CommandSyntaxException {
        TestCommand.stopTests();
        CommandSourceStack $$12 = $$0.source();
        ServerLevel $$2 = $$12.getLevel();
        List $$3 = $$0.findTestPos().flatMap($$1 -> $$2.getBlockEntity((BlockPos)$$1, BlockEntityType.TEST_INSTANCE_BLOCK).stream()).toList();
        for (TestInstanceBlockEntity $$4 : $$3) {
            StructureUtils.clearSpaceForStructure($$4.getStructureBoundingBox(), $$2);
            $$4.removeBarriers();
            $$2.destroyBlock($$4.getBlockPos(), false);
        }
        if ($$3.isEmpty()) {
            throw CLEAR_NO_TESTS.create();
        }
        $$12.sendSuccess(() -> Component.translatable("commands.test.clear.success", $$3.size()), true);
        return $$3.size();
    }

    private static int export(TestFinder $$0) throws CommandSyntaxException {
        CommandSourceStack $$1 = $$0.source();
        ServerLevel $$2 = $$1.getLevel();
        int $$3 = 0;
        boolean $$4 = true;
        Iterator $$5 = $$0.findTestPos().iterator();
        while ($$5.hasNext()) {
            BlockPos $$6 = (BlockPos)$$5.next();
            BlockEntity blockEntity = $$2.getBlockEntity($$6);
            if (blockEntity instanceof TestInstanceBlockEntity) {
                TestInstanceBlockEntity $$7 = (TestInstanceBlockEntity)blockEntity;
                if (!$$7.exportTest($$1::sendSystemMessage)) {
                    $$4 = false;
                }
                ++$$3;
                continue;
            }
            throw TEST_INSTANCE_COULD_NOT_BE_FOUND.create();
        }
        if ($$3 == 0) {
            throw NO_STRUCTURES_TO_EXPORT.create();
        }
        String $$8 = "Exported " + $$3 + " structures";
        $$0.source().sendSuccess(() -> Component.literal($$8), true);
        return $$4 ? 0 : 1;
    }

    private static int verify(TestFinder $$0) {
        TestCommand.stopTests();
        CommandSourceStack $$1 = $$0.source();
        ServerLevel $$2 = $$1.getLevel();
        BlockPos $$3 = TestCommand.createTestPositionAround($$1);
        List<GameTestInfo> $$4 = Stream.concat(TestCommand.toGameTestInfos($$1, RetryOptions.noRetries(), $$0), TestCommand.toGameTestInfo($$1, RetryOptions.noRetries(), $$0, 0)).toList();
        FailedTestTracker.forgetFailedTests();
        ArrayList<GameTestBatch> $$5 = new ArrayList<GameTestBatch>();
        for (GameTestInfo $$6 : $$4) {
            for (Rotation $$7 : Rotation.values()) {
                ArrayList<GameTestInfo> $$8 = new ArrayList<GameTestInfo>();
                for (int $$9 = 0; $$9 < 100; ++$$9) {
                    GameTestInfo $$10 = new GameTestInfo($$6.getTestHolder(), $$7, $$2, new RetryOptions(1, true));
                    $$10.setTestBlockPos($$6.getTestBlockPos());
                    $$8.add($$10);
                }
                GameTestBatch $$11 = GameTestBatchFactory.toGameTestBatch($$8, $$6.getTest().batch(), $$7.ordinal());
                $$5.add($$11);
            }
        }
        StructureGridSpawner $$12 = new StructureGridSpawner($$3, 10, true);
        GameTestRunner $$13 = GameTestRunner.Builder.fromBatches($$5, $$2).batcher(GameTestBatchFactory.fromGameTestInfo(100)).newStructureSpawner($$12).existingStructureSpawner($$12).haltOnError().clearBetweenBatches().build();
        return TestCommand.trackAndStartRunner($$1, $$13);
    }

    private static int run(TestFinder $$0, RetryOptions $$1, int $$2, int $$3) {
        TestCommand.stopTests();
        CommandSourceStack $$4 = $$0.source();
        ServerLevel $$5 = $$4.getLevel();
        BlockPos $$6 = TestCommand.createTestPositionAround($$4);
        List<GameTestInfo> $$7 = Stream.concat(TestCommand.toGameTestInfos($$4, $$1, $$0), TestCommand.toGameTestInfo($$4, $$1, $$0, $$2)).toList();
        if ($$7.isEmpty()) {
            $$4.sendSuccess(() -> Component.translatable("commands.test.no_tests"), false);
            return 0;
        }
        FailedTestTracker.forgetFailedTests();
        $$4.sendSuccess(() -> Component.translatable("commands.test.run.running", $$7.size()), false);
        GameTestRunner $$8 = GameTestRunner.Builder.fromInfo($$7, $$5).newStructureSpawner(new StructureGridSpawner($$6, $$3, false)).build();
        return TestCommand.trackAndStartRunner($$4, $$8);
    }

    private static int locate(TestFinder $$0) throws CommandSyntaxException {
        $$0.source().sendSystemMessage(Component.translatable("commands.test.locate.started"));
        MutableInt $$1 = new MutableInt(0);
        BlockPos $$2 = BlockPos.containing($$0.source().getPosition());
        $$0.findTestPos().forEach($$3 -> {
            void $$6;
            BlockEntity $$4 = $$0.source().getLevel().getBlockEntity((BlockPos)$$3);
            if (!($$4 instanceof TestInstanceBlockEntity)) {
                return;
            }
            TestInstanceBlockEntity $$5 = (TestInstanceBlockEntity)$$4;
            Direction $$7 = $$6.getRotation().rotate(Direction.NORTH);
            BlockPos $$8 = $$6.getBlockPos().relative($$7, 2);
            int $$9 = (int)$$7.getOpposite().toYRot();
            String $$10 = String.format(Locale.ROOT, "/tp @s %d %d %d %d 0", $$8.getX(), $$8.getY(), $$8.getZ(), $$9);
            int $$11 = $$2.getX() - $$3.getX();
            int $$122 = $$2.getZ() - $$3.getZ();
            int $$13 = Mth.floor(Mth.sqrt($$11 * $$11 + $$122 * $$122));
            MutableComponent $$14 = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", $$3.getX(), $$3.getY(), $$3.getZ())).withStyle($$1 -> $$1.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent.SuggestCommand($$10)).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip"))));
            $$0.source().sendSuccess(() -> Component.translatable("commands.test.locate.found", $$14, $$13), false);
            $$1.increment();
        });
        int $$32 = $$1.intValue();
        if ($$32 == 0) {
            throw NO_TEST_INSTANCES.create();
        }
        $$0.source().sendSuccess(() -> Component.translatable("commands.test.locate.done", $$32), true);
        return $$32;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(ArgumentBuilder<CommandSourceStack, ?> $$0, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> $$12, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> $$2) {
        return $$0.executes($$1 -> TestCommand.run((TestFinder)$$12.apply($$1), RetryOptions.noRetries(), 0, 8)).then(((RequiredArgumentBuilder)Commands.argument("numberOfTimes", IntegerArgumentType.integer((int)0)).executes($$1 -> TestCommand.run((TestFinder)$$12.apply($$1), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)$$1, (String)"numberOfTimes"), false), 0, 8))).then($$2.apply(Commands.argument("untilFailed", BoolArgumentType.bool()).executes($$1 -> TestCommand.run((TestFinder)$$12.apply($$1), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)$$1, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)$$1, (String)"untilFailed")), 0, 8)))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(ArgumentBuilder<CommandSourceStack, ?> $$02, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> $$1) {
        return TestCommand.runWithRetryOptions($$02, $$1, $$0 -> $$0);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptionsAndBuildInfo(ArgumentBuilder<CommandSourceStack, ?> $$0, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> $$1) {
        return TestCommand.runWithRetryOptions($$0, $$1, $$12 -> $$12.then(((RequiredArgumentBuilder)Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes($$1 -> TestCommand.run((TestFinder)$$1.apply($$1), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)$$1, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)$$1, (String)"untilFailed")), IntegerArgumentType.getInteger((CommandContext)$$1, (String)"rotationSteps"), 8))).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes($$1 -> TestCommand.run((TestFinder)$$1.apply($$1), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)$$1, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)$$1, (String)"untilFailed")), IntegerArgumentType.getInteger((CommandContext)$$1, (String)"rotationSteps"), IntegerArgumentType.getInteger((CommandContext)$$1, (String)"testsPerRow"))))));
    }

    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$1) {
        ArgumentBuilder<CommandSourceStack, ?> $$2 = TestCommand.runWithRetryOptionsAndBuildInfo(Commands.argument("onlyRequiredTests", BoolArgumentType.bool()), $$0 -> TestFinder.builder().failedTests((CommandContext<CommandSourceStack>)$$0, BoolArgumentType.getBool((CommandContext)$$0, (String)"onlyRequiredTests")));
        LiteralArgumentBuilder $$3 = (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("test").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("run").then(TestCommand.runWithRetryOptionsAndBuildInfo(Commands.argument("tests", ResourceSelectorArgument.resourceSelector($$1, Registries.TEST_INSTANCE)), $$0 -> TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)$$0, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)$$0, "tests")))))).then(Commands.literal("runmultiple").then(((RequiredArgumentBuilder)Commands.argument("tests", ResourceSelectorArgument.resourceSelector($$1, Registries.TEST_INSTANCE)).executes($$0 -> TestCommand.run(TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)$$0, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)$$0, "tests")), RetryOptions.noRetries(), 0, 8))).then(Commands.argument("amount", IntegerArgumentType.integer()).executes($$0 -> TestCommand.run(TestFinder.builder().createMultipleCopies(IntegerArgumentType.getInteger((CommandContext)$$0, (String)"amount")).byResourceSelection((CommandContext<CommandSourceStack>)$$0, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)$$0, "tests")), RetryOptions.noRetries(), 0, 8)))))).then(TestCommand.runWithRetryOptions(Commands.literal("runthese"), TestFinder.builder()::allNearby))).then(TestCommand.runWithRetryOptions(Commands.literal("runclosest"), TestFinder.builder()::nearest))).then(TestCommand.runWithRetryOptions(Commands.literal("runthat"), TestFinder.builder()::lookedAt))).then(TestCommand.runWithRetryOptionsAndBuildInfo(Commands.literal("runfailed").then($$2), TestFinder.builder()::failedTests))).then(Commands.literal("verify").then(Commands.argument("tests", ResourceSelectorArgument.resourceSelector($$1, Registries.TEST_INSTANCE)).executes($$0 -> TestCommand.verify(TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)$$0, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)$$0, "tests"))))))).then(Commands.literal("locate").then(Commands.argument("tests", ResourceSelectorArgument.resourceSelector($$1, Registries.TEST_INSTANCE)).executes($$0 -> TestCommand.locate(TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)$$0, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)$$0, "tests"))))))).then(Commands.literal("resetclosest").executes($$0 -> TestCommand.reset(TestFinder.builder().nearest((CommandContext<CommandSourceStack>)$$0))))).then(Commands.literal("resetthese").executes($$0 -> TestCommand.reset(TestFinder.builder().allNearby((CommandContext<CommandSourceStack>)$$0))))).then(Commands.literal("resetthat").executes($$0 -> TestCommand.reset(TestFinder.builder().lookedAt((CommandContext<CommandSourceStack>)$$0))))).then(Commands.literal("clearthat").executes($$0 -> TestCommand.clear(TestFinder.builder().lookedAt((CommandContext<CommandSourceStack>)$$0))))).then(Commands.literal("clearthese").executes($$0 -> TestCommand.clear(TestFinder.builder().allNearby((CommandContext<CommandSourceStack>)$$0))))).then(((LiteralArgumentBuilder)Commands.literal("clearall").executes($$0 -> TestCommand.clear(TestFinder.builder().radius((CommandContext<CommandSourceStack>)$$0, 250)))).then(Commands.argument("radius", IntegerArgumentType.integer()).executes($$0 -> TestCommand.clear(TestFinder.builder().radius((CommandContext<CommandSourceStack>)$$0, Mth.clamp(IntegerArgumentType.getInteger((CommandContext)$$0, (String)"radius"), 0, 1024))))))).then(Commands.literal("stop").executes($$0 -> TestCommand.stopTests()))).then(((LiteralArgumentBuilder)Commands.literal("pos").executes($$0 -> TestCommand.showPos((CommandSourceStack)$$0.getSource(), "pos"))).then(Commands.argument("var", StringArgumentType.word()).executes($$0 -> TestCommand.showPos((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"var")))))).then(Commands.literal("create").then(((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id()).suggests(TestCommand::suggestTestFunction).executes($$0 -> TestCommand.createNewStructure((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "id"), 5, 5, 5))).then(((RequiredArgumentBuilder)Commands.argument("width", IntegerArgumentType.integer()).executes($$0 -> TestCommand.createNewStructure((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "id"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"width"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"width"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"width")))).then(Commands.argument("height", IntegerArgumentType.integer()).then(Commands.argument("depth", IntegerArgumentType.integer()).executes($$0 -> TestCommand.createNewStructure((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "id"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"width"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"height"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"depth"))))))));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            $$3 = (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)$$3.then(Commands.literal("export").then(Commands.argument("test", ResourceArgument.resource($$1, Registries.TEST_INSTANCE)).executes($$0 -> TestCommand.exportTestStructure((CommandSourceStack)$$0.getSource(), ResourceArgument.getResource((CommandContext<CommandSourceStack>)$$0, "test", Registries.TEST_INSTANCE)))))).then(Commands.literal("exportclosest").executes($$0 -> TestCommand.export(TestFinder.builder().nearest((CommandContext<CommandSourceStack>)$$0))))).then(Commands.literal("exportthese").executes($$0 -> TestCommand.export(TestFinder.builder().allNearby((CommandContext<CommandSourceStack>)$$0))))).then(Commands.literal("exportthat").executes($$0 -> TestCommand.export(TestFinder.builder().lookedAt((CommandContext<CommandSourceStack>)$$0))));
        }
        $$02.register($$3);
    }

    public static CompletableFuture<Suggestions> suggestTestFunction(CommandContext<CommandSourceStack> $$0, SuggestionsBuilder $$1) {
        Stream<String> $$2 = ((CommandSourceStack)$$0.getSource()).registryAccess().lookupOrThrow(Registries.TEST_FUNCTION).listElements().map(Holder::getRegisteredName);
        return SharedSuggestionProvider.suggest($$2, $$1);
    }

    private static int resetGameTestInfo(CommandSourceStack $$0, GameTestInfo $$1) {
        TestInstanceBlockEntity $$2 = $$1.getTestInstanceBlockEntity();
        $$2.resetTest($$0::sendSystemMessage);
        return 1;
    }

    private static Stream<GameTestInfo> toGameTestInfos(CommandSourceStack $$0, RetryOptions $$1, TestPosFinder $$22) {
        return $$22.findTestPos().map($$2 -> TestCommand.createGameTestInfo($$2, $$0, $$1)).flatMap(Optional::stream);
    }

    private static Stream<GameTestInfo> toGameTestInfo(CommandSourceStack $$0, RetryOptions $$12, TestInstanceFinder $$2, int $$32) {
        return $$2.findTests().filter($$1 -> TestCommand.verifyStructureExists($$0, ((GameTestInstance)$$1.value()).structure())).map($$3 -> new GameTestInfo((Holder.Reference<GameTestInstance>)$$3, StructureUtils.getRotationForRotationSteps($$32), $$0.getLevel(), $$12));
    }

    /*
     * WARNING - void declaration
     */
    private static Optional<GameTestInfo> createGameTestInfo(BlockPos $$0, CommandSourceStack $$1, RetryOptions $$2) {
        void $$5;
        ServerLevel $$3 = $$1.getLevel();
        BlockEntity blockEntity = $$3.getBlockEntity($$0);
        if (!(blockEntity instanceof TestInstanceBlockEntity)) {
            $$1.sendFailure(Component.translatable("commands.test.error.test_instance_not_found.position", $$0.getX(), $$0.getY(), $$0.getZ()));
            return Optional.empty();
        }
        TestInstanceBlockEntity $$4 = (TestInstanceBlockEntity)blockEntity;
        Optional $$6 = $$5.test().flatMap(((Registry)$$1.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE))::get);
        if ($$6.isEmpty()) {
            $$1.sendFailure(Component.translatable("commands.test.error.non_existant_test", $$5.getTestName()));
            return Optional.empty();
        }
        Holder.Reference $$7 = (Holder.Reference)$$6.get();
        GameTestInfo $$8 = new GameTestInfo($$7, $$5.getRotation(), $$3, $$2);
        $$8.setTestBlockPos($$0);
        if (!TestCommand.verifyStructureExists($$1, $$8.getStructure())) {
            return Optional.empty();
        }
        return Optional.of($$8);
    }

    private static int createNewStructure(CommandSourceStack $$0, Identifier $$12, int $$2, int $$3, int $$4) throws CommandSyntaxException {
        if ($$2 > 48 || $$3 > 48 || $$4 > 48) {
            throw TOO_LARGE.create((Object)48);
        }
        ServerLevel $$5 = $$0.getLevel();
        BlockPos $$6 = TestCommand.createTestPositionAround($$0);
        TestInstanceBlockEntity $$7 = StructureUtils.createNewEmptyTest($$12, $$6, new Vec3i($$2, $$3, $$4), Rotation.NONE, $$5);
        BlockPos $$8 = $$7.getStructurePos();
        BlockPos $$9 = $$8.offset($$2 - 1, 0, $$4 - 1);
        BlockPos.betweenClosedStream($$8, $$9).forEach($$1 -> $$5.setBlockAndUpdate((BlockPos)$$1, Blocks.BEDROCK.defaultBlockState()));
        $$0.sendSuccess(() -> Component.translatable("commands.test.create.success", $$7.getTestName()), true);
        return 1;
    }

    /*
     * WARNING - void declaration
     */
    private static int showPos(CommandSourceStack $$0, String $$1) throws CommandSyntaxException {
        void $$8;
        ServerLevel $$5;
        ServerPlayer $$2 = $$0.getPlayerOrException();
        BlockHitResult $$3 = (BlockHitResult)$$2.pick(10.0, 1.0f, false);
        BlockPos $$4 = $$3.getBlockPos();
        Optional<BlockPos> $$6 = StructureUtils.findTestContainingPos($$4, 15, $$5 = $$0.getLevel());
        if ($$6.isEmpty()) {
            $$6 = StructureUtils.findTestContainingPos($$4, 250, $$5);
        }
        if ($$6.isEmpty()) {
            throw NO_TEST_CONTAINING.create((Object)$$4.getX(), (Object)$$4.getY(), (Object)$$4.getZ());
        }
        BlockEntity blockEntity = $$5.getBlockEntity($$6.get());
        if (!(blockEntity instanceof TestInstanceBlockEntity)) {
            throw TEST_INSTANCE_COULD_NOT_BE_FOUND.create();
        }
        TestInstanceBlockEntity $$7 = (TestInstanceBlockEntity)blockEntity;
        BlockPos $$9 = $$8.getStructurePos();
        BlockPos $$10 = $$4.subtract($$9);
        String $$11 = $$10.getX() + ", " + $$10.getY() + ", " + $$10.getZ();
        String $$12 = $$8.getTestName().getString();
        MutableComponent $$13 = Component.translatable("commands.test.coordinates", $$10.getX(), $$10.getY(), $$10.getZ()).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withHoverEvent(new HoverEvent.ShowText(Component.translatable("commands.test.coordinates.copy"))).withClickEvent(new ClickEvent.CopyToClipboard("final BlockPos " + $$1 + " = new BlockPos(" + $$11 + ");")));
        $$0.sendSuccess(() -> Component.translatable("commands.test.relative_position", $$12, $$13), false);
        $$2.connection.send(new ClientboundGameTestHighlightPosPacket($$4, $$10));
        return 1;
    }

    private static int stopTests() {
        GameTestTicker.SINGLETON.clear();
        return 1;
    }

    public static int trackAndStartRunner(CommandSourceStack $$02, GameTestRunner $$1) {
        $$1.addListener(new TestBatchSummaryDisplayer($$02));
        MultipleTestTracker $$2 = new MultipleTestTracker($$1.getTestInfos());
        $$2.addListener(new TestSummaryDisplayer($$02, $$2));
        $$2.addFailureListener($$0 -> FailedTestTracker.rememberFailedTest($$0.getTestHolder()));
        $$1.start();
        return 1;
    }

    private static int exportTestStructure(CommandSourceStack $$0, Holder<GameTestInstance> $$1) {
        if (!TestInstanceBlockEntity.export($$0.getLevel(), $$1.value().structure(), $$0::sendSystemMessage)) {
            return 0;
        }
        return 1;
    }

    private static boolean verifyStructureExists(CommandSourceStack $$0, Identifier $$1) {
        if ($$0.getLevel().getStructureManager().get($$1).isEmpty()) {
            $$0.sendFailure(Component.translatable("commands.test.error.structure_not_found", Component.translationArg($$1)));
            return false;
        }
        return true;
    }

    private static BlockPos createTestPositionAround(CommandSourceStack $$0) {
        BlockPos $$1 = BlockPos.containing($$0.getPosition());
        int $$2 = $$0.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, $$1).getY();
        return new BlockPos($$1.getX(), $$2, $$1.getZ() + 3);
    }

    record TestBatchSummaryDisplayer(CommandSourceStack source) implements GameTestBatchListener
    {
        @Override
        public void testBatchStarting(GameTestBatch $$0) {
            this.source.sendSuccess(() -> Component.translatable("commands.test.batch.starting", $$0.environment().getRegisteredName(), $$0.index()), true);
        }

        @Override
        public void testBatchFinished(GameTestBatch $$0) {
        }
    }

    public record TestSummaryDisplayer(CommandSourceStack source, MultipleTestTracker tracker) implements GameTestListener
    {
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
                this.source.sendSuccess(() -> Component.translatable("commands.test.summary", this.tracker.getTotalCount()).withStyle(ChatFormatting.WHITE), true);
                if (this.tracker.hasFailedRequired()) {
                    this.source.sendFailure(Component.translatable("commands.test.summary.failed", this.tracker.getFailedRequiredCount()));
                } else {
                    this.source.sendSuccess(() -> Component.translatable("commands.test.summary.all_required_passed").withStyle(ChatFormatting.GREEN), true);
                }
                if (this.tracker.hasFailedOptional()) {
                    this.source.sendSystemMessage(Component.translatable("commands.test.summary.optional_failed", this.tracker.getFailedOptionalCount()));
                }
            }
        }
    }
}

