package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.HolderLookup.RegistryLookup.Delegate;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.SuggestCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket.NodeInspector;
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
import net.minecraft.server.permissions.PermissionCheck.AlwaysPass;
import net.minecraft.server.permissions.PermissionCheck.Require;
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
   private static final ThreadLocal<ExecutionContext<net.minecraft.commands.CommandSourceStack>> CURRENT_EXECUTION_CONTEXT = new ThreadLocal<>();
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final PermissionCheck LEVEL_ALL = AlwaysPass.INSTANCE;
   public static final PermissionCheck LEVEL_MODERATORS = new Require(Permissions.COMMANDS_MODERATOR);
   public static final PermissionCheck LEVEL_GAMEMASTERS = new Require(Permissions.COMMANDS_GAMEMASTER);
   public static final PermissionCheck LEVEL_ADMINS = new Require(Permissions.COMMANDS_ADMIN);
   public static final PermissionCheck LEVEL_OWNERS = new Require(Permissions.COMMANDS_OWNER);
   private static final NodeInspector<net.minecraft.commands.CommandSourceStack> COMMAND_NODE_INSPECTOR = new NodeInspector<net.minecraft.commands.CommandSourceStack>() {
      private final net.minecraft.commands.CommandSourceStack noPermissionSource = net.minecraft.commands.Commands.createCompilationContext(
         PermissionSet.NO_PERMISSIONS
      );

      @Nullable
      public Identifier suggestionId(ArgumentCommandNode<net.minecraft.commands.CommandSourceStack, ?> $$0) {
         SuggestionProvider<net.minecraft.commands.CommandSourceStack> $$1 = $$0.getCustomSuggestions();
         return $$1 != null ? SuggestionProviders.getName($$1) : null;
      }

      public boolean isExecutable(CommandNode<net.minecraft.commands.CommandSourceStack> $$0) {
         return $$0.getCommand() != null;
      }

      public boolean isRestricted(CommandNode<net.minecraft.commands.CommandSourceStack> $$0) {
         Predicate<net.minecraft.commands.CommandSourceStack> $$1 = $$0.getRequirement();
         return !$$1.test(this.noPermissionSource);
      }
   };
   private final CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher = new CommandDispatcher();

   public Commands(net.minecraft.commands.Commands.CommandSelection $$0, net.minecraft.commands.CommandBuildContext $$1) {
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
      SeedCommand.register(this.dispatcher, $$0 != net.minecraft.commands.Commands.CommandSelection.INTEGRATED);
      VersionCommand.register(this.dispatcher, $$0 != net.minecraft.commands.Commands.CommandSelection.INTEGRATED);
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

      this.dispatcher.setConsumer(net.minecraft.commands.ExecutionCommandSource.resultConsumer());
   }

   public static <S> ParseResults<S> mapSource(ParseResults<S> $$0, UnaryOperator<S> $$1) {
      CommandContextBuilder<S> $$2 = $$0.getContext();
      CommandContextBuilder<S> $$3 = $$2.withSource($$1.apply((S)$$2.getSource()));
      return new ParseResults($$3, $$0.getReader(), $$0.getExceptions());
   }

   public void performPrefixedCommand(net.minecraft.commands.CommandSourceStack $$0, String $$1) {
      $$1 = trimOptionalPrefix($$1);
      this.performCommand(this.dispatcher.parse($$1, $$0), $$1);
   }

   public static String trimOptionalPrefix(String $$0) {
      return $$0.startsWith("/") ? $$0.substring(1) : $$0;
   }

   public void performCommand(ParseResults<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      net.minecraft.commands.CommandSourceStack $$2 = (net.minecraft.commands.CommandSourceStack)$$0.getContext().getSource();
      Profiler.get().push(() -> "/" + $$1);
      ContextChain<net.minecraft.commands.CommandSourceStack> $$3 = finishParsing($$0, $$1, $$2);

      try {
         if ($$3 != null) {
            executeCommandInContext(
               $$2, $$3x -> ExecutionContext.queueInitialCommandExecution($$3x, $$1, $$3, $$2, net.minecraft.commands.CommandResultCallback.EMPTY)
            );
         }
      } catch (Exception var12) {
         MutableComponent $$5 = Component.literal(var12.getMessage() == null ? var12.getClass().getName() : var12.getMessage());
         if (LOGGER.isDebugEnabled()) {
            LOGGER.error("Command exception: /{}", $$1, var12);
            StackTraceElement[] $$6 = var12.getStackTrace();

            for (int $$7 = 0; $$7 < Math.min($$6.length, 3); $$7++) {
               $$5.append("\n\n")
                  .append($$6[$$7].getMethodName())
                  .append("\n ")
                  .append($$6[$$7].getFileName())
                  .append(":")
                  .append(String.valueOf($$6[$$7].getLineNumber()));
            }
         }

         $$2.sendFailure(Component.translatable("command.failed").withStyle($$1x -> $$1x.withHoverEvent(new ShowText($$5))));
         if (SharedConstants.DEBUG_VERBOSE_COMMAND_ERRORS || SharedConstants.IS_RUNNING_IN_IDE) {
            $$2.sendFailure(Component.literal(Util.describeError(var12)));
            LOGGER.error("'/{}' threw an exception", $$1, var12);
         }
      } finally {
         Profiler.get().pop();
      }
   }

   @Nullable
   private static ContextChain<net.minecraft.commands.CommandSourceStack> finishParsing(
      ParseResults<net.minecraft.commands.CommandSourceStack> $$0, String $$1, net.minecraft.commands.CommandSourceStack $$2
   ) {
      try {
         validateParseResults($$0);
         return (ContextChain<net.minecraft.commands.CommandSourceStack>)ContextChain.tryFlatten($$0.getContext().build($$1))
            .orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext($$0.getReader()));
      } catch (CommandSyntaxException var7) {
         $$2.sendFailure(ComponentUtils.fromMessage(var7.getRawMessage()));
         if (var7.getInput() != null && var7.getCursor() >= 0) {
            int $$4 = Math.min(var7.getInput().length(), var7.getCursor());
            MutableComponent $$5 = Component.empty().withStyle(ChatFormatting.GRAY).withStyle($$1x -> $$1x.withClickEvent(new SuggestCommand("/" + $$1)));
            if ($$4 > 10) {
               $$5.append(CommonComponents.ELLIPSIS);
            }

            $$5.append(var7.getInput().substring(Math.max(0, $$4 - 10), $$4));
            if ($$4 < var7.getInput().length()) {
               Component $$6 = Component.literal(var7.getInput().substring($$4)).withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.UNDERLINE});
               $$5.append($$6);
            }

            $$5.append(Component.translatable("command.context.here").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}));
            $$2.sendFailure($$5);
         }

         return null;
      }
   }

   public static void executeCommandInContext(
      net.minecraft.commands.CommandSourceStack $$0, Consumer<ExecutionContext<net.minecraft.commands.CommandSourceStack>> $$1
   ) {
      ExecutionContext<net.minecraft.commands.CommandSourceStack> $$2 = CURRENT_EXECUTION_CONTEXT.get();
      boolean $$3 = $$2 == null;
      if ($$3) {
         GameRules $$4 = $$0.getLevel().getGameRules();
         int $$5 = Math.max(1, (Integer)$$4.get(GameRules.MAX_COMMAND_SEQUENCE_LENGTH));
         int $$6 = (Integer)$$4.get(GameRules.MAX_COMMAND_FORKS);

         try (ExecutionContext<net.minecraft.commands.CommandSourceStack> $$7 = new ExecutionContext<>($$5, $$6, Profiler.get())) {
            CURRENT_EXECUTION_CONTEXT.set($$7);
            $$1.accept($$7);
            $$7.runCommandQueue();
         } finally {
            CURRENT_EXECUTION_CONTEXT.set(null);
         }
      } else {
         $$1.accept($$2);
      }
   }

   public void sendCommands(ServerPlayer $$0) {
      Map<CommandNode<net.minecraft.commands.CommandSourceStack>, CommandNode<net.minecraft.commands.CommandSourceStack>> $$1 = new HashMap<>();
      RootCommandNode<net.minecraft.commands.CommandSourceStack> $$2 = new RootCommandNode();
      $$1.put(this.dispatcher.getRoot(), $$2);
      fillUsableCommands(this.dispatcher.getRoot(), $$2, $$0.createCommandSourceStack(), $$1);
      $$0.connection.send(new ClientboundCommandsPacket($$2, COMMAND_NODE_INSPECTOR));
   }

   private static <S> void fillUsableCommands(CommandNode<S> $$0, CommandNode<S> $$1, S $$2, Map<CommandNode<S>, CommandNode<S>> $$3) {
      for (CommandNode<S> $$4 : $$0.getChildren()) {
         if ($$4.canUse($$2)) {
            ArgumentBuilder<S, ?> $$5 = $$4.createBuilder();
            if ($$5.getRedirect() != null) {
               $$5.redirect($$3.get($$5.getRedirect()));
            }

            CommandNode<S> $$6 = $$5.build();
            $$3.put($$4, $$6);
            $$1.addChild($$6);
            if (!$$4.getChildren().isEmpty()) {
               fillUsableCommands($$4, $$6, $$2, $$3);
            }
         }
      }
   }

   public static LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> literal(String $$0) {
      return LiteralArgumentBuilder.literal($$0);
   }

   public static <T> RequiredArgumentBuilder<net.minecraft.commands.CommandSourceStack, T> argument(String $$0, ArgumentType<T> $$1) {
      return RequiredArgumentBuilder.argument($$0, $$1);
   }

   public static Predicate<String> createValidator(net.minecraft.commands.Commands.ParseFunction $$0) {
      return $$1 -> {
         try {
            $$0.parse(new StringReader($$1));
            return true;
         } catch (CommandSyntaxException var3) {
            return false;
         }
      };
   }

   public CommandDispatcher<net.minecraft.commands.CommandSourceStack> getDispatcher() {
      return this.dispatcher;
   }

   public static <S> void validateParseResults(ParseResults<S> $$0) throws CommandSyntaxException {
      CommandSyntaxException $$1 = getParseException($$0);
      if ($$1 != null) {
         throw $$1;
      }
   }

   @Nullable
   public static <S> CommandSyntaxException getParseException(ParseResults<S> $$0) {
      if (!$$0.getReader().canRead()) {
         return null;
      } else if ($$0.getExceptions().size() == 1) {
         return (CommandSyntaxException)$$0.getExceptions().values().iterator().next();
      } else {
         return $$0.getContext().getRange().isEmpty()
            ? CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext($$0.getReader())
            : CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext($$0.getReader());
      }
   }

   public static net.minecraft.commands.CommandBuildContext createValidationContext(final Provider $$0) {
      return new net.minecraft.commands.CommandBuildContext() {
         @Override
         public FeatureFlagSet enabledFeatures() {
            return FeatureFlags.REGISTRY.allFlags();
         }

         public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
            return $$0.listRegistryKeys();
         }

         public <T> Optional<RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$0x) {
            return $$0.lookup($$0).map(this::createLookup);
         }

         private <T> Delegate<T> createLookup(final RegistryLookup<T> $$0x) {
            return new Delegate<T>() {
               public RegistryLookup<T> parent() {
                  return $$0;
               }

               public Optional<Named<T>> get(TagKey<T> $$0xx) {
                  return Optional.of(this.getOrThrow($$0));
               }

               public Named<T> getOrThrow(TagKey<T> $$0xx) {
                  Optional<Named<T>> $$1 = this.parent().get($$0);
                  return $$1.orElseGet(() -> HolderSet.emptyNamed(this.parent(), $$0));
               }
            };
         }
      };
   }

   public static void validate() {
      net.minecraft.commands.CommandBuildContext $$0 = createValidationContext(VanillaRegistries.createLookup());
      CommandDispatcher<net.minecraft.commands.CommandSourceStack> $$1 = new net.minecraft.commands.Commands(
            net.minecraft.commands.Commands.CommandSelection.ALL, $$0
         )
         .getDispatcher();
      RootCommandNode<net.minecraft.commands.CommandSourceStack> $$2 = $$1.getRoot();
      $$1.findAmbiguities(
         ($$1x, $$2x, $$3x, $$4x) -> LOGGER.warn(
            "Ambiguity between arguments {} and {} with inputs: {}", new Object[]{$$1.getPath($$2x), $$1.getPath($$3x), $$4x}
         )
      );
      Set<ArgumentType<?>> $$3 = ArgumentUtils.findUsedArgumentTypes($$2);
      Set<ArgumentType<?>> $$4 = $$3.stream().filter($$0x -> !ArgumentTypeInfos.isClassRecognized($$0x.getClass())).collect(Collectors.toSet());
      if (!$$4.isEmpty()) {
         LOGGER.warn("Missing type registration for following arguments:\n {}", $$4.stream().map($$0x -> "\t" + $$0x).collect(Collectors.joining(",\n")));
         throw new IllegalStateException("Unregistered argument types");
      }
   }

   public static <T extends PermissionSetSupplier> PermissionProviderCheck<T> hasPermission(PermissionCheck $$0) {
      return new PermissionProviderCheck($$0);
   }

   public static net.minecraft.commands.CommandSourceStack createCompilationContext(PermissionSet $$0) {
      return new net.minecraft.commands.CommandSourceStack(
         net.minecraft.commands.CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, $$0, "", CommonComponents.EMPTY, null, null
      );
   }

   public static enum CommandSelection {
      ALL(true, true),
      DEDICATED(false, true),
      INTEGRATED(true, false);

      final boolean includeIntegrated;
      final boolean includeDedicated;

      private CommandSelection(final boolean $$0, final boolean $$1) {
         this.includeIntegrated = $$0;
         this.includeDedicated = $$1;
      }
   }

   @FunctionalInterface
   public interface ParseFunction {
      void parse(StringReader var1) throws CommandSyntaxException;
   }
}
