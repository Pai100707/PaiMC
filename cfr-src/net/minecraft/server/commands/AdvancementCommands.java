/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCommands {
    private static final DynamicCommandExceptionType ERROR_NO_ACTION_PERFORMED = new DynamicCommandExceptionType($$0 -> (Component)$$0);
    private static final Dynamic2CommandExceptionType ERROR_CRITERION_NOT_FOUND = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.advancement.criterionNotFound", $$0, $$1));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("advancement").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("grant").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.literal("only").then(((RequiredArgumentBuilder)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes($$0 -> AdvancementCommands.perform((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.GRANT, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)$$0, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement"), Mode.ONLY)))).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests(($$0, $$1) -> SharedSuggestionProvider.suggest(ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement").value().criteria().keySet(), $$1)).executes($$0 -> AdvancementCommands.performCriterion((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.GRANT, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement"), StringArgumentType.getString((CommandContext)$$0, (String)"criterion"))))))).then(Commands.literal("from").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes($$0 -> AdvancementCommands.perform((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.GRANT, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)$$0, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement"), Mode.FROM)))))).then(Commands.literal("until").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes($$0 -> AdvancementCommands.perform((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.GRANT, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)$$0, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement"), Mode.UNTIL)))))).then(Commands.literal("through").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes($$0 -> AdvancementCommands.perform((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.GRANT, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)$$0, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement"), Mode.THROUGH)))))).then(Commands.literal("everything").executes($$0 -> AdvancementCommands.perform((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.GRANT, ((CommandSourceStack)$$0.getSource()).getServer().getAdvancements().getAllAdvancements(), false)))))).then(Commands.literal("revoke").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.literal("only").then(((RequiredArgumentBuilder)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes($$0 -> AdvancementCommands.perform((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)$$0, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement"), Mode.ONLY)))).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests(($$0, $$1) -> SharedSuggestionProvider.suggest(ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement").value().criteria().keySet(), $$1)).executes($$0 -> AdvancementCommands.performCriterion((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.REVOKE, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement"), StringArgumentType.getString((CommandContext)$$0, (String)"criterion"))))))).then(Commands.literal("from").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes($$0 -> AdvancementCommands.perform((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)$$0, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement"), Mode.FROM)))))).then(Commands.literal("until").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes($$0 -> AdvancementCommands.perform((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)$$0, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement"), Mode.UNTIL)))))).then(Commands.literal("through").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes($$0 -> AdvancementCommands.perform((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)$$0, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)$$0, "advancement"), Mode.THROUGH)))))).then(Commands.literal("everything").executes($$0 -> AdvancementCommands.perform((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Action.REVOKE, ((CommandSourceStack)$$0.getSource()).getServer().getAdvancements().getAllAdvancements()))))));
    }

    private static int perform(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Action $$2, Collection<AdvancementHolder> $$3) throws CommandSyntaxException {
        return AdvancementCommands.perform($$0, $$1, $$2, $$3, true);
    }

    private static int perform(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Action $$2, Collection<AdvancementHolder> $$3, boolean $$4) throws CommandSyntaxException {
        int $$5 = 0;
        for (ServerPlayer $$6 : $$1) {
            $$5 += $$2.perform($$6, $$3, $$4);
        }
        if ($$5 == 0) {
            if ($$3.size() == 1) {
                if ($$1.size() == 1) {
                    throw ERROR_NO_ACTION_PERFORMED.create((Object)Component.translatable($$2.getKey() + ".one.to.one.failure", Advancement.name($$3.iterator().next()), $$1.iterator().next().getDisplayName()));
                }
                throw ERROR_NO_ACTION_PERFORMED.create((Object)Component.translatable($$2.getKey() + ".one.to.many.failure", Advancement.name($$3.iterator().next()), $$1.size()));
            }
            if ($$1.size() == 1) {
                throw ERROR_NO_ACTION_PERFORMED.create((Object)Component.translatable($$2.getKey() + ".many.to.one.failure", $$3.size(), $$1.iterator().next().getDisplayName()));
            }
            throw ERROR_NO_ACTION_PERFORMED.create((Object)Component.translatable($$2.getKey() + ".many.to.many.failure", $$3.size(), $$1.size()));
        }
        if ($$3.size() == 1) {
            if ($$1.size() == 1) {
                $$0.sendSuccess(() -> Component.translatable($$2.getKey() + ".one.to.one.success", Advancement.name((AdvancementHolder)$$3.iterator().next()), ((ServerPlayer)$$1.iterator().next()).getDisplayName()), true);
            } else {
                $$0.sendSuccess(() -> Component.translatable($$2.getKey() + ".one.to.many.success", Advancement.name((AdvancementHolder)$$3.iterator().next()), $$1.size()), true);
            }
        } else if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable($$2.getKey() + ".many.to.one.success", $$3.size(), ((ServerPlayer)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable($$2.getKey() + ".many.to.many.success", $$3.size(), $$1.size()), true);
        }
        return $$5;
    }

    private static int performCriterion(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Action $$2, AdvancementHolder $$3, String $$4) throws CommandSyntaxException {
        int $$5 = 0;
        Advancement $$6 = $$3.value();
        if (!$$6.criteria().containsKey($$4)) {
            throw ERROR_CRITERION_NOT_FOUND.create((Object)Advancement.name($$3), (Object)$$4);
        }
        for (ServerPlayer $$7 : $$1) {
            if (!$$2.performCriterion($$7, $$3, $$4)) continue;
            ++$$5;
        }
        if ($$5 == 0) {
            if ($$1.size() == 1) {
                throw ERROR_NO_ACTION_PERFORMED.create((Object)Component.translatable($$2.getKey() + ".criterion.to.one.failure", $$4, Advancement.name($$3), $$1.iterator().next().getDisplayName()));
            }
            throw ERROR_NO_ACTION_PERFORMED.create((Object)Component.translatable($$2.getKey() + ".criterion.to.many.failure", $$4, Advancement.name($$3), $$1.size()));
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable($$2.getKey() + ".criterion.to.one.success", $$4, Advancement.name($$3), ((ServerPlayer)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable($$2.getKey() + ".criterion.to.many.success", $$4, Advancement.name($$3), $$1.size()), true);
        }
        return $$5;
    }

    private static List<AdvancementHolder> getAdvancements(CommandContext<CommandSourceStack> $$0, AdvancementHolder $$1, Mode $$2) {
        AdvancementTree $$3 = ((CommandSourceStack)$$0.getSource()).getServer().getAdvancements().tree();
        AdvancementNode $$4 = $$3.get($$1);
        if ($$4 == null) {
            return List.of($$1);
        }
        ArrayList<AdvancementHolder> $$5 = new ArrayList<AdvancementHolder>();
        if ($$2.parents) {
            for (AdvancementNode $$6 = $$4.parent(); $$6 != null; $$6 = $$6.parent()) {
                $$5.add($$6.holder());
            }
        }
        $$5.add($$1);
        if ($$2.children) {
            AdvancementCommands.addChildren($$4, $$5);
        }
        return $$5;
    }

    private static void addChildren(AdvancementNode $$0, List<AdvancementHolder> $$1) {
        for (AdvancementNode $$2 : $$0.children()) {
            $$1.add($$2.holder());
            AdvancementCommands.addChildren($$2, $$1);
        }
    }

    static enum Action {
        GRANT("grant"){

            @Override
            protected boolean perform(ServerPlayer $$0, AdvancementHolder $$1) {
                AdvancementProgress $$2 = $$0.getAdvancements().getOrStartProgress($$1);
                if ($$2.isDone()) {
                    return false;
                }
                for (String $$3 : $$2.getRemainingCriteria()) {
                    $$0.getAdvancements().award($$1, $$3);
                }
                return true;
            }

            @Override
            protected boolean performCriterion(ServerPlayer $$0, AdvancementHolder $$1, String $$2) {
                return $$0.getAdvancements().award($$1, $$2);
            }
        }
        ,
        REVOKE("revoke"){

            @Override
            protected boolean perform(ServerPlayer $$0, AdvancementHolder $$1) {
                AdvancementProgress $$2 = $$0.getAdvancements().getOrStartProgress($$1);
                if (!$$2.hasProgress()) {
                    return false;
                }
                for (String $$3 : $$2.getCompletedCriteria()) {
                    $$0.getAdvancements().revoke($$1, $$3);
                }
                return true;
            }

            @Override
            protected boolean performCriterion(ServerPlayer $$0, AdvancementHolder $$1, String $$2) {
                return $$0.getAdvancements().revoke($$1, $$2);
            }
        };

        private final String key;

        Action(String $$0) {
            this.key = "commands.advancement." + $$0;
        }

        public int perform(ServerPlayer $$0, Iterable<AdvancementHolder> $$1, boolean $$2) {
            int $$3 = 0;
            if (!$$2) {
                $$0.getAdvancements().flushDirty($$0, true);
            }
            for (AdvancementHolder $$4 : $$1) {
                if (!this.perform($$0, $$4)) continue;
                ++$$3;
            }
            if (!$$2) {
                $$0.getAdvancements().flushDirty($$0, false);
            }
            return $$3;
        }

        protected abstract boolean perform(ServerPlayer var1, AdvancementHolder var2);

        protected abstract boolean performCriterion(ServerPlayer var1, AdvancementHolder var2, String var3);

        protected String getKey() {
            return this.key;
        }
    }

    static enum Mode {
        ONLY(false, false),
        THROUGH(true, true),
        FROM(false, true),
        UNTIL(true, false),
        EVERYTHING(true, true);

        final boolean parents;
        final boolean children;

        private Mode(boolean $$0, boolean $$1) {
            this.parents = $$0;
            this.children = $$1;
        }
    }
}

