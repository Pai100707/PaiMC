package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.entity.player.Player;

public class BossBarCommands {
   private static final DynamicCommandExceptionType ERROR_ALREADY_EXISTS = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.bossbar.create.failed", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_DOESNT_EXIST = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.bossbar.unknown", new Object[]{$$0})
   );
   private static final SimpleCommandExceptionType ERROR_NO_PLAYER_CHANGE = new SimpleCommandExceptionType(
      Component.translatable("commands.bossbar.set.players.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_NO_NAME_CHANGE = new SimpleCommandExceptionType(
      Component.translatable("commands.bossbar.set.name.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_NO_COLOR_CHANGE = new SimpleCommandExceptionType(
      Component.translatable("commands.bossbar.set.color.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_NO_STYLE_CHANGE = new SimpleCommandExceptionType(
      Component.translatable("commands.bossbar.set.style.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_NO_VALUE_CHANGE = new SimpleCommandExceptionType(
      Component.translatable("commands.bossbar.set.value.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_NO_MAX_CHANGE = new SimpleCommandExceptionType(
      Component.translatable("commands.bossbar.set.max.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_ALREADY_HIDDEN = new SimpleCommandExceptionType(
      Component.translatable("commands.bossbar.set.visibility.unchanged.hidden")
   );
   private static final SimpleCommandExceptionType ERROR_ALREADY_VISIBLE = new SimpleCommandExceptionType(
      Component.translatable("commands.bossbar.set.visibility.unchanged.visible")
   );
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_BOSS_BAR = ($$0, $$1) -> SharedSuggestionProvider.suggestResource(
      ((CommandSourceStack)$$0.getSource()).getServer().getCustomBossEvents().getIds(), $$1
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                              "bossbar"
                           )
                           .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                        .then(
                           Commands.literal("add")
                              .then(
                                 Commands.argument("id", IdentifierArgument.id())
                                    .then(
                                       Commands.argument("name", ComponentArgument.textComponent($$1))
                                          .executes(
                                             $$0x -> createBar(
                                                (CommandSourceStack)$$0x.getSource(),
                                                IdentifierArgument.getId($$0x, "id"),
                                                ComponentArgument.getResolvedComponent($$0x, "name")
                                             )
                                          )
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("remove")
                           .then(
                              Commands.argument("id", IdentifierArgument.id())
                                 .suggests(SUGGEST_BOSS_BAR)
                                 .executes($$0x -> removeBar((CommandSourceStack)$$0x.getSource(), getBossBar($$0x)))
                           )
                     ))
                  .then(Commands.literal("list").executes($$0x -> listBars((CommandSourceStack)$$0x.getSource()))))
               .then(
                  Commands.literal("set")
                     .then(
                        ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                                "id", IdentifierArgument.id()
                                             )
                                             .suggests(SUGGEST_BOSS_BAR)
                                             .then(
                                                Commands.literal("name")
                                                   .then(
                                                      Commands.argument("name", ComponentArgument.textComponent($$1))
                                                         .executes(
                                                            $$0x -> setName(
                                                               (CommandSourceStack)$$0x.getSource(),
                                                               getBossBar($$0x),
                                                               ComponentArgument.getResolvedComponent($$0x, "name")
                                                            )
                                                         )
                                                   )
                                             ))
                                          .then(
                                             ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                                     "color"
                                                                  )
                                                                  .then(
                                                                     Commands.literal("pink")
                                                                        .executes(
                                                                           $$0x -> setColor(
                                                                              (CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarColor.PINK
                                                                           )
                                                                        )
                                                                  ))
                                                               .then(
                                                                  Commands.literal("blue")
                                                                     .executes(
                                                                        $$0x -> setColor(
                                                                           (CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarColor.BLUE
                                                                        )
                                                                     )
                                                               ))
                                                            .then(
                                                               Commands.literal("red")
                                                                  .executes(
                                                                     $$0x -> setColor((CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarColor.RED)
                                                                  )
                                                            ))
                                                         .then(
                                                            Commands.literal("green")
                                                               .executes(
                                                                  $$0x -> setColor((CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarColor.GREEN)
                                                               )
                                                         ))
                                                      .then(
                                                         Commands.literal("yellow")
                                                            .executes(
                                                               $$0x -> setColor((CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarColor.YELLOW)
                                                            )
                                                      ))
                                                   .then(
                                                      Commands.literal("purple")
                                                         .executes(
                                                            $$0x -> setColor((CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarColor.PURPLE)
                                                         )
                                                   ))
                                                .then(
                                                   Commands.literal("white")
                                                      .executes($$0x -> setColor((CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarColor.WHITE))
                                                )
                                          ))
                                       .then(
                                          ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                            "style"
                                                         )
                                                         .then(
                                                            Commands.literal("progress")
                                                               .executes(
                                                                  $$0x -> setStyle(
                                                                     (CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarOverlay.PROGRESS
                                                                  )
                                                               )
                                                         ))
                                                      .then(
                                                         Commands.literal("notched_6")
                                                            .executes(
                                                               $$0x -> setStyle(
                                                                  (CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarOverlay.NOTCHED_6
                                                               )
                                                            )
                                                      ))
                                                   .then(
                                                      Commands.literal("notched_10")
                                                         .executes(
                                                            $$0x -> setStyle((CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarOverlay.NOTCHED_10)
                                                         )
                                                   ))
                                                .then(
                                                   Commands.literal("notched_12")
                                                      .executes(
                                                         $$0x -> setStyle((CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarOverlay.NOTCHED_12)
                                                      )
                                                ))
                                             .then(
                                                Commands.literal("notched_20")
                                                   .executes(
                                                      $$0x -> setStyle((CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BossBarOverlay.NOTCHED_20)
                                                   )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("value")
                                          .then(
                                             Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(
                                                   $$0x -> setValue(
                                                      (CommandSourceStack)$$0x.getSource(), getBossBar($$0x), IntegerArgumentType.getInteger($$0x, "value")
                                                   )
                                                )
                                          )
                                    ))
                                 .then(
                                    Commands.literal("max")
                                       .then(
                                          Commands.argument("max", IntegerArgumentType.integer(1))
                                             .executes(
                                                $$0x -> setMax(
                                                   (CommandSourceStack)$$0x.getSource(), getBossBar($$0x), IntegerArgumentType.getInteger($$0x, "max")
                                                )
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("visible")
                                    .then(
                                       Commands.argument("visible", BoolArgumentType.bool())
                                          .executes(
                                             $$0x -> setVisible(
                                                (CommandSourceStack)$$0x.getSource(), getBossBar($$0x), BoolArgumentType.getBool($$0x, "visible")
                                             )
                                          )
                                    )
                              ))
                           .then(
                              ((LiteralArgumentBuilder)Commands.literal("players")
                                    .executes($$0x -> setPlayers((CommandSourceStack)$$0x.getSource(), getBossBar($$0x), Collections.emptyList())))
                                 .then(
                                    Commands.argument("targets", EntityArgument.players())
                                       .executes(
                                          $$0x -> setPlayers(
                                             (CommandSourceStack)$$0x.getSource(), getBossBar($$0x), EntityArgument.getOptionalPlayers($$0x, "targets")
                                          )
                                       )
                                 )
                           )
                     )
               ))
            .then(
               Commands.literal("get")
                  .then(
                     ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id())
                                 .suggests(SUGGEST_BOSS_BAR)
                                 .then(Commands.literal("value").executes($$0x -> getValue((CommandSourceStack)$$0x.getSource(), getBossBar($$0x)))))
                              .then(Commands.literal("max").executes($$0x -> getMax((CommandSourceStack)$$0x.getSource(), getBossBar($$0x)))))
                           .then(Commands.literal("visible").executes($$0x -> getVisible((CommandSourceStack)$$0x.getSource(), getBossBar($$0x)))))
                        .then(Commands.literal("players").executes($$0x -> getPlayers((CommandSourceStack)$$0x.getSource(), getBossBar($$0x))))
                  )
            )
      );
   }

   private static int getValue(CommandSourceStack $$0, CustomBossEvent $$1) {
      $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.value", new Object[]{$$1.getDisplayName(), $$1.getValue()}), true);
      return $$1.getValue();
   }

   private static int getMax(CommandSourceStack $$0, CustomBossEvent $$1) {
      $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.max", new Object[]{$$1.getDisplayName(), $$1.getMax()}), true);
      return $$1.getMax();
   }

   private static int getVisible(CommandSourceStack $$0, CustomBossEvent $$1) {
      if ($$1.isVisible()) {
         $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.visible.visible", new Object[]{$$1.getDisplayName()}), true);
         return 1;
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.visible.hidden", new Object[]{$$1.getDisplayName()}), true);
         return 0;
      }
   }

   private static int getPlayers(CommandSourceStack $$0, CustomBossEvent $$1) {
      if ($$1.getPlayers().isEmpty()) {
         $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.players.none", new Object[]{$$1.getDisplayName()}), true);
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.bossbar.get.players.some",
               new Object[]{$$1.getDisplayName(), $$1.getPlayers().size(), ComponentUtils.formatList($$1.getPlayers(), Player::getDisplayName)}
            ),
            true
         );
      }

      return $$1.getPlayers().size();
   }

   private static int setVisible(CommandSourceStack $$0, CustomBossEvent $$1, boolean $$2) throws CommandSyntaxException {
      if ($$1.isVisible() == $$2) {
         if ($$2) {
            throw ERROR_ALREADY_VISIBLE.create();
         } else {
            throw ERROR_ALREADY_HIDDEN.create();
         }
      } else {
         $$1.setVisible($$2);
         if ($$2) {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.visible.success.visible", new Object[]{$$1.getDisplayName()}), true);
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.visible.success.hidden", new Object[]{$$1.getDisplayName()}), true);
         }

         return 0;
      }
   }

   private static int setValue(CommandSourceStack $$0, CustomBossEvent $$1, int $$2) throws CommandSyntaxException {
      if ($$1.getValue() == $$2) {
         throw ERROR_NO_VALUE_CHANGE.create();
      } else {
         $$1.setValue($$2);
         $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.value.success", new Object[]{$$1.getDisplayName(), $$2}), true);
         return $$2;
      }
   }

   private static int setMax(CommandSourceStack $$0, CustomBossEvent $$1, int $$2) throws CommandSyntaxException {
      if ($$1.getMax() == $$2) {
         throw ERROR_NO_MAX_CHANGE.create();
      } else {
         $$1.setMax($$2);
         $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.max.success", new Object[]{$$1.getDisplayName(), $$2}), true);
         return $$2;
      }
   }

   private static int setColor(CommandSourceStack $$0, CustomBossEvent $$1, BossBarColor $$2) throws CommandSyntaxException {
      if ($$1.getColor().equals($$2)) {
         throw ERROR_NO_COLOR_CHANGE.create();
      } else {
         $$1.setColor($$2);
         $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.color.success", new Object[]{$$1.getDisplayName()}), true);
         return 0;
      }
   }

   private static int setStyle(CommandSourceStack $$0, CustomBossEvent $$1, BossBarOverlay $$2) throws CommandSyntaxException {
      if ($$1.getOverlay().equals($$2)) {
         throw ERROR_NO_STYLE_CHANGE.create();
      } else {
         $$1.setOverlay($$2);
         $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.style.success", new Object[]{$$1.getDisplayName()}), true);
         return 0;
      }
   }

   private static int setName(CommandSourceStack $$0, CustomBossEvent $$1, Component $$2) throws CommandSyntaxException {
      Component $$3 = ComponentUtils.updateForEntity($$0, $$2, null, 0);
      if ($$1.getName().equals($$3)) {
         throw ERROR_NO_NAME_CHANGE.create();
      } else {
         $$1.setName($$3);
         $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.name.success", new Object[]{$$1.getDisplayName()}), true);
         return 0;
      }
   }

   private static int setPlayers(CommandSourceStack $$0, CustomBossEvent $$1, Collection<ServerPlayer> $$2) throws CommandSyntaxException {
      boolean $$3 = $$1.setPlayers($$2);
      if (!$$3) {
         throw ERROR_NO_PLAYER_CHANGE.create();
      } else {
         if ($$1.getPlayers().isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.players.success.none", new Object[]{$$1.getDisplayName()}), true);
         } else {
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.bossbar.set.players.success.some",
                  new Object[]{$$1.getDisplayName(), $$2.size(), ComponentUtils.formatList($$2, Player::getDisplayName)}
               ),
               true
            );
         }

         return $$1.getPlayers().size();
      }
   }

   private static int listBars(CommandSourceStack $$0) {
      Collection<CustomBossEvent> $$1 = $$0.getServer().getCustomBossEvents().getEvents();
      if ($$1.isEmpty()) {
         $$0.sendSuccess(() -> Component.translatable("commands.bossbar.list.bars.none"), false);
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.bossbar.list.bars.some", new Object[]{$$1.size(), ComponentUtils.formatList($$1, CustomBossEvent::getDisplayName)}
            ),
            false
         );
      }

      return $$1.size();
   }

   private static int createBar(CommandSourceStack $$0, Identifier $$1, Component $$2) throws CommandSyntaxException {
      CustomBossEvents $$3 = $$0.getServer().getCustomBossEvents();
      if ($$3.get($$1) != null) {
         throw ERROR_ALREADY_EXISTS.create($$1.toString());
      } else {
         CustomBossEvent $$4 = $$3.create($$1, ComponentUtils.updateForEntity($$0, $$2, null, 0));
         $$0.sendSuccess(() -> Component.translatable("commands.bossbar.create.success", new Object[]{$$4.getDisplayName()}), true);
         return $$3.getEvents().size();
      }
   }

   private static int removeBar(CommandSourceStack $$0, CustomBossEvent $$1) {
      CustomBossEvents $$2 = $$0.getServer().getCustomBossEvents();
      $$1.removeAllPlayers();
      $$2.remove($$1);
      $$0.sendSuccess(() -> Component.translatable("commands.bossbar.remove.success", new Object[]{$$1.getDisplayName()}), true);
      return $$2.getEvents().size();
   }

   public static CustomBossEvent getBossBar(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
      Identifier $$1 = IdentifierArgument.getId($$0, "id");
      CustomBossEvent $$2 = ((CommandSourceStack)$$0.getSource()).getServer().getCustomBossEvents().get($$1);
      if ($$2 == null) {
         throw ERROR_DOESNT_EXIST.create($$1.toString());
      } else {
         return $$2;
      }
   }
}
