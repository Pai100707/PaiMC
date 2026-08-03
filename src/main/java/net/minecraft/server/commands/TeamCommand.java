package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team.CollisionRule;
import net.minecraft.world.scores.Team.Visibility;

public class TeamCommand {
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EXISTS = new SimpleCommandExceptionType(
      Component.translatable("commands.team.add.duplicate")
   );
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EMPTY = new SimpleCommandExceptionType(
      Component.translatable("commands.team.empty.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_NAME = new SimpleCommandExceptionType(
      Component.translatable("commands.team.option.name.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_COLOR = new SimpleCommandExceptionType(
      Component.translatable("commands.team.option.color.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED = new SimpleCommandExceptionType(
      Component.translatable("commands.team.option.friendlyfire.alreadyEnabled")
   );
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED = new SimpleCommandExceptionType(
      Component.translatable("commands.team.option.friendlyfire.alreadyDisabled")
   );
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED = new SimpleCommandExceptionType(
      Component.translatable("commands.team.option.seeFriendlyInvisibles.alreadyEnabled")
   );
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED = new SimpleCommandExceptionType(
      Component.translatable("commands.team.option.seeFriendlyInvisibles.alreadyDisabled")
   );
   private static final SimpleCommandExceptionType ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(
      Component.translatable("commands.team.option.nametagVisibility.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(
      Component.translatable("commands.team.option.deathMessageVisibility.unchanged")
   );
   private static final SimpleCommandExceptionType ERROR_TEAM_COLLISION_UNCHANGED = new SimpleCommandExceptionType(
      Component.translatable("commands.team.option.collisionRule.unchanged")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                    "team"
                                 )
                                 .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                              .then(
                                 ((LiteralArgumentBuilder)Commands.literal("list").executes($$0x -> listTeams((CommandSourceStack)$$0x.getSource())))
                                    .then(
                                       Commands.argument("team", TeamArgument.team())
                                          .executes($$0x -> listMembers((CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team")))
                                    )
                              ))
                           .then(
                              Commands.literal("add")
                                 .then(
                                    ((RequiredArgumentBuilder)Commands.argument("team", StringArgumentType.word())
                                          .executes($$0x -> createTeam((CommandSourceStack)$$0x.getSource(), StringArgumentType.getString($$0x, "team"))))
                                       .then(
                                          Commands.argument("displayName", ComponentArgument.textComponent($$1))
                                             .executes(
                                                $$0x -> createTeam(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   StringArgumentType.getString($$0x, "team"),
                                                   ComponentArgument.getResolvedComponent($$0x, "displayName")
                                                )
                                             )
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("remove")
                              .then(
                                 Commands.argument("team", TeamArgument.team())
                                    .executes($$0x -> deleteTeam((CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team")))
                              )
                        ))
                     .then(
                        Commands.literal("empty")
                           .then(
                              Commands.argument("team", TeamArgument.team())
                                 .executes($$0x -> emptyTeam((CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team")))
                           )
                     ))
                  .then(
                     Commands.literal("join")
                        .then(
                           ((RequiredArgumentBuilder)Commands.argument("team", TeamArgument.team())
                                 .executes(
                                    $$0x -> joinTeam(
                                       (CommandSourceStack)$$0x.getSource(),
                                       TeamArgument.getTeam($$0x, "team"),
                                       Collections.singleton(((CommandSourceStack)$$0x.getSource()).getEntityOrException())
                                    )
                                 ))
                              .then(
                                 Commands.argument("members", ScoreHolderArgument.scoreHolders())
                                    .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                    .executes(
                                       $$0x -> joinTeam(
                                          (CommandSourceStack)$$0x.getSource(),
                                          TeamArgument.getTeam($$0x, "team"),
                                          ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "members")
                                       )
                                    )
                              )
                        )
                  ))
               .then(
                  Commands.literal("leave")
                     .then(
                        Commands.argument("members", ScoreHolderArgument.scoreHolders())
                           .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                           .executes($$0x -> leaveTeam((CommandSourceStack)$$0x.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard($$0x, "members")))
                     )
               ))
            .then(
               Commands.literal("modify")
                  .then(
                     ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                                   "team", TeamArgument.team()
                                                )
                                                .then(
                                                   Commands.literal("displayName")
                                                      .then(
                                                         Commands.argument("displayName", ComponentArgument.textComponent($$1))
                                                            .executes(
                                                               $$0x -> setDisplayName(
                                                                  (CommandSourceStack)$$0x.getSource(),
                                                                  TeamArgument.getTeam($$0x, "team"),
                                                                  ComponentArgument.getResolvedComponent($$0x, "displayName")
                                                               )
                                                            )
                                                      )
                                                ))
                                             .then(
                                                Commands.literal("color")
                                                   .then(
                                                      Commands.argument("value", ColorArgument.color())
                                                         .executes(
                                                            $$0x -> setColor(
                                                               (CommandSourceStack)$$0x.getSource(),
                                                               TeamArgument.getTeam($$0x, "team"),
                                                               ColorArgument.getColor($$0x, "value")
                                                            )
                                                         )
                                                   )
                                             ))
                                          .then(
                                             Commands.literal("friendlyFire")
                                                .then(
                                                   Commands.argument("allowed", BoolArgumentType.bool())
                                                      .executes(
                                                         $$0x -> setFriendlyFire(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            TeamArgument.getTeam($$0x, "team"),
                                                            BoolArgumentType.getBool($$0x, "allowed")
                                                         )
                                                      )
                                                )
                                          ))
                                       .then(
                                          Commands.literal("seeFriendlyInvisibles")
                                             .then(
                                                Commands.argument("allowed", BoolArgumentType.bool())
                                                   .executes(
                                                      $$0x -> setFriendlySight(
                                                         (CommandSourceStack)$$0x.getSource(),
                                                         TeamArgument.getTeam($$0x, "team"),
                                                         BoolArgumentType.getBool($$0x, "allowed")
                                                      )
                                                   )
                                             )
                                       ))
                                    .then(
                                       ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("nametagVisibility")
                                                   .then(
                                                      Commands.literal("never")
                                                         .executes(
                                                            $$0x -> setNametagVisibility(
                                                               (CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team"), Visibility.NEVER
                                                            )
                                                         )
                                                   ))
                                                .then(
                                                   Commands.literal("hideForOtherTeams")
                                                      .executes(
                                                         $$0x -> setNametagVisibility(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            TeamArgument.getTeam($$0x, "team"),
                                                            Visibility.HIDE_FOR_OTHER_TEAMS
                                                         )
                                                      )
                                                ))
                                             .then(
                                                Commands.literal("hideForOwnTeam")
                                                   .executes(
                                                      $$0x -> setNametagVisibility(
                                                         (CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team"), Visibility.HIDE_FOR_OWN_TEAM
                                                      )
                                                   )
                                             ))
                                          .then(
                                             Commands.literal("always")
                                                .executes(
                                                   $$0x -> setNametagVisibility(
                                                      (CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team"), Visibility.ALWAYS
                                                   )
                                                )
                                          )
                                    ))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("deathMessageVisibility")
                                                .then(
                                                   Commands.literal("never")
                                                      .executes(
                                                         $$0x -> setDeathMessageVisibility(
                                                            (CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team"), Visibility.NEVER
                                                         )
                                                      )
                                                ))
                                             .then(
                                                Commands.literal("hideForOtherTeams")
                                                   .executes(
                                                      $$0x -> setDeathMessageVisibility(
                                                         (CommandSourceStack)$$0x.getSource(),
                                                         TeamArgument.getTeam($$0x, "team"),
                                                         Visibility.HIDE_FOR_OTHER_TEAMS
                                                      )
                                                   )
                                             ))
                                          .then(
                                             Commands.literal("hideForOwnTeam")
                                                .executes(
                                                   $$0x -> setDeathMessageVisibility(
                                                      (CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team"), Visibility.HIDE_FOR_OWN_TEAM
                                                   )
                                                )
                                          ))
                                       .then(
                                          Commands.literal("always")
                                             .executes(
                                                $$0x -> setDeathMessageVisibility(
                                                   (CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team"), Visibility.ALWAYS
                                                )
                                             )
                                       )
                                 ))
                              .then(
                                 ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("collisionRule")
                                             .then(
                                                Commands.literal("never")
                                                   .executes(
                                                      $$0x -> setCollision(
                                                         (CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team"), CollisionRule.NEVER
                                                      )
                                                   )
                                             ))
                                          .then(
                                             Commands.literal("pushOwnTeam")
                                                .executes(
                                                   $$0x -> setCollision(
                                                      (CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team"), CollisionRule.PUSH_OWN_TEAM
                                                   )
                                                )
                                          ))
                                       .then(
                                          Commands.literal("pushOtherTeams")
                                             .executes(
                                                $$0x -> setCollision(
                                                   (CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team"), CollisionRule.PUSH_OTHER_TEAMS
                                                )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("always")
                                          .executes(
                                             $$0x -> setCollision(
                                                (CommandSourceStack)$$0x.getSource(), TeamArgument.getTeam($$0x, "team"), CollisionRule.ALWAYS
                                             )
                                          )
                                    )
                              ))
                           .then(
                              Commands.literal("prefix")
                                 .then(
                                    Commands.argument("prefix", ComponentArgument.textComponent($$1))
                                       .executes(
                                          $$0x -> setPrefix(
                                             (CommandSourceStack)$$0x.getSource(),
                                             TeamArgument.getTeam($$0x, "team"),
                                             ComponentArgument.getResolvedComponent($$0x, "prefix")
                                          )
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("suffix")
                              .then(
                                 Commands.argument("suffix", ComponentArgument.textComponent($$1))
                                    .executes(
                                       $$0x -> setSuffix(
                                          (CommandSourceStack)$$0x.getSource(),
                                          TeamArgument.getTeam($$0x, "team"),
                                          ComponentArgument.getResolvedComponent($$0x, "suffix")
                                       )
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static Component getFirstMemberName(Collection<ScoreHolder> $$0) {
      return $$0.iterator().next().getFeedbackDisplayName();
   }

   private static int leaveTeam(CommandSourceStack $$0, Collection<ScoreHolder> $$1) {
      Scoreboard $$2 = $$0.getServer().getScoreboard();

      for (ScoreHolder $$3 : $$1) {
         $$2.removePlayerFromTeam($$3.getScoreboardName());
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(() -> Component.translatable("commands.team.leave.success.single", new Object[]{getFirstMemberName($$1)}), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.team.leave.success.multiple", new Object[]{$$1.size()}), true);
      }

      return $$1.size();
   }

   private static int joinTeam(CommandSourceStack $$0, PlayerTeam $$1, Collection<ScoreHolder> $$2) {
      Scoreboard $$3 = $$0.getServer().getScoreboard();

      for (ScoreHolder $$4 : $$2) {
         $$3.addPlayerToTeam($$4.getScoreboardName(), $$1);
      }

      if ($$2.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable("commands.team.join.success.single", new Object[]{getFirstMemberName($$2), $$1.getFormattedDisplayName()}), true
         );
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.team.join.success.multiple", new Object[]{$$2.size(), $$1.getFormattedDisplayName()}), true);
      }

      return $$2.size();
   }

   private static int setNametagVisibility(CommandSourceStack $$0, PlayerTeam $$1, Visibility $$2) throws CommandSyntaxException {
      if ($$1.getNameTagVisibility() == $$2) {
         throw ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED.create();
      } else {
         $$1.setNameTagVisibility($$2);
         $$0.sendSuccess(
            () -> Component.translatable("commands.team.option.nametagVisibility.success", new Object[]{$$1.getFormattedDisplayName(), $$2.getDisplayName()}),
            true
         );
         return 0;
      }
   }

   private static int setDeathMessageVisibility(CommandSourceStack $$0, PlayerTeam $$1, Visibility $$2) throws CommandSyntaxException {
      if ($$1.getDeathMessageVisibility() == $$2) {
         throw ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED.create();
      } else {
         $$1.setDeathMessageVisibility($$2);
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.team.option.deathMessageVisibility.success", new Object[]{$$1.getFormattedDisplayName(), $$2.getDisplayName()}
            ),
            true
         );
         return 0;
      }
   }

   private static int setCollision(CommandSourceStack $$0, PlayerTeam $$1, CollisionRule $$2) throws CommandSyntaxException {
      if ($$1.getCollisionRule() == $$2) {
         throw ERROR_TEAM_COLLISION_UNCHANGED.create();
      } else {
         $$1.setCollisionRule($$2);
         $$0.sendSuccess(
            () -> Component.translatable("commands.team.option.collisionRule.success", new Object[]{$$1.getFormattedDisplayName(), $$2.getDisplayName()}), true
         );
         return 0;
      }
   }

   private static int setFriendlySight(CommandSourceStack $$0, PlayerTeam $$1, boolean $$2) throws CommandSyntaxException {
      if ($$1.canSeeFriendlyInvisibles() == $$2) {
         if ($$2) {
            throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED.create();
         } else {
            throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED.create();
         }
      } else {
         $$1.setSeeFriendlyInvisibles($$2);
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.team.option.seeFriendlyInvisibles." + ($$2 ? "enabled" : "disabled"), new Object[]{$$1.getFormattedDisplayName()}
            ),
            true
         );
         return 0;
      }
   }

   private static int setFriendlyFire(CommandSourceStack $$0, PlayerTeam $$1, boolean $$2) throws CommandSyntaxException {
      if ($$1.isAllowFriendlyFire() == $$2) {
         if ($$2) {
            throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED.create();
         } else {
            throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED.create();
         }
      } else {
         $$1.setAllowFriendlyFire($$2);
         $$0.sendSuccess(
            () -> Component.translatable("commands.team.option.friendlyfire." + ($$2 ? "enabled" : "disabled"), new Object[]{$$1.getFormattedDisplayName()}),
            true
         );
         return 0;
      }
   }

   private static int setDisplayName(CommandSourceStack $$0, PlayerTeam $$1, Component $$2) throws CommandSyntaxException {
      if ($$1.getDisplayName().equals($$2)) {
         throw ERROR_TEAM_ALREADY_NAME.create();
      } else {
         $$1.setDisplayName($$2);
         $$0.sendSuccess(() -> Component.translatable("commands.team.option.name.success", new Object[]{$$1.getFormattedDisplayName()}), true);
         return 0;
      }
   }

   private static int setColor(CommandSourceStack $$0, PlayerTeam $$1, ChatFormatting $$2) throws CommandSyntaxException {
      if ($$1.getColor() == $$2) {
         throw ERROR_TEAM_ALREADY_COLOR.create();
      } else {
         $$1.setColor($$2);
         $$0.sendSuccess(() -> Component.translatable("commands.team.option.color.success", new Object[]{$$1.getFormattedDisplayName(), $$2.getName()}), true);
         return 0;
      }
   }

   private static int emptyTeam(CommandSourceStack $$0, PlayerTeam $$1) throws CommandSyntaxException {
      Scoreboard $$2 = $$0.getServer().getScoreboard();
      Collection<String> $$3 = Lists.newArrayList($$1.getPlayers());
      if ($$3.isEmpty()) {
         throw ERROR_TEAM_ALREADY_EMPTY.create();
      } else {
         for (String $$4 : $$3) {
            $$2.removePlayerFromTeam($$4, $$1);
         }

         $$0.sendSuccess(() -> Component.translatable("commands.team.empty.success", new Object[]{$$3.size(), $$1.getFormattedDisplayName()}), true);
         return $$3.size();
      }
   }

   private static int deleteTeam(CommandSourceStack $$0, PlayerTeam $$1) {
      Scoreboard $$2 = $$0.getServer().getScoreboard();
      $$2.removePlayerTeam($$1);
      $$0.sendSuccess(() -> Component.translatable("commands.team.remove.success", new Object[]{$$1.getFormattedDisplayName()}), true);
      return $$2.getPlayerTeams().size();
   }

   private static int createTeam(CommandSourceStack $$0, String $$1) throws CommandSyntaxException {
      return createTeam($$0, $$1, Component.literal($$1));
   }

   private static int createTeam(CommandSourceStack $$0, String $$1, Component $$2) throws CommandSyntaxException {
      Scoreboard $$3 = $$0.getServer().getScoreboard();
      if ($$3.getPlayerTeam($$1) != null) {
         throw ERROR_TEAM_ALREADY_EXISTS.create();
      } else {
         PlayerTeam $$4 = $$3.addPlayerTeam($$1);
         $$4.setDisplayName($$2);
         $$0.sendSuccess(() -> Component.translatable("commands.team.add.success", new Object[]{$$4.getFormattedDisplayName()}), true);
         return $$3.getPlayerTeams().size();
      }
   }

   private static int listMembers(CommandSourceStack $$0, PlayerTeam $$1) {
      Collection<String> $$2 = $$1.getPlayers();
      if ($$2.isEmpty()) {
         $$0.sendSuccess(() -> Component.translatable("commands.team.list.members.empty", new Object[]{$$1.getFormattedDisplayName()}), false);
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.team.list.members.success", new Object[]{$$1.getFormattedDisplayName(), $$2.size(), ComponentUtils.formatList($$2)}
            ),
            false
         );
      }

      return $$2.size();
   }

   private static int listTeams(CommandSourceStack $$0) {
      Collection<PlayerTeam> $$1 = $$0.getServer().getScoreboard().getPlayerTeams();
      if ($$1.isEmpty()) {
         $$0.sendSuccess(() -> Component.translatable("commands.team.list.teams.empty"), false);
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.team.list.teams.success", new Object[]{$$1.size(), ComponentUtils.formatList($$1, PlayerTeam::getFormattedDisplayName)}
            ),
            false
         );
      }

      return $$1.size();
   }

   private static int setPrefix(CommandSourceStack $$0, PlayerTeam $$1, Component $$2) {
      $$1.setPlayerPrefix($$2);
      $$0.sendSuccess(() -> Component.translatable("commands.team.option.prefix.success", new Object[]{$$2}), false);
      return 1;
   }

   private static int setSuffix(CommandSourceStack $$0, PlayerTeam $$1, Component $$2) {
      $$1.setPlayerSuffix($$2);
      $$0.sendSuccess(() -> Component.translatable("commands.team.option.suffix.success", new Object[]{$$2}), false);
      return 1;
   }
}
