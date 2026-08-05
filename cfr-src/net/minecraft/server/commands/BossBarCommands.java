/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;

public class BossBarCommands {
    private static final DynamicCommandExceptionType ERROR_ALREADY_EXISTS = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.bossbar.create.failed", $$0));
    private static final DynamicCommandExceptionType ERROR_DOESNT_EXIST = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.bossbar.unknown", $$0));
    private static final SimpleCommandExceptionType ERROR_NO_PLAYER_CHANGE = new SimpleCommandExceptionType((Message)Component.translatable("commands.bossbar.set.players.unchanged"));
    private static final SimpleCommandExceptionType ERROR_NO_NAME_CHANGE = new SimpleCommandExceptionType((Message)Component.translatable("commands.bossbar.set.name.unchanged"));
    private static final SimpleCommandExceptionType ERROR_NO_COLOR_CHANGE = new SimpleCommandExceptionType((Message)Component.translatable("commands.bossbar.set.color.unchanged"));
    private static final SimpleCommandExceptionType ERROR_NO_STYLE_CHANGE = new SimpleCommandExceptionType((Message)Component.translatable("commands.bossbar.set.style.unchanged"));
    private static final SimpleCommandExceptionType ERROR_NO_VALUE_CHANGE = new SimpleCommandExceptionType((Message)Component.translatable("commands.bossbar.set.value.unchanged"));
    private static final SimpleCommandExceptionType ERROR_NO_MAX_CHANGE = new SimpleCommandExceptionType((Message)Component.translatable("commands.bossbar.set.max.unchanged"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_HIDDEN = new SimpleCommandExceptionType((Message)Component.translatable("commands.bossbar.set.visibility.unchanged.hidden"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_VISIBLE = new SimpleCommandExceptionType((Message)Component.translatable("commands.bossbar.set.visibility.unchanged.visible"));
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_BOSS_BAR = ($$0, $$1) -> SharedSuggestionProvider.suggestResource(((CommandSourceStack)$$0.getSource()).getServer().getCustomBossEvents().getIds(), $$1);

    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$1) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("bossbar").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("add").then(Commands.argument("id", IdentifierArgument.id()).then(Commands.argument("name", ComponentArgument.textComponent($$1)).executes($$0 -> BossBarCommands.createBar((CommandSourceStack)$$0.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "id"), ComponentArgument.getResolvedComponent((CommandContext<CommandSourceStack>)$$0, "name"))))))).then(Commands.literal("remove").then(Commands.argument("id", IdentifierArgument.id()).suggests(SUGGEST_BOSS_BAR).executes($$0 -> BossBarCommands.removeBar((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0)))))).then(Commands.literal("list").executes($$0 -> BossBarCommands.listBars((CommandSourceStack)$$0.getSource())))).then(Commands.literal("set").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id()).suggests(SUGGEST_BOSS_BAR).then(Commands.literal("name").then(Commands.argument("name", ComponentArgument.textComponent($$1)).executes($$0 -> BossBarCommands.setName((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), ComponentArgument.getResolvedComponent((CommandContext<CommandSourceStack>)$$0, "name")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("color").then(Commands.literal("pink").executes($$0 -> BossBarCommands.setColor((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarColor.PINK)))).then(Commands.literal("blue").executes($$0 -> BossBarCommands.setColor((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarColor.BLUE)))).then(Commands.literal("red").executes($$0 -> BossBarCommands.setColor((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarColor.RED)))).then(Commands.literal("green").executes($$0 -> BossBarCommands.setColor((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarColor.GREEN)))).then(Commands.literal("yellow").executes($$0 -> BossBarCommands.setColor((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarColor.YELLOW)))).then(Commands.literal("purple").executes($$0 -> BossBarCommands.setColor((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarColor.PURPLE)))).then(Commands.literal("white").executes($$0 -> BossBarCommands.setColor((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarColor.WHITE))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("style").then(Commands.literal("progress").executes($$0 -> BossBarCommands.setStyle((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarOverlay.PROGRESS)))).then(Commands.literal("notched_6").executes($$0 -> BossBarCommands.setStyle((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarOverlay.NOTCHED_6)))).then(Commands.literal("notched_10").executes($$0 -> BossBarCommands.setStyle((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarOverlay.NOTCHED_10)))).then(Commands.literal("notched_12").executes($$0 -> BossBarCommands.setStyle((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarOverlay.NOTCHED_12)))).then(Commands.literal("notched_20").executes($$0 -> BossBarCommands.setStyle((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BossEvent.BossBarOverlay.NOTCHED_20))))).then(Commands.literal("value").then(Commands.argument("value", IntegerArgumentType.integer((int)0)).executes($$0 -> BossBarCommands.setValue((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"value")))))).then(Commands.literal("max").then(Commands.argument("max", IntegerArgumentType.integer((int)1)).executes($$0 -> BossBarCommands.setMax((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"max")))))).then(Commands.literal("visible").then(Commands.argument("visible", BoolArgumentType.bool()).executes($$0 -> BossBarCommands.setVisible((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), BoolArgumentType.getBool((CommandContext)$$0, (String)"visible")))))).then(((LiteralArgumentBuilder)Commands.literal("players").executes($$0 -> BossBarCommands.setPlayers((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), Collections.emptyList()))).then(Commands.argument("targets", EntityArgument.players()).executes($$0 -> BossBarCommands.setPlayers((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0), EntityArgument.getOptionalPlayers((CommandContext<CommandSourceStack>)$$0, "targets")))))))).then(Commands.literal("get").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id()).suggests(SUGGEST_BOSS_BAR).then(Commands.literal("value").executes($$0 -> BossBarCommands.getValue((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0))))).then(Commands.literal("max").executes($$0 -> BossBarCommands.getMax((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0))))).then(Commands.literal("visible").executes($$0 -> BossBarCommands.getVisible((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0))))).then(Commands.literal("players").executes($$0 -> BossBarCommands.getPlayers((CommandSourceStack)$$0.getSource(), BossBarCommands.getBossBar((CommandContext<CommandSourceStack>)$$0)))))));
    }

    private static int getValue(CommandSourceStack $$0, CustomBossEvent $$1) {
        $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.value", $$1.getDisplayName(), $$1.getValue()), true);
        return $$1.getValue();
    }

    private static int getMax(CommandSourceStack $$0, CustomBossEvent $$1) {
        $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.max", $$1.getDisplayName(), $$1.getMax()), true);
        return $$1.getMax();
    }

    private static int getVisible(CommandSourceStack $$0, CustomBossEvent $$1) {
        if ($$1.isVisible()) {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.visible.visible", $$1.getDisplayName()), true);
            return 1;
        }
        $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.visible.hidden", $$1.getDisplayName()), true);
        return 0;
    }

    private static int getPlayers(CommandSourceStack $$0, CustomBossEvent $$1) {
        if ($$1.getPlayers().isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.players.none", $$1.getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.get.players.some", $$1.getDisplayName(), $$1.getPlayers().size(), ComponentUtils.formatList($$1.getPlayers(), Player::getDisplayName)), true);
        }
        return $$1.getPlayers().size();
    }

    private static int setVisible(CommandSourceStack $$0, CustomBossEvent $$1, boolean $$2) throws CommandSyntaxException {
        if ($$1.isVisible() == $$2) {
            if ($$2) {
                throw ERROR_ALREADY_VISIBLE.create();
            }
            throw ERROR_ALREADY_HIDDEN.create();
        }
        $$1.setVisible($$2);
        if ($$2) {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.visible.success.visible", $$1.getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.visible.success.hidden", $$1.getDisplayName()), true);
        }
        return 0;
    }

    private static int setValue(CommandSourceStack $$0, CustomBossEvent $$1, int $$2) throws CommandSyntaxException {
        if ($$1.getValue() == $$2) {
            throw ERROR_NO_VALUE_CHANGE.create();
        }
        $$1.setValue($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.value.success", $$1.getDisplayName(), $$2), true);
        return $$2;
    }

    private static int setMax(CommandSourceStack $$0, CustomBossEvent $$1, int $$2) throws CommandSyntaxException {
        if ($$1.getMax() == $$2) {
            throw ERROR_NO_MAX_CHANGE.create();
        }
        $$1.setMax($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.max.success", $$1.getDisplayName(), $$2), true);
        return $$2;
    }

    private static int setColor(CommandSourceStack $$0, CustomBossEvent $$1, BossEvent.BossBarColor $$2) throws CommandSyntaxException {
        if ($$1.getColor().equals($$2)) {
            throw ERROR_NO_COLOR_CHANGE.create();
        }
        $$1.setColor($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.color.success", $$1.getDisplayName()), true);
        return 0;
    }

    private static int setStyle(CommandSourceStack $$0, CustomBossEvent $$1, BossEvent.BossBarOverlay $$2) throws CommandSyntaxException {
        if ($$1.getOverlay().equals($$2)) {
            throw ERROR_NO_STYLE_CHANGE.create();
        }
        $$1.setOverlay($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.style.success", $$1.getDisplayName()), true);
        return 0;
    }

    private static int setName(CommandSourceStack $$0, CustomBossEvent $$1, Component $$2) throws CommandSyntaxException {
        MutableComponent $$3 = ComponentUtils.updateForEntity($$0, $$2, null, 0);
        if ($$1.getName().equals($$3)) {
            throw ERROR_NO_NAME_CHANGE.create();
        }
        $$1.setName($$3);
        $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.name.success", $$1.getDisplayName()), true);
        return 0;
    }

    private static int setPlayers(CommandSourceStack $$0, CustomBossEvent $$1, Collection<ServerPlayer> $$2) throws CommandSyntaxException {
        boolean $$3 = $$1.setPlayers($$2);
        if (!$$3) {
            throw ERROR_NO_PLAYER_CHANGE.create();
        }
        if ($$1.getPlayers().isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.players.success.none", $$1.getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.set.players.success.some", $$1.getDisplayName(), $$2.size(), ComponentUtils.formatList($$2, Player::getDisplayName)), true);
        }
        return $$1.getPlayers().size();
    }

    private static int listBars(CommandSourceStack $$0) {
        Collection<CustomBossEvent> $$1 = $$0.getServer().getCustomBossEvents().getEvents();
        if ($$1.isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.list.bars.none"), false);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.bossbar.list.bars.some", $$1.size(), ComponentUtils.formatList($$1, CustomBossEvent::getDisplayName)), false);
        }
        return $$1.size();
    }

    private static int createBar(CommandSourceStack $$0, Identifier $$1, Component $$2) throws CommandSyntaxException {
        CustomBossEvents $$3 = $$0.getServer().getCustomBossEvents();
        if ($$3.get($$1) != null) {
            throw ERROR_ALREADY_EXISTS.create((Object)$$1.toString());
        }
        CustomBossEvent $$4 = $$3.create($$1, ComponentUtils.updateForEntity($$0, $$2, null, 0));
        $$0.sendSuccess(() -> Component.translatable("commands.bossbar.create.success", $$4.getDisplayName()), true);
        return $$3.getEvents().size();
    }

    private static int removeBar(CommandSourceStack $$0, CustomBossEvent $$1) {
        CustomBossEvents $$2 = $$0.getServer().getCustomBossEvents();
        $$1.removeAllPlayers();
        $$2.remove($$1);
        $$0.sendSuccess(() -> Component.translatable("commands.bossbar.remove.success", $$1.getDisplayName()), true);
        return $$2.getEvents().size();
    }

    public static CustomBossEvent getBossBar(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
        Identifier $$1 = IdentifierArgument.getId($$0, "id");
        CustomBossEvent $$2 = ((CommandSourceStack)$$0.getSource()).getServer().getCustomBossEvents().get($$1);
        if ($$2 == null) {
            throw ERROR_DOESNT_EXIST.create((Object)$$1.toString());
        }
        return $$2;
    }
}

