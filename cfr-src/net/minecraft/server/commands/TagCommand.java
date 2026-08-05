/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.HashSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.Entity;

public class TagCommand {
    private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.tag.add.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.tag.remove.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tag").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities()).then(Commands.literal("add").then(Commands.argument("name", StringArgumentType.word()).executes($$0 -> TagCommand.addTag((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), StringArgumentType.getString((CommandContext)$$0, (String)"name")))))).then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.word()).suggests(($$0, $$1) -> SharedSuggestionProvider.suggest(TagCommand.getTags(EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets")), $$1)).executes($$0 -> TagCommand.removeTag((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), StringArgumentType.getString((CommandContext)$$0, (String)"name")))))).then(Commands.literal("list").executes($$0 -> TagCommand.listTags((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"))))));
    }

    private static Collection<String> getTags(Collection<? extends Entity> $$0) {
        HashSet $$1 = Sets.newHashSet();
        for (Entity entity : $$0) {
            $$1.addAll(entity.getTags());
        }
        return $$1;
    }

    private static int addTag(CommandSourceStack $$0, Collection<? extends Entity> $$1, String $$2) throws CommandSyntaxException {
        int $$3 = 0;
        for (Entity entity : $$1) {
            if (!entity.addTag($$2)) continue;
            ++$$3;
        }
        if ($$3 == 0) {
            throw ERROR_ADD_FAILED.create();
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.tag.add.success.single", $$2, ((Entity)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.tag.add.success.multiple", $$2, $$1.size()), true);
        }
        return $$3;
    }

    private static int removeTag(CommandSourceStack $$0, Collection<? extends Entity> $$1, String $$2) throws CommandSyntaxException {
        int $$3 = 0;
        for (Entity entity : $$1) {
            if (!entity.removeTag($$2)) continue;
            ++$$3;
        }
        if ($$3 == 0) {
            throw ERROR_REMOVE_FAILED.create();
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.tag.remove.success.single", $$2, ((Entity)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.tag.remove.success.multiple", $$2, $$1.size()), true);
        }
        return $$3;
    }

    private static int listTags(CommandSourceStack $$0, Collection<? extends Entity> $$1) {
        HashSet $$2 = Sets.newHashSet();
        for (Entity entity : $$1) {
            $$2.addAll(entity.getTags());
        }
        if ($$1.size() == 1) {
            Entity $$4 = $$1.iterator().next();
            if ($$2.isEmpty()) {
                $$0.sendSuccess(() -> Component.translatable("commands.tag.list.single.empty", $$4.getDisplayName()), false);
            } else {
                $$0.sendSuccess(() -> Component.translatable("commands.tag.list.single.success", $$4.getDisplayName(), $$2.size(), ComponentUtils.formatList($$2)), false);
            }
        } else if ($$2.isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.tag.list.multiple.empty", $$1.size()), false);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.tag.list.multiple.success", $$2.size(), $$2.size(), ComponentUtils.formatList($$2)), false);
        }
        return $$2.size();
    }
}

