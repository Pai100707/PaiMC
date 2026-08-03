package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.util.TimeUtil;

public class TickCommand {
   private static final float MAX_TICKRATE = 10000.0F;
   private static final String DEFAULT_TICKRATE = String.valueOf(20);

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "tick"
                              )
                              .requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
                           .then(Commands.literal("query").executes($$0x -> tickQuery((CommandSourceStack)$$0x.getSource()))))
                        .then(
                           Commands.literal("rate")
                              .then(
                                 Commands.argument("rate", FloatArgumentType.floatArg(1.0F, 10000.0F))
                                    .suggests(($$0x, $$1) -> SharedSuggestionProvider.suggest(new String[]{DEFAULT_TICKRATE}, $$1))
                                    .executes($$0x -> setTickingRate((CommandSourceStack)$$0x.getSource(), FloatArgumentType.getFloat($$0x, "rate")))
                              )
                        ))
                     .then(
                        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("step")
                                 .executes($$0x -> step((CommandSourceStack)$$0x.getSource(), 1)))
                              .then(Commands.literal("stop").executes($$0x -> stopStepping((CommandSourceStack)$$0x.getSource()))))
                           .then(
                              Commands.argument("time", TimeArgument.time(1))
                                 .suggests(($$0x, $$1) -> SharedSuggestionProvider.suggest(new String[]{"1t", "1s"}, $$1))
                                 .executes($$0x -> step((CommandSourceStack)$$0x.getSource(), IntegerArgumentType.getInteger($$0x, "time")))
                           )
                     ))
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("sprint")
                           .then(Commands.literal("stop").executes($$0x -> stopSprinting((CommandSourceStack)$$0x.getSource()))))
                        .then(
                           Commands.argument("time", TimeArgument.time(1))
                              .suggests(($$0x, $$1) -> SharedSuggestionProvider.suggest(new String[]{"60s", "1d", "3d"}, $$1))
                              .executes($$0x -> sprint((CommandSourceStack)$$0x.getSource(), IntegerArgumentType.getInteger($$0x, "time")))
                        )
                  ))
               .then(Commands.literal("unfreeze").executes($$0x -> setFreeze((CommandSourceStack)$$0x.getSource(), false))))
            .then(Commands.literal("freeze").executes($$0x -> setFreeze((CommandSourceStack)$$0x.getSource(), true)))
      );
   }

   private static String nanosToMilisString(long $$0) {
      return String.format(Locale.ROOT, "%.1f", (float)$$0 / (float)TimeUtil.NANOSECONDS_PER_MILLISECOND);
   }

   private static int setTickingRate(CommandSourceStack $$0, float $$1) {
      net.minecraft.server.ServerTickRateManager $$2 = $$0.getServer().tickRateManager();
      $$2.setTickRate($$1);
      String $$3 = String.format(Locale.ROOT, "%.1f", $$1);
      $$0.sendSuccess(() -> Component.translatable("commands.tick.rate.success", new Object[]{$$3}), true);
      return (int)$$1;
   }

   private static int tickQuery(CommandSourceStack $$0) {
      net.minecraft.server.ServerTickRateManager $$1 = $$0.getServer().tickRateManager();
      String $$2 = nanosToMilisString($$0.getServer().getAverageTickTimeNanos());
      float $$3 = $$1.tickrate();
      String $$4 = String.format(Locale.ROOT, "%.1f", $$3);
      if ($$1.isSprinting()) {
         $$0.sendSuccess(() -> Component.translatable("commands.tick.status.sprinting"), false);
         $$0.sendSuccess(() -> Component.translatable("commands.tick.query.rate.sprinting", new Object[]{$$4, $$2}), false);
      } else {
         if ($$1.isFrozen()) {
            $$0.sendSuccess(() -> Component.translatable("commands.tick.status.frozen"), false);
         } else if ($$1.nanosecondsPerTick() < $$0.getServer().getAverageTickTimeNanos()) {
            $$0.sendSuccess(() -> Component.translatable("commands.tick.status.lagging"), false);
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.tick.status.running"), false);
         }

         String $$5 = nanosToMilisString($$1.nanosecondsPerTick());
         $$0.sendSuccess(() -> Component.translatable("commands.tick.query.rate.running", new Object[]{$$4, $$2, $$5}), false);
      }

      long[] $$6 = Arrays.copyOf($$0.getServer().getTickTimesNanos(), $$0.getServer().getTickTimesNanos().length);
      Arrays.sort($$6);
      String $$7 = nanosToMilisString($$6[$$6.length / 2]);
      String $$8 = nanosToMilisString($$6[(int)($$6.length * 0.95)]);
      String $$9 = nanosToMilisString($$6[(int)($$6.length * 0.99)]);
      $$0.sendSuccess(() -> Component.translatable("commands.tick.query.percentiles", new Object[]{$$7, $$8, $$9, $$6.length}), false);
      return (int)$$3;
   }

   private static int sprint(CommandSourceStack $$0, int $$1) {
      boolean $$2 = $$0.getServer().tickRateManager().requestGameToSprint($$1);
      if ($$2) {
         $$0.sendSuccess(() -> Component.translatable("commands.tick.sprint.stop.success"), true);
      }

      $$0.sendSuccess(() -> Component.translatable("commands.tick.status.sprinting"), true);
      return 1;
   }

   private static int setFreeze(CommandSourceStack $$0, boolean $$1) {
      net.minecraft.server.ServerTickRateManager $$2 = $$0.getServer().tickRateManager();
      if ($$1) {
         if ($$2.isSprinting()) {
            $$2.stopSprinting();
         }

         if ($$2.isSteppingForward()) {
            $$2.stopStepping();
         }
      }

      $$2.setFrozen($$1);
      if ($$1) {
         $$0.sendSuccess(() -> Component.translatable("commands.tick.status.frozen"), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.tick.status.running"), true);
      }

      return $$1 ? 1 : 0;
   }

   private static int step(CommandSourceStack $$0, int $$1) {
      net.minecraft.server.ServerTickRateManager $$2 = $$0.getServer().tickRateManager();
      boolean $$3 = $$2.stepGameIfPaused($$1);
      if ($$3) {
         $$0.sendSuccess(() -> Component.translatable("commands.tick.step.success", new Object[]{$$1}), true);
      } else {
         $$0.sendFailure(Component.translatable("commands.tick.step.fail"));
      }

      return 1;
   }

   private static int stopStepping(CommandSourceStack $$0) {
      net.minecraft.server.ServerTickRateManager $$1 = $$0.getServer().tickRateManager();
      boolean $$2 = $$1.stopStepping();
      if ($$2) {
         $$0.sendSuccess(() -> Component.translatable("commands.tick.step.stop.success"), true);
         return 1;
      } else {
         $$0.sendFailure(Component.translatable("commands.tick.step.stop.fail"));
         return 0;
      }
   }

   private static int stopSprinting(CommandSourceStack $$0) {
      net.minecraft.server.ServerTickRateManager $$1 = $$0.getServer().tickRateManager();
      boolean $$2 = $$1.stopSprinting();
      if ($$2) {
         $$0.sendSuccess(() -> Component.translatable("commands.tick.sprint.stop.success"), true);
         return 1;
      } else {
         $$0.sendFailure(Component.translatable("commands.tick.sprint.stop.fail"));
         return 0;
      }
   }
}
