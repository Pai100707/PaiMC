package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.MacroFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;
import net.minecraft.world.level.timers.TimerQueue;

public class ScheduleCommand {
   private static final SimpleCommandExceptionType ERROR_SAME_TICK = new SimpleCommandExceptionType(Component.translatable("commands.schedule.same_tick"));
   private static final DynamicCommandExceptionType ERROR_CANT_REMOVE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.schedule.cleared.failure", new Object[]{$$0})
   );
   private static final SimpleCommandExceptionType ERROR_MACRO = new SimpleCommandExceptionType(
      Component.translatableEscape("commands.schedule.macro", new Object[0])
   );
   private static final SuggestionProvider<CommandSourceStack> SUGGEST_SCHEDULE = ($$0, $$1) -> SharedSuggestionProvider.suggest(
      ((CommandSourceStack)$$0.getSource()).getServer().getWorldData().overworldData().getScheduledEvents().getEventsIds(), $$1
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("schedule")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .then(
                  Commands.literal("function")
                     .then(
                        Commands.argument("function", FunctionArgument.functions())
                           .suggests(FunctionCommand.SUGGEST_FUNCTION)
                           .then(
                              ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("time", TimeArgument.time())
                                       .executes(
                                          $$0x -> schedule(
                                             (CommandSourceStack)$$0x.getSource(),
                                             FunctionArgument.getFunctionOrTag($$0x, "function"),
                                             IntegerArgumentType.getInteger($$0x, "time"),
                                             true
                                          )
                                       ))
                                    .then(
                                       Commands.literal("append")
                                          .executes(
                                             $$0x -> schedule(
                                                (CommandSourceStack)$$0x.getSource(),
                                                FunctionArgument.getFunctionOrTag($$0x, "function"),
                                                IntegerArgumentType.getInteger($$0x, "time"),
                                                false
                                             )
                                          )
                                    ))
                                 .then(
                                    Commands.literal("replace")
                                       .executes(
                                          $$0x -> schedule(
                                             (CommandSourceStack)$$0x.getSource(),
                                             FunctionArgument.getFunctionOrTag($$0x, "function"),
                                             IntegerArgumentType.getInteger($$0x, "time"),
                                             true
                                          )
                                       )
                                 )
                           )
                     )
               ))
            .then(
               Commands.literal("clear")
                  .then(
                     Commands.argument("function", StringArgumentType.greedyString())
                        .suggests(SUGGEST_SCHEDULE)
                        .executes($$0x -> remove((CommandSourceStack)$$0x.getSource(), StringArgumentType.getString($$0x, "function")))
                  )
            )
      );
   }

   private static int schedule(
      CommandSourceStack $$0,
      Pair<Identifier, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> $$1,
      int $$2,
      boolean $$3
   ) throws CommandSyntaxException {
      if ($$2 == 0) {
         throw ERROR_SAME_TICK.create();
      } else {
         long $$4 = $$0.getLevel().getGameTime() + $$2;
         Identifier $$5 = (Identifier)$$1.getFirst();
         TimerQueue<net.minecraft.server.MinecraftServer> $$6 = $$0.getServer().getWorldData().overworldData().getScheduledEvents();
         Optional<CommandFunction<CommandSourceStack>> $$7 = ((Either)$$1.getSecond()).left();
         if ($$7.isPresent()) {
            if ($$7.get() instanceof MacroFunction) {
               throw ERROR_MACRO.create();
            }

            String $$8 = $$5.toString();
            if ($$3) {
               $$6.remove($$8);
            }

            $$6.schedule($$8, $$4, new FunctionCallback($$5));
            $$0.sendSuccess(() -> Component.translatable("commands.schedule.created.function", new Object[]{Component.translationArg($$5), $$2, $$4}), true);
         } else {
            String $$9 = "#" + $$5;
            if ($$3) {
               $$6.remove($$9);
            }

            $$6.schedule($$9, $$4, new FunctionTagCallback($$5));
            $$0.sendSuccess(() -> Component.translatable("commands.schedule.created.tag", new Object[]{Component.translationArg($$5), $$2, $$4}), true);
         }

         return Math.floorMod($$4, Integer.MAX_VALUE);
      }
   }

   private static int remove(CommandSourceStack $$0, String $$1) throws CommandSyntaxException {
      int $$2 = $$0.getServer().getWorldData().overworldData().getScheduledEvents().remove($$1);
      if ($$2 == 0) {
         throw ERROR_CANT_REMOVE.create($$1);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.schedule.cleared.success", new Object[]{$$2, $$1}), true);
         return $$2;
      }
   }
}
