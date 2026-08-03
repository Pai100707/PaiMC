package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Stopwatch;
import net.minecraft.world.Stopwatches;

public class StopwatchCommand {
   private static final DynamicCommandExceptionType ERROR_ALREADY_EXISTS = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.stopwatch.already_exists", new Object[]{$$0})
   );
   public static final DynamicCommandExceptionType ERROR_DOES_NOT_EXIST = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.stopwatch.does_not_exist", new Object[]{$$0})
   );
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_STOPWATCHES = ($$0, $$1) -> SharedSuggestionProvider.suggestResource(
      ((CommandSourceStack)$$0.getSource()).getServer().getStopwatches().ids(), $$1
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                           "stopwatch"
                        )
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                     .then(
                        Commands.literal("create")
                           .then(
                              Commands.argument("id", IdentifierArgument.id())
                                 .executes($$0x -> createStopwatch((CommandSourceStack)$$0x.getSource(), IdentifierArgument.getId($$0x, "id")))
                           )
                     ))
                  .then(
                     Commands.literal("query")
                        .then(
                           ((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id())
                                 .suggests(SUGGEST_STOPWATCHES)
                                 .then(
                                    Commands.argument("scale", DoubleArgumentType.doubleArg())
                                       .executes(
                                          $$0x -> queryStopwatch(
                                             (CommandSourceStack)$$0x.getSource(),
                                             IdentifierArgument.getId($$0x, "id"),
                                             DoubleArgumentType.getDouble($$0x, "scale")
                                          )
                                       )
                                 ))
                              .executes($$0x -> queryStopwatch((CommandSourceStack)$$0x.getSource(), IdentifierArgument.getId($$0x, "id"), 1.0))
                        )
                  ))
               .then(
                  Commands.literal("restart")
                     .then(
                        Commands.argument("id", IdentifierArgument.id())
                           .suggests(SUGGEST_STOPWATCHES)
                           .executes($$0x -> restartStopwatch((CommandSourceStack)$$0x.getSource(), IdentifierArgument.getId($$0x, "id")))
                     )
               ))
            .then(
               Commands.literal("remove")
                  .then(
                     Commands.argument("id", IdentifierArgument.id())
                        .suggests(SUGGEST_STOPWATCHES)
                        .executes($$0x -> removeStopwatch((CommandSourceStack)$$0x.getSource(), IdentifierArgument.getId($$0x, "id")))
                  )
            )
      );
   }

   private static int createStopwatch(CommandSourceStack $$0, Identifier $$1) throws CommandSyntaxException {
      net.minecraft.server.MinecraftServer $$2 = $$0.getServer();
      Stopwatches $$3 = $$2.getStopwatches();
      Stopwatch $$4 = new Stopwatch(Stopwatches.currentTime());
      if (!$$3.add($$1, $$4)) {
         throw ERROR_ALREADY_EXISTS.create($$1);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.stopwatch.create.success", new Object[]{Component.translationArg($$1)}), true);
         return 1;
      }
   }

   private static int queryStopwatch(CommandSourceStack $$0, Identifier $$1, double $$2) throws CommandSyntaxException {
      net.minecraft.server.MinecraftServer $$3 = $$0.getServer();
      Stopwatches $$4 = $$3.getStopwatches();
      Stopwatch $$5 = $$4.get($$1);
      if ($$5 == null) {
         throw ERROR_DOES_NOT_EXIST.create($$1);
      } else {
         long $$6 = Stopwatches.currentTime();
         double $$7 = $$5.elapsedSeconds($$6);
         $$0.sendSuccess(() -> Component.translatable("commands.stopwatch.query", new Object[]{Component.translationArg($$1), $$7}), true);
         return (int)($$7 * $$2);
      }
   }

   private static int restartStopwatch(CommandSourceStack $$0, Identifier $$1) throws CommandSyntaxException {
      net.minecraft.server.MinecraftServer $$2 = $$0.getServer();
      Stopwatches $$3 = $$2.getStopwatches();
      if (!$$3.update($$1, $$0x -> new Stopwatch(Stopwatches.currentTime()))) {
         throw ERROR_DOES_NOT_EXIST.create($$1);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.stopwatch.restart.success", new Object[]{Component.translationArg($$1)}), true);
         return 1;
      }
   }

   private static int removeStopwatch(CommandSourceStack $$0, Identifier $$1) throws CommandSyntaxException {
      net.minecraft.server.MinecraftServer $$2 = $$0.getServer();
      Stopwatches $$3 = $$2.getStopwatches();
      if (!$$3.remove($$1)) {
         throw ERROR_DOES_NOT_EXIST.create($$1);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.stopwatch.remove.success", new Object[]{Component.translationArg($$1)}), true);
         return 1;
      }
   }
}
