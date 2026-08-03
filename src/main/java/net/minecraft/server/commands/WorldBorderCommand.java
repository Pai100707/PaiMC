package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

public class WorldBorderCommand {
   private static final SimpleCommandExceptionType ERROR_SAME_CENTER = new SimpleCommandExceptionType(
      Component.translatable("commands.worldborder.center.failed")
   );
   private static final SimpleCommandExceptionType ERROR_SAME_SIZE = new SimpleCommandExceptionType(
      Component.translatable("commands.worldborder.set.failed.nochange")
   );
   private static final SimpleCommandExceptionType ERROR_TOO_SMALL = new SimpleCommandExceptionType(
      Component.translatable("commands.worldborder.set.failed.small")
   );
   private static final SimpleCommandExceptionType ERROR_TOO_BIG = new SimpleCommandExceptionType(
      Component.translatable("commands.worldborder.set.failed.big", new Object[]{5.999997E7F})
   );
   private static final SimpleCommandExceptionType ERROR_TOO_FAR_OUT = new SimpleCommandExceptionType(
      Component.translatable("commands.worldborder.set.failed.far", new Object[]{2.9999984E7})
   );
   private static final SimpleCommandExceptionType ERROR_SAME_WARNING_TIME = new SimpleCommandExceptionType(
      Component.translatable("commands.worldborder.warning.time.failed")
   );
   private static final SimpleCommandExceptionType ERROR_SAME_WARNING_DISTANCE = new SimpleCommandExceptionType(
      Component.translatable("commands.worldborder.warning.distance.failed")
   );
   private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_BUFFER = new SimpleCommandExceptionType(
      Component.translatable("commands.worldborder.damage.buffer.failed")
   );
   private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_AMOUNT = new SimpleCommandExceptionType(
      Component.translatable("commands.worldborder.damage.amount.failed")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "worldborder"
                              )
                              .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                           .then(
                              Commands.literal("add")
                                 .then(
                                    ((RequiredArgumentBuilder)Commands.argument("distance", DoubleArgumentType.doubleArg(-5.999997E7F, 5.999997E7F))
                                          .executes(
                                             $$0x -> setSize(
                                                (CommandSourceStack)$$0x.getSource(),
                                                ((CommandSourceStack)$$0x.getSource()).getLevel().getWorldBorder().getSize()
                                                   + DoubleArgumentType.getDouble($$0x, "distance"),
                                                0L
                                             )
                                          ))
                                       .then(
                                          Commands.argument("time", TimeArgument.time(0))
                                             .executes(
                                                $$0x -> setSize(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   ((CommandSourceStack)$$0x.getSource()).getLevel().getWorldBorder().getSize()
                                                      + DoubleArgumentType.getDouble($$0x, "distance"),
                                                   ((CommandSourceStack)$$0x.getSource()).getLevel().getWorldBorder().getLerpTime()
                                                      + IntegerArgumentType.getInteger($$0x, "time")
                                                )
                                             )
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("set")
                              .then(
                                 ((RequiredArgumentBuilder)Commands.argument("distance", DoubleArgumentType.doubleArg(-5.999997E7F, 5.999997E7F))
                                       .executes($$0x -> setSize((CommandSourceStack)$$0x.getSource(), DoubleArgumentType.getDouble($$0x, "distance"), 0L)))
                                    .then(
                                       Commands.argument("time", TimeArgument.time(0))
                                          .executes(
                                             $$0x -> setSize(
                                                (CommandSourceStack)$$0x.getSource(),
                                                DoubleArgumentType.getDouble($$0x, "distance"),
                                                IntegerArgumentType.getInteger($$0x, "time")
                                             )
                                          )
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("center")
                           .then(
                              Commands.argument("pos", Vec2Argument.vec2())
                                 .executes($$0x -> setCenter((CommandSourceStack)$$0x.getSource(), Vec2Argument.getVec2($$0x, "pos")))
                           )
                     ))
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("damage")
                           .then(
                              Commands.literal("amount")
                                 .then(
                                    Commands.argument("damagePerBlock", FloatArgumentType.floatArg(0.0F))
                                       .executes(
                                          $$0x -> setDamageAmount((CommandSourceStack)$$0x.getSource(), FloatArgumentType.getFloat($$0x, "damagePerBlock"))
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("buffer")
                              .then(
                                 Commands.argument("distance", FloatArgumentType.floatArg(0.0F))
                                    .executes($$0x -> setDamageBuffer((CommandSourceStack)$$0x.getSource(), FloatArgumentType.getFloat($$0x, "distance")))
                              )
                        )
                  ))
               .then(Commands.literal("get").executes($$0x -> getSize((CommandSourceStack)$$0x.getSource()))))
            .then(
               ((LiteralArgumentBuilder)Commands.literal("warning")
                     .then(
                        Commands.literal("distance")
                           .then(
                              Commands.argument("distance", IntegerArgumentType.integer(0))
                                 .executes($$0x -> setWarningDistance((CommandSourceStack)$$0x.getSource(), IntegerArgumentType.getInteger($$0x, "distance")))
                           )
                     ))
                  .then(
                     Commands.literal("time")
                        .then(
                           Commands.argument("time", TimeArgument.time(0))
                              .executes($$0x -> setWarningTime((CommandSourceStack)$$0x.getSource(), IntegerArgumentType.getInteger($$0x, "time")))
                        )
                  )
            )
      );
   }

