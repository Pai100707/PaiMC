/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
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
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class TeamCommand {
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EXISTS = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.add.duplicate"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EMPTY = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.empty.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_NAME = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.option.name.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_COLOR = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.option.color.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.option.friendlyfire.alreadyEnabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.option.friendlyfire.alreadyDisabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.option.seeFriendlyInvisibles.alreadyEnabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.option.seeFriendlyInvisibles.alreadyDisabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.option.nametagVisibility.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.option.deathMessageVisibility.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_COLLISION_UNCHANGED = new SimpleCommandExceptionType((Message)Component.translatable("commands.team.option.collisionRule.unchanged"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$1) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("team").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((LiteralArgumentBuilder)Commands.literal("list").executes($$0 -> TeamCommand.listTeams((CommandSourceStack)$$0.getSource()))).then(Commands.argument("team", TeamArgument.team()).executes($$0 -> TeamCommand.listMembers((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team")))))).then(Commands.literal("add").then(((RequiredArgumentBuilder)Commands.argument("team", StringArgumentType.word()).executes($$0 -> TeamCommand.createTeam((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"team")))).then(Commands.argument("displayName", ComponentArgument.textComponent($$1)).executes($$0 -> TeamCommand.createTeam((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"team"), ComponentArgument.getResolvedComponent((CommandContext<CommandSourceStack>)$$0, "displayName"))))))).then(Commands.literal("remove").then(Commands.argument("team", TeamArgument.team()).executes($$0 -> TeamCommand.deleteTeam((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team")))))).then(Commands.literal("empty").then(Commands.argument("team", TeamArgument.team()).executes($$0 -> TeamCommand.emptyTeam((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team")))))).then(Commands.literal("join").then(((RequiredArgumentBuilder)Commands.argument("team", TeamArgument.team()).executes($$0 -> TeamCommand.joinTeam((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Collections.singleton(((CommandSourceStack)$$0.getSource()).getEntityOrException())))).then(Commands.argument("members", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes($$0 -> TeamCommand.joinTeam((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)$$0, "members"))))))).then(Commands.literal("leave").then(Commands.argument("members", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes($$0 -> TeamCommand.leaveTeam((CommandSourceStack)$$0.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard((CommandContext<CommandSourceStack>)$$0, "members")))))).then(Commands.literal("modify").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("team", TeamArgument.team()).then(Commands.literal("displayName").then(Commands.argument("displayName", ComponentArgument.textComponent($$1)).executes($$0 -> TeamCommand.setDisplayName((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), ComponentArgument.getResolvedComponent((CommandContext<CommandSourceStack>)$$0, "displayName")))))).then(Commands.literal("color").then(Commands.argument("value", ColorArgument.color()).executes($$0 -> TeamCommand.setColor((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), ColorArgument.getColor((CommandContext<CommandSourceStack>)$$0, "value")))))).then(Commands.literal("friendlyFire").then(Commands.argument("allowed", BoolArgumentType.bool()).executes($$0 -> TeamCommand.setFriendlyFire((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), BoolArgumentType.getBool((CommandContext)$$0, (String)"allowed")))))).then(Commands.literal("seeFriendlyInvisibles").then(Commands.argument("allowed", BoolArgumentType.bool()).executes($$0 -> TeamCommand.setFriendlySight((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), BoolArgumentType.getBool((CommandContext)$$0, (String)"allowed")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("nametagVisibility").then(Commands.literal("never").executes($$0 -> TeamCommand.setNametagVisibility((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.Visibility.NEVER)))).then(Commands.literal("hideForOtherTeams").executes($$0 -> TeamCommand.setNametagVisibility((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.Visibility.HIDE_FOR_OTHER_TEAMS)))).then(Commands.literal("hideForOwnTeam").executes($$0 -> TeamCommand.setNametagVisibility((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.Visibility.HIDE_FOR_OWN_TEAM)))).then(Commands.literal("always").executes($$0 -> TeamCommand.setNametagVisibility((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.Visibility.ALWAYS))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("deathMessageVisibility").then(Commands.literal("never").executes($$0 -> TeamCommand.setDeathMessageVisibility((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.Visibility.NEVER)))).then(Commands.literal("hideForOtherTeams").executes($$0 -> TeamCommand.setDeathMessageVisibility((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.Visibility.HIDE_FOR_OTHER_TEAMS)))).then(Commands.literal("hideForOwnTeam").executes($$0 -> TeamCommand.setDeathMessageVisibility((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.Visibility.HIDE_FOR_OWN_TEAM)))).then(Commands.literal("always").executes($$0 -> TeamCommand.setDeathMessageVisibility((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.Visibility.ALWAYS))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("collisionRule").then(Commands.literal("never").executes($$0 -> TeamCommand.setCollision((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.CollisionRule.NEVER)))).then(Commands.literal("pushOwnTeam").executes($$0 -> TeamCommand.setCollision((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.CollisionRule.PUSH_OWN_TEAM)))).then(Commands.literal("pushOtherTeams").executes($$0 -> TeamCommand.setCollision((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.CollisionRule.PUSH_OTHER_TEAMS)))).then(Commands.literal("always").executes($$0 -> TeamCommand.setCollision((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), Team.CollisionRule.ALWAYS))))).then(Commands.literal("prefix").then(Commands.argument("prefix", ComponentArgument.textComponent($$1)).executes($$0 -> TeamCommand.setPrefix((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), ComponentArgument.getResolvedComponent((CommandContext<CommandSourceStack>)$$0, "prefix")))))).then(Commands.literal("suffix").then(Commands.argument("suffix", ComponentArgument.textComponent($$1)).executes($$0 -> TeamCommand.setSuffix((CommandSourceStack)$$0.getSource(), TeamArgument.getTeam((CommandContext<CommandSourceStack>)$$0, "team"), ComponentArgument.getResolvedComponent((CommandContext<CommandSourceStack>)$$0, "suffix"))))))));
    }

    private static Component getFirstMemberName(Collection<ScoreHolder> $$0) {
        return $$0.iterator().next().getFeedbackDisplayName();
    }

    private static int leaveTeam(CommandSourceStack $$0, Collection<ScoreHolder> $$1) {
        ServerScoreboard $$2 = $$0.getServer().getScoreboard();
        for (ScoreHolder $$3 : $$1) {
            $$2.removePlayerFromTeam($$3.getScoreboardName());
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.team.leave.success.single", TeamCommand.getFirstMemberName($$1)), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.team.leave.success.multiple", $$1.size()), true);
        }
        return $$1.size();
    }

    private static int joinTeam(CommandSourceStack $$0, PlayerTeam $$1, Collection<ScoreHolder> $$2) {
        ServerScoreboard $$3 = $$0.getServer().getScoreboard();
        for (ScoreHolder $$4 : $$2) {
            ((Scoreboard)$$3).addPlayerToTeam($$4.getScoreboardName(), $$1);
        }
        if ($$2.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.team.join.success.single", TeamCommand.getFirstMemberName($$2), $$1.getFormattedDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.team.join.success.multiple", $$2.size(), $$1.getFormattedDisplayName()), true);
        }
        return $$2.size();
    }

    private static int setNametagVisibility(CommandSourceStack $$0, PlayerTeam $$1, Team.Visibility $$2) throws CommandSyntaxException {
        if ($$1.getNameTagVisibility() == $$2) {
            throw ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED.create();
        }
        $$1.setNameTagVisibility($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.team.option.nametagVisibility.success", $$1.getFormattedDisplayName(), $$2.getDisplayName()), true);
        return 0;
    }

    private static int setDeathMessageVisibility(CommandSourceStack $$0, PlayerTeam $$1, Team.Visibility $$2) throws CommandSyntaxException {
        if ($$1.getDeathMessageVisibility() == $$2) {
            throw ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED.create();
        }
        $$1.setDeathMessageVisibility($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.team.option.deathMessageVisibility.success", $$1.getFormattedDisplayName(), $$2.getDisplayName()), true);
        return 0;
    }

    private static int setCollision(CommandSourceStack $$0, PlayerTeam $$1, Team.CollisionRule $$2) throws CommandSyntaxException {
        if ($$1.getCollisionRule() == $$2) {
            throw ERROR_TEAM_COLLISION_UNCHANGED.create();
        }
        $$1.setCollisionRule($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.team.option.collisionRule.success", $$1.getFormattedDisplayName(), $$2.getDisplayName()), true);
        return 0;
    }

    private static int setFriendlySight(CommandSourceStack $$0, PlayerTeam $$1, boolean $$2) throws CommandSyntaxException {
        if ($$1.canSeeFriendlyInvisibles() == $$2) {
            if ($$2) {
                throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED.create();
            }
            throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED.create();
        }
        $$1.setSeeFriendlyInvisibles($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.team.option.seeFriendlyInvisibles." + ($$2 ? "enabled" : "disabled"), $$1.getFormattedDisplayName()), true);
        return 0;
    }

    private static int setFriendlyFire(CommandSourceStack $$0, PlayerTeam $$1, boolean $$2) throws CommandSyntaxException {
        if ($$1.isAllowFriendlyFire() == $$2) {
            if ($$2) {
                throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED.create();
            }
            throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED.create();
        }
        $$1.setAllowFriendlyFire($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.team.option.friendlyfire." + ($$2 ? "enabled" : "disabled"), $$1.getFormattedDisplayName()), true);
        return 0;
    }

    private static int setDisplayName(CommandSourceStack $$0, PlayerTeam $$1, Component $$2) throws CommandSyntaxException {
        if ($$1.getDisplayName().equals($$2)) {
            throw ERROR_TEAM_ALREADY_NAME.create();
        }
        $$1.setDisplayName($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.team.option.name.success", $$1.getFormattedDisplayName()), true);
        return 0;
    }

    private static int setColor(CommandSourceStack $$0, PlayerTeam $$1, ChatFormatting $$2) throws CommandSyntaxException {
        if ($$1.getColor() == $$2) {
            throw ERROR_TEAM_ALREADY_COLOR.create();
        }
        $$1.setColor($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.team.option.color.success", $$1.getFormattedDisplayName(), $$2.getName()), true);
        return 0;
    }

    private static int emptyTeam(CommandSourceStack $$0, PlayerTeam $$1) throws CommandSyntaxException {
        ServerScoreboard $$2 = $$0.getServer().getScoreboard();
        ArrayList $$3 = Lists.newArrayList($$1.getPlayers());
        if ($$3.isEmpty()) {
            throw ERROR_TEAM_ALREADY_EMPTY.create();
        }
        for (String $$4 : $$3) {
            ((Scoreboard)$$2).removePlayerFromTeam($$4, $$1);
        }
        $$0.sendSuccess(() -> Component.translatable("commands.team.empty.success", $$3.size(), $$1.getFormattedDisplayName()), true);
        return $$3.size();
    }

    private static int deleteTeam(CommandSourceStack $$0, PlayerTeam $$1) {
        ServerScoreboard $$2 = $$0.getServer().getScoreboard();
        $$2.removePlayerTeam($$1);
        $$0.sendSuccess(() -> Component.translatable("commands.team.remove.success", $$1.getFormattedDisplayName()), true);
        return $$2.getPlayerTeams().size();
    }

    private static int createTeam(CommandSourceStack $$0, String $$1) throws CommandSyntaxException {
        return TeamCommand.createTeam($$0, $$1, Component.literal($$1));
    }

    private static int createTeam(CommandSourceStack $$0, String $$1, Component $$2) throws CommandSyntaxException {
        ServerScoreboard $$3 = $$0.getServer().getScoreboard();
        if ($$3.getPlayerTeam($$1) != null) {
            throw ERROR_TEAM_ALREADY_EXISTS.create();
        }
        PlayerTeam $$4 = $$3.addPlayerTeam($$1);
        $$4.setDisplayName($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.team.add.success", $$4.getFormattedDisplayName()), true);
        return $$3.getPlayerTeams().size();
    }

    private static int listMembers(CommandSourceStack $$0, PlayerTeam $$1) {
        Collection<String> $$2 = $$1.getPlayers();
        if ($$2.isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.team.list.members.empty", $$1.getFormattedDisplayName()), false);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.team.list.members.success", $$1.getFormattedDisplayName(), $$2.size(), ComponentUtils.formatList($$2)), false);
        }
        return $$2.size();
    }

    private static int listTeams(CommandSourceStack $$0) {
        Collection<PlayerTeam> $$1 = $$0.getServer().getScoreboard().getPlayerTeams();
        if ($$1.isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.team.list.teams.empty"), false);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.team.list.teams.success", $$1.size(), ComponentUtils.formatList($$1, PlayerTeam::getFormattedDisplayName)), false);
        }
        return $$1.size();
    }

    private static int setPrefix(CommandSourceStack $$0, PlayerTeam $$1, Component $$2) {
        $$1.setPlayerPrefix($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.team.option.prefix.success", $$2), false);
        return 1;
    }

    private static int setSuffix(CommandSourceStack $$0, PlayerTeam $$1, Component $$2) {
        $$1.setPlayerSuffix($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.team.option.suffix.success", $$2), false);
        return 1;
    }
}

