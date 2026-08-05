/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeCommand {
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.recipe.give.failed"));
    private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.recipe.take.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("recipe").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("give").then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.argument("recipe", ResourceKeyArgument.key(Registries.RECIPE)).executes($$0 -> RecipeCommand.giveRecipes((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Collections.singleton(ResourceKeyArgument.getRecipe((CommandContext<CommandSourceStack>)$$0, "recipe")))))).then(Commands.literal("*").executes($$0 -> RecipeCommand.giveRecipes((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), ((CommandSourceStack)$$0.getSource()).getServer().getRecipeManager().getRecipes())))))).then(Commands.literal("take").then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.argument("recipe", ResourceKeyArgument.key(Registries.RECIPE)).executes($$0 -> RecipeCommand.takeRecipes((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), Collections.singleton(ResourceKeyArgument.getRecipe((CommandContext<CommandSourceStack>)$$0, "recipe")))))).then(Commands.literal("*").executes($$0 -> RecipeCommand.takeRecipes((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), ((CommandSourceStack)$$0.getSource()).getServer().getRecipeManager().getRecipes()))))));
    }

    private static int giveRecipes(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Collection<RecipeHolder<?>> $$2) throws CommandSyntaxException {
        int $$3 = 0;
        for (ServerPlayer $$4 : $$1) {
            $$3 += $$4.awardRecipes($$2);
        }
        if ($$3 == 0) {
            throw ERROR_GIVE_FAILED.create();
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.recipe.give.success.single", $$2.size(), ((ServerPlayer)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.recipe.give.success.multiple", $$2.size(), $$1.size()), true);
        }
        return $$3;
    }

    private static int takeRecipes(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Collection<RecipeHolder<?>> $$2) throws CommandSyntaxException {
        int $$3 = 0;
        for (ServerPlayer $$4 : $$1) {
            $$3 += $$4.resetRecipes($$2);
        }
        if ($$3 == 0) {
            throw ERROR_TAKE_FAILED.create();
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.recipe.take.success.single", $$2.size(), ((ServerPlayer)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.recipe.take.success.multiple", $$2.size(), $$1.size()), true);
        }
        return $$3;
    }
}

