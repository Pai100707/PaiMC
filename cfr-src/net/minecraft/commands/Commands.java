/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.CommandContextBuilder
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.commands.AttributeCommand;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.commands.ChaseCommand;
import net.minecraft.server.commands.ClearInventoryCommands;
import net.minecraft.server.commands.CloneCommands;
import net.minecraft.server.commands.DamageCommand;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.DebugCommand;
import net.minecraft.server.commands.DebugConfigCommand;
import net.minecraft.server.commands.DebugMobSpawningCommand;
import net.minecraft.server.commands.DebugPathCommand;
import net.minecraft.server.commands.DefaultGameModeCommands;
import net.minecraft.server.commands.DialogCommand;
import net.minecraft.server.commands.DifficultyCommand;
import net.minecraft.server.commands.EffectCommands;
import net.minecraft.server.commands.EmoteCommands;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.commands.ExperienceCommand;
import net.minecraft.server.commands.FetchProfileCommand;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.server.commands.ForceLoadCommand;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.commands.HelpCommand;
import net.minecraft.server.commands.ItemCommands;
import net.minecraft.server.commands.JfrCommand;
import net.minecraft.server.commands.KickCommand;
import net.minecraft.server.commands.KillCommand;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.ParticleCommand;
import net.minecraft.server.commands.PerfCommand;
import net.minecraft.server.commands.PlaceCommand;
import net.minecraft.server.commands.PlaySoundCommand;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.server.commands.RaidCommand;
import net.minecraft.server.commands.RandomCommand;
import net.minecraft.server.commands.RecipeCommand;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.commands.ReturnCommand;
import net.minecraft.server.commands.RideCommand;
import net.minecraft.server.commands.RotateCommand;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.SaveOffCommand;
import net.minecraft.server.commands.SaveOnCommand;
import net.minecraft.server.commands.SayCommand;
import net.minecraft.server.commands.ScheduleCommand;
import net.minecraft.server.commands.ScoreboardCommand;
import net.minecraft.server.commands.SeedCommand;
import net.minecraft.server.commands.ServerPackCommand;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.commands.SetPlayerIdleTimeoutCommand;
import net.minecraft.server.commands.SetSpawnCommand;
import net.minecraft.server.commands.SetWorldSpawnCommand;
import net.minecraft.server.commands.SpawnArmorTrimsCommand;
import net.minecraft.server.commands.SpectateCommand;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.server.commands.StopCommand;
import net.minecraft.server.commands.StopSoundCommand;
import net.minecraft.server.commands.StopwatchCommand;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.commands.TagCommand;
import net.minecraft.server.commands.TeamCommand;
import net.minecraft.server.commands.TeamMsgCommand;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.commands.TellRawCommand;
import net.minecraft.server.commands.TickCommand;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.commands.TitleCommand;
import net.minecraft.server.commands.TransferCommand;
import net.minecraft.server.commands.TriggerCommand;
import net.minecraft.server.commands.VersionCommand;
import net.minecraft.server.commands.WardenSpawnTrackerCommand;
import net.minecraft.server.commands.WaypointCommand;
import net.minecraft.server.commands.WeatherCommand;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionProviderCheck;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.PermissionSetSupplier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Commands {
    public static final String COMMAND_PREFIX = "/";
    private static final ThreadLocal<@Nullable ExecutionContext<CommandSourceStack>> CURRENT_EXECUTION_CONTEXT = new ThreadLocal();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final PermissionCheck LEVEL_ALL = PermissionCheck.AlwaysPass.INSTANCE;
    public static final PermissionCheck LEVEL_MODERATORS = new PermissionCheck.Require(Permissions.COMMANDS_MODERATOR);
    public static final PermissionCheck LEVEL_GAMEMASTERS = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);
    public static final PermissionCheck LEVEL_ADMINS = new PermissionCheck.Require(Permissions.COMMANDS_ADMIN);
    public static final PermissionCheck LEVEL_OWNERS = new PermissionCheck.Require(Permissions.COMMANDS_OWNER);
    private static final ClientboundCommandsPacket.NodeInspector<CommandSourceStack> COMMAND_NODE_INSPECTOR = new ClientboundCommandsPacket.NodeInspector<CommandSourceStack>(){
        private final CommandSourceStack noPermissionSource = Commands.createCompilationContext(PermissionSet.NO_PERMISSIONS);

        @Override
        public @Nullable Identifier suggestionId(ArgumentCommandNode<CommandSourceStack, ?> $$0) {
            SuggestionProvider $$1 = $$0.getCustomSuggestions();
            return $$1 != null ? SuggestionProviders.getName($$1) : null;
        }

        @Override
        public boolean isExecutable(CommandNode<CommandSourceStack> $$0) {
            return $$0.getCommand() != null;
        }

        @Override
        public boolean isRestricted(CommandNode<CommandSourceStack> $$0) {
            Predicate $$1 = $$0.getRequirement();
            return !$$1.test(this.noPermissionSource);
        }
    };
    private final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher();

    public Commands(CommandSelection $$0, CommandBuildContext $$1) {
        AdvancementCommands.register(this.dispatcher);
        AttributeCommand.register(this.dispatcher, $$1);
        ExecuteCommand.register(this.dispatcher, $$1);
        BossBarCommands.register(this.dispatcher, $$1);
        ClearInventoryCommands.register(this.dispatcher, $$1);
        CloneCommands.register(this.dispatcher, $$1);
        DamageCommand.register(this.dispatcher, $$1);
        DataCommands.register(this.dispatcher);
        DataPackCommand.register(this.dispatcher, $$1);
        DebugCommand.register(this.dispatcher);
        DefaultGameModeCommands.register(this.dispatcher);
        DialogCommand.register(this.dispatcher, $$1);
        DifficultyCommand.register(this.dispatcher);
        EffectCommands.register(this.dispatcher, $$1);
        EmoteCommands.register(this.dispatcher);
        EnchantCommand.register(this.dispatcher, $$1);
        ExperienceCommand.register(this.dispatcher);
        FillCommand.register(this.dispatcher, $$1);
        FillBiomeCommand.register(this.dispatcher, $$1);
        ForceLoadCommand.register(this.dispatcher);
        FunctionCommand.register(this.dispatcher);
        GameModeCommand.register(this.dispatcher);
        GameRuleCommand.register(this.dispatcher, $$1);
        GiveCommand.register(this.dispatcher, $$1);
        HelpCommand.register(this.dispatcher);
        ItemCommands.register(this.dispatcher, $$1);
        KickCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        ListPlayersCommand.register(this.dispatcher);
        LocateCommand.register(this.dispatcher, $$1);
        LootCommand.register(this.dispatcher, $$1);
        MsgCommand.register(this.dispatcher);
        ParticleCommand.register(this.dispatcher, $$1);
        PlaceCommand.register(this.dispatcher);
        PlaySoundCommand.register(this.dispatcher);
        RandomCommand.register(this.dispatcher);
        ReloadCommand.register(this.dispatcher);
        RecipeCommand.register(this.dispatcher);
        FetchProfileCommand.register(this.dispatcher);
        ReturnCommand.register(this.dispatcher);
        RideCommand.register(this.dispatcher);
        RotateCommand.register(this.dispatcher);
        SayCommand.register(this.dispatcher);
        ScheduleCommand.register(this.dispatcher);
        ScoreboardCommand.register(this.dispatcher, $$1);
        SeedCommand.register(this.dispatcher, $$0 != CommandSelection.INTEGRATED);
        VersionCommand.register(this.dispatcher, $$0 != CommandSelection.INTEGRATED);
        SetBlockCommand.register(this.dispatcher, $$1);
        SetSpawnCommand.register(this.dispatcher);
        SetWorldSpawnCommand.register(this.dispatcher);
        SpectateCommand.register(this.dispatcher);
        SpreadPlayersCommand.register(this.dispatcher);
        StopSoundCommand.register(this.dispatcher);
        StopwatchCommand.register(this.dispatcher);
        SummonCommand.register(this.dispatcher, $$1);
        TagCommand.register(this.dispatcher);
        TeamCommand.register(this.dispatcher, $$1);
        TeamMsgCommand.register(this.dispatcher);
        TeleportCommand.register(this.dispatcher);
        TellRawCommand.register(this.dispatcher, $$1);
        TestCommand.register(this.dispatcher, $$1);
        TickCommand.register(this.dispatcher);
        TimeCommand.register(this.dispatcher);
        TitleCommand.register(this.dispatcher, $$1);
        TriggerCommand.register(this.dispatcher);
        WaypointCommand.register(this.dispatcher, $$1);
        WeatherCommand.register(this.dispatcher);
        WorldBorderCommand.register(this.dispatcher);
        if (JvmProfiler.INSTANCE.isAvailable()) {
            JfrCommand.register(this.dispatcher);
        }
        if (SharedConstants.DEBUG_CHASE_COMMAND) {
            ChaseCommand.register(this.dispatcher);
        }
        if (SharedConstants.DEBUG_DEV_COMMANDS || SharedConstants.IS_RUNNING_IN_IDE) {
            RaidCommand.register(this.dispatcher, $$1);
            DebugPathCommand.register(this.dispatcher);
            DebugMobSpawningCommand.register(this.dispatcher);
            WardenSpawnTrackerCommand.register(this.dispatcher);
            SpawnArmorTrimsCommand.register(this.dispatcher);
            ServerPackCommand.register(this.dispatcher);
            if ($$0.includeDedicated) {
                DebugConfigCommand.register(this.dispatcher, $$1);
            }
        }
        if ($$0.includeDedicated) {
            BanIpCommands.register(this.dispatcher);
            BanListCommands.register(this.dispatcher);
            BanPlayerCommands.register(this.dispatcher);
            DeOpCommands.register(this.dispatcher);
            OpCommand.register(this.dispatcher);
            PardonCommand.register(this.dispatcher);
            PardonIpCommand.register(this.dispatcher);
            PerfCommand.register(this.dispatcher);
            SaveAllCommand.register(this.dispatcher);
            SaveOffCommand.register(this.dispatcher);
            SaveOnCommand.register(this.dispatcher);
            SetPlayerIdleTimeoutCommand.register(this.dispatcher);
            StopCommand.register(this.dispatcher);
            TransferCommand.register(this.dispatcher);
            WhitelistCommand.register(this.dispatcher);
        }
        if ($$0.includeIntegrated) {
            PublishCommand.register(this.dispatcher);
        }
        this.dispatcher.setConsumer(ExecutionCommandSource.resultConsumer());
    }

    public static <S> ParseResults<S> mapSource(ParseResults<S> $$0, UnaryOperator<S> $$1) {
        CommandContextBuilder $$2 = $$0.getContext();
        CommandContextBuilder $$3 = $$2.withSource($$1.apply($$2.getSource()));
        return new ParseResults($$3, $$0.getReader(), $$0.getExceptions());
    }

    public void performPrefixedCommand(CommandSourceStack $$0, String $$1) {
        $$1 = Commands.trimOptionalPrefix($$1);
        this.performCommand((ParseResults<CommandSourceStack>)this.dispatcher.parse($$1, (Object)$$0), $$1);
    }

    public static String trimOptionalPrefix(String $$0) {
        return $$0.startsWith(COMMAND_PREFIX) ? $$0.substring(1) : $$0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void performCommand(ParseResults<CommandSourceStack> $$0, String $$12) {
        CommandSourceStack $$2 = (CommandSourceStack)$$0.getContext().getSource();
        Profiler.get().push(() -> COMMAND_PREFIX + $$12);
        ContextChain<CommandSourceStack> $$32 = Commands.finishParsing($$0, $$12, $$2);
        try {
            if ($$32 != null) {
                Commands.executeCommandInContext($$2, $$3 -> ExecutionContext.queueInitialCommandExecution($$3, $$12, $$32, $$2, CommandResultCallback.EMPTY));
            }
        }
        catch (Exception $$4) {
            MutableComponent $$5 = Component.literal($$4.getMessage() == null ? $$4.getClass().getName() : $$4.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Command exception: /{}", (Object)$$12, (Object)$$4);
                StackTraceElement[] $$6 = $$4.getStackTrace();
                for (int $$7 = 0; $$7 < Math.min($$6.length, 3); ++$$7) {
                    $$5.append("\n\n").append($$6[$$7].getMethodName()).append("\n ").append($$6[$$7].getFileName()).append(":").append(String.valueOf($$6[$$7].getLineNumber()));
                }
            }
            $$2.sendFailure(Component.translatable("command.failed").withStyle($$1 -> $$1.withHoverEvent(new HoverEvent.ShowText($$5))));
            if (SharedConstants.DEBUG_VERBOSE_COMMAND_ERRORS || SharedConstants.IS_RUNNING_IN_IDE) {
                $$2.sendFailure(Component.literal(Util.describeError($$4)));
                LOGGER.error("'/{}' threw an exception", (Object)$$12, (Object)$$4);
            }
        }
        finally {
            Profiler.get().pop();
        }
    }

    private static @Nullable ContextChain<CommandSourceStack> finishParsing(ParseResults<CommandSourceStack> $$0, String $$12, CommandSourceStack $$2) {
        try {
            Commands.validateParseResults($$0);
            return (ContextChain)ContextChain.tryFlatten((CommandContext)$$0.getContext().build($$12)).orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext($$0.getReader()));
        }
        catch (CommandSyntaxException $$3) {
            $$2.sendFailure(ComponentUtils.fromMessage($$3.getRawMessage()));
            if ($$3.getInput() != null && $$3.getCursor() >= 0) {
                int $$4 = Math.min($$3.getInput().length(), $$3.getCursor());
                MutableComponent $$5 = Component.empty().withStyle(ChatFormatting.GRAY).withStyle($$1 -> $$1.withClickEvent(new ClickEvent.SuggestCommand(COMMAND_PREFIX + $$12)));
                if ($$4 > 10) {
                    $$5.append(CommonComponents.ELLIPSIS);
                }
                $$5.append($$3.getInput().substring(Math.max(0, $$4 - 10), $$4));
                if ($$4 < $$3.getInput().length()) {
                    MutableComponent $$6 = Component.literal($$3.getInput().substring($$4)).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
                    $$5.append($$6);
                }
                $$5.append(Component.translatable("command.context.here").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                $$2.sendFailure($$5);
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void executeCommandInContext(CommandSourceStack $$0, Consumer<ExecutionContext<CommandSourceStack>> $$1) {
        block9: {
            boolean $$3;
            ExecutionContext<CommandSourceStack> $$2 = CURRENT_EXECUTION_CONTEXT.get();
            boolean bl = $$3 = $$2 == null;
            if ($$3) {
                GameRules $$4 = $$0.getLevel().getGameRules();
                int $$5 = Math.max(1, $$4.get(GameRules.MAX_COMMAND_SEQUENCE_LENGTH));
                int $$6 = $$4.get(GameRules.MAX_COMMAND_FORKS);
                try (ExecutionContext $$7 = new ExecutionContext($$5, $$6, Profiler.get());){
                    CURRENT_EXECUTION_CONTEXT.set($$7);
                    $$1.accept($$7);
                    $$7.runCommandQueue();
                    break block9;
                }
                finally {
                    CURRENT_EXECUTION_CONTEXT.set(null);
                }
            }
            $$1.accept($$2);
        }
    }

    public void sendCommands(ServerPlayer $$0) {
        HashMap $$1 = new HashMap();
        RootCommandNode $$2 = new RootCommandNode();
        $$1.put((CommandNode)this.dispatcher.getRoot(), (CommandNode)$$2);
        Commands.fillUsableCommands(this.dispatcher.getRoot(), $$2, $$0.createCommandSourceStack(), $$1);
        $$0.connection.send(new ClientboundCommandsPacket($$2, COMMAND_NODE_INSPECTOR));
    }

    private static <S> void fillUsableCommands(CommandNode<S> $$0, CommandNode<S> $$1, S $$2, Map<CommandNode<S>, CommandNode<S>> $$3) {
        for (CommandNode $$4 : $$0.getChildren()) {
            if (!$$4.canUse($$2)) continue;
            ArgumentBuilder $$5 = $$4.createBuilder();
            if ($$5.getRedirect() != null) {
                $$5.redirect($$3.get($$5.getRedirect()));
            }
            CommandNode $$6 = $$5.build();
            $$3.put($$4, $$6);
            $$1.addChild($$6);
            if ($$4.getChildren().isEmpty()) continue;
            Commands.fillUsableCommands($$4, $$6, $$2, $$3);
        }
    }

    public static LiteralArgumentBuilder<CommandSourceStack> literal(String $$0) {
        return LiteralArgumentBuilder.literal((String)$$0);
    }

    public static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String $$0, ArgumentType<T> $$1) {
        return RequiredArgumentBuilder.argument((String)$$0, $$1);
    }

    public static Predicate<String> createValidator(ParseFunction $$0) {
        return $$1 -> {
            try {
                $$0.parse(new StringReader($$1));
                return true;
            }
            catch (CommandSyntaxException $$2) {
                return false;
            }
        };
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return this.dispatcher;
    }

    public static <S> void validateParseResults(ParseResults<S> $$0) throws CommandSyntaxException {
        CommandSyntaxException $$1 = Commands.getParseException($$0);
        if ($$1 != null) {
            throw $$1;
        }
    }

    public static <S> @Nullable CommandSyntaxException getParseException(ParseResults<S> $$0) {
        if (!$$0.getReader().canRead()) {
            return null;
        }
        if ($$0.getExceptions().size() == 1) {
            return (CommandSyntaxException)((Object)$$0.getExceptions().values().iterator().next());
        }
        if ($$0.getContext().getRange().isEmpty()) {
            return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext($$0.getReader());
        }
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext($$0.getReader());
    }

    public static CommandBuildContext createValidationContext(final HolderLookup.Provider $$0) {
        return new CommandBuildContext(){

            @Override
            public FeatureFlagSet enabledFeatures() {
                return FeatureFlags.REGISTRY.allFlags();
            }

            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                return $$0.listRegistryKeys();
            }

            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$02) {
                return $$0.lookup($$02).map(this::createLookup);
            }

            private <T> HolderLookup.RegistryLookup.Delegate<T> createLookup(final HolderLookup.RegistryLookup<T> $$02) {
                return new HolderLookup.RegistryLookup.Delegate<T>(this){

                    @Override
                    public HolderLookup.RegistryLookup<T> parent() {
                        return $$02;
                    }

                    @Override
                    public Optional<HolderSet.Named<T>> get(TagKey<T> $$0) {
                        return Optional.of(this.getOrThrow($$0));
                    }

                    @Override
                    public HolderSet.Named<T> getOrThrow(TagKey<T> $$0) {
                        Optional<HolderSet.Named<HolderSet.Named>> $$1 = this.parent().get($$0);
                        return $$1.orElseGet(() -> HolderSet.emptyNamed(this.parent(), $$0));
                    }
                };
            }
        };
    }

    public static void validate() {
        CommandBuildContext $$02 = Commands.createValidationContext(VanillaRegistries.createLookup());
        CommandDispatcher<CommandSourceStack> $$12 = new Commands(CommandSelection.ALL, $$02).getDispatcher();
        RootCommandNode $$22 = $$12.getRoot();
        $$12.findAmbiguities(($$1, $$2, $$3, $$4) -> LOGGER.warn("Ambiguity between arguments {} and {} with inputs: {}", new Object[]{$$12.getPath($$2), $$12.getPath($$3), $$4}));
        Set<ArgumentType<?>> $$32 = ArgumentUtils.findUsedArgumentTypes($$22);
        Set $$42 = $$32.stream().filter($$0 -> !ArgumentTypeInfos.isClassRecognized($$0.getClass())).collect(Collectors.toSet());
        if (!$$42.isEmpty()) {
            LOGGER.warn("Missing type registration for following arguments:\n {}", (Object)$$42.stream().map($$0 -> "\t" + String.valueOf($$0)).collect(Collectors.joining(",\n")));
            throw new IllegalStateException("Unregistered argument types");
        }
    }

    public static <T extends PermissionSetSupplier> PermissionProviderCheck<T> hasPermission(PermissionCheck $$0) {
        return new PermissionProviderCheck($$0);
    }

    public static CommandSourceStack createCompilationContext(PermissionSet $$0) {
        return new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, $$0, "", CommonComponents.EMPTY, null, null);
    }

    public static enum CommandSelection {
        ALL(true, true),
        DEDICATED(false, true),
        INTEGRATED(true, false);

        final boolean includeIntegrated;
        final boolean includeDedicated;

        private CommandSelection(boolean $$0, boolean $$1) {
            this.includeIntegrated = $$0;
            this.includeDedicated = $$1;
        }
    }

    @FunctionalInterface
    public static interface ParseFunction {
        public void parse(StringReader var1) throws CommandSyntaxException;
    }
}

