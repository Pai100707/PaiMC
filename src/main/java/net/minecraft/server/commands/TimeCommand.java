package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class TimeCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("time")
                     .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                  .then(
                     ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("set")
                                    .then(Commands.literal("day").executes($$0x -> setTime((CommandSourceStack)$$0x.getSource(), 1000))))
                                 .then(Commands.literal("noon").executes($$0x -> setTime((CommandSourceStack)$$0x.getSource(), 6000))))
                              .then(Commands.literal("night").executes($$0x -> setTime((CommandSourceStack)$$0x.getSource(), 13000))))
                           .then(Commands.literal("midnight").executes($$0x -> setTime((CommandSourceStack)$$0x.getSource(), 18000))))
                        .then(
                           Commands.argument("time", TimeArgument.time())
                              .executes($$0x -> setTime((CommandSourceStack)$$0x.getSource(), IntegerArgumentType.getInteger($$0x, "time")))
                        )
                  ))
               .then(
                  Commands.literal("add")
                     .then(
                        Commands.argument("time", TimeArgument.time())
                           .executes($$0x -> addTime((CommandSourceStack)$$0x.getSource(), IntegerArgumentType.getInteger($$0x, "time")))
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("query")
                        .then(
                           Commands.literal("daytime")
                              .executes($$0x -> queryTime((CommandSourceStack)$$0x.getSource(), getDayTime(((CommandSourceStack)$$0x.getSource()).getLevel())))
                        ))
                     .then(
                        Commands.literal("gametime")
                           .executes(
                              $$0x -> queryTime(
                                 (CommandSourceStack)$$0x.getSource(), (int)(((CommandSourceStack)$$0x.getSource()).getLevel().getGameTime() % 2147483647L)
                              )
                           )
                     ))
                  .then(
                     Commands.literal("day")
                        .executes(
                           $$0x -> queryTime(
                              (CommandSourceStack)$$0x.getSource(), (int)(((CommandSourceStack)$$0x.getSource()).getLevel().getDayCount() % 2147483647L)
                           )
                        )
                  )
            )
      );
   }

   private static int getDayTime(ServerLevel $$0) {
      return (int)($$0.getDayTime() % 24000L);
   }

   private static int queryTime(CommandSourceStack $$0, int $$1) {
      $$0.sendSuccess(() -> Component.translatable("commands.time.query", new Object[]{$$1}), false);
      return $$1;
   }

   public static int setTime(CommandSourceStack $$0, int $$1) {
      for (ServerLevel $$2 : $$0.getServer().getAllLevels()) {
         $$2.setDayTime($$1);
      }

      $$0.getServer().forceTimeSynchronization();
      $$0.sendSuccess(() -> Component.translatable("commands.time.set", new Object[]{$$1}), true);
      return getDayTime($$0.getLevel());
   }

   public static int addTime(CommandSourceStack $$0, int $$1) {
      for (ServerLevel $$2 : $$0.getServer().getAllLevels()) {
         $$2.setDayTime($$2.getDayTime() + $$1);
      }

      $$0.getServer().forceTimeSynchronization();
      int $$3 = getDayTime($$0.getLevel());
      $$0.sendSuccess(() -> Component.translatable("commands.time.set", new Object[]{$$3}), true);
      return $$3;
   }
}