   private static int setDamageBuffer(CommandSourceStack $$0, float $$1) throws CommandSyntaxException {
      WorldBorder $$2 = $$0.getLevel().getWorldBorder();
      if ($$2.getSafeZone() == $$1) {
         throw ERROR_SAME_DAMAGE_BUFFER.create();
      } else {
         $$2.setSafeZone($$1);
         $$0.sendSuccess(
            () -> Component.translatable("commands.worldborder.damage.buffer.success", new Object[]{String.format(Locale.ROOT, "%.2f", $$1)}), true
         );
         return (int)$$1;
      }
   }

   private static int setDamageAmount(CommandSourceStack $$0, float $$1) throws CommandSyntaxException {
      WorldBorder $$2 = $$0.getLevel().getWorldBorder();
      if ($$2.getDamagePerBlock() == $$1) {
         throw ERROR_SAME_DAMAGE_AMOUNT.create();
      } else {
         $$2.setDamagePerBlock($$1);
         $$0.sendSuccess(
            () -> Component.translatable("commands.worldborder.damage.amount.success", new Object[]{String.format(Locale.ROOT, "%.2f", $$1)}), true
         );
         return (int)$$1;
      }
   }

   private static int setWarningTime(CommandSourceStack $$0, int $$1) throws CommandSyntaxException {
      WorldBorder $$2 = $$0.getLevel().getWorldBorder();
      if ($$2.getWarningTime() == $$1) {
         throw ERROR_SAME_WARNING_TIME.create();
      } else {
         $$2.setWarningTime($$1);
         $$0.sendSuccess(() -> Component.translatable("commands.worldborder.warning.time.success", new Object[]{formatTicksToSeconds($$1)}), true);
         return $$1;
      }
   }

   private static int setWarningDistance(CommandSourceStack $$0, int $$1) throws CommandSyntaxException {
      WorldBorder $$2 = $$0.getLevel().getWorldBorder();
      if ($$2.getWarningBlocks() == $$1) {
         throw ERROR_SAME_WARNING_DISTANCE.create();
      } else {
         $$2.setWarningBlocks($$1);
         $$0.sendSuccess(() -> Component.translatable("commands.worldborder.warning.distance.success", new Object[]{$$1}), true);
         return $$1;
      }
   }

   private static int getSize(CommandSourceStack $$0) {
      double $$1 = $$0.getLevel().getWorldBorder().getSize();
      $$0.sendSuccess(() -> Component.translatable("commands.worldborder.get", new Object[]{String.format(Locale.ROOT, "%.0f", $$1)}), false);
      return Mth.floor($$1 + 0.5);
   }

   private static int setCenter(CommandSourceStack $$0, Vec2 $$1) throws CommandSyntaxException {
      WorldBorder $$2 = $$0.getLevel().getWorldBorder();
      if ($$2.getCenterX() == $$1.x && $$2.getCenterZ() == $$1.y) {
         throw ERROR_SAME_CENTER.create();
      } else if (!(Math.abs($$1.x) > 2.9999984E7) && !(Math.abs($$1.y) > 2.9999984E7)) {
         $$2.setCenter($$1.x, $$1.y);
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.worldborder.center.success", new Object[]{String.format(Locale.ROOT, "%.2f", $$1.x), String.format(Locale.ROOT, "%.2f", $$1.y)}
            ),
            true
         );
         return 0;
      } else {
         throw ERROR_TOO_FAR_OUT.create();
      }
   }

   private static int setSize(CommandSourceStack $$0, double $$1, long $$2) throws CommandSyntaxException {
      ServerLevel $$3 = $$0.getLevel();
      WorldBorder $$4 = $$3.getWorldBorder();
      double $$5 = $$4.getSize();
      if ($$5 == $$1) {
         throw ERROR_SAME_SIZE.create();
      } else if ($$1 < 1.0) {
         throw ERROR_TOO_SMALL.create();
      } else if ($$1 > 5.999997E7F) {
         throw ERROR_TOO_BIG.create();
      } else {
         String $$6 = String.format(Locale.ROOT, "%.1f", $$1);
         if ($$2 > 0L) {
            $$4.lerpSizeBetween($$5, $$1, $$2, $$3.getGameTime());
            if ($$1 > $$5) {
               $$0.sendSuccess(() -> Component.translatable("commands.worldborder.set.grow", new Object[]{$$6, formatTicksToSeconds($$2)}), true);
            } else {
               $$0.sendSuccess(() -> Component.translatable("commands.worldborder.set.shrink", new Object[]{$$6, formatTicksToSeconds($$2)}), true);
            }
         } else {
            $$4.setSize($$1);
            $$0.sendSuccess(() -> Component.translatable("commands.worldborder.set.immediate", new Object[]{$$6}), true);
         }

         return (int)($$1 - $$5);
      }
   }

   private static String formatTicksToSeconds(long $$0) {
      return String.format(Locale.ROOT, "%.2f", $$0 / 20.0);
   }
}
