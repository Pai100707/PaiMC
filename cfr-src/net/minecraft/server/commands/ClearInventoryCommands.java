/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ClearInventoryCommands {
    private static final DynamicCommandExceptionType ERROR_SINGLE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("clear.failed.single", $$0));
    private static final DynamicCommandExceptionType ERROR_MULTIPLE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("clear.failed.multiple", $$0));

    public static void register(CommandDispatcher<CommandSourceStack> $$03, CommandBuildContext $$1) {
        $$03.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clear").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes($$02 -> ClearInventoryCommands.clearUnlimited((CommandSourceStack)$$02.getSource(), Collections.singleton(((CommandSourceStack)$$02.getSource()).getPlayerOrException()), $$0 -> true))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes($$02 -> ClearInventoryCommands.clearUnlimited((CommandSourceStack)$$02.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$02, "targets"), $$0 -> true))).then(((RequiredArgumentBuilder)Commands.argument("item", ItemPredicateArgument.itemPredicate($$1)).executes($$0 -> ClearInventoryCommands.clearUnlimited((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)$$0, "item")))).then(Commands.argument("maxCount", IntegerArgumentType.integer((int)0)).executes($$0 -> ClearInventoryCommands.clearInventory((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)$$0, "item"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"maxCount")))))));
    }

    private static int clearUnlimited(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Predicate<ItemStack> $$2) throws CommandSyntaxException {
        return ClearInventoryCommands.clearInventory($$0, $$1, $$2, -1);
    }

    private static int clearInventory(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Predicate<ItemStack> $$2, int $$3) throws CommandSyntaxException {
        int $$4 = 0;
        for (ServerPlayer $$5 : $$1) {
            $$4 += $$5.getInventory().clearOrCountMatchingItems($$2, $$3, $$5.inventoryMenu.getCraftSlots());
            $$5.containerMenu.broadcastChanges();
            $$5.inventoryMenu.slotsChanged($$5.getInventory());
        }
        if ($$4 == 0) {
            if ($$1.size() == 1) {
                throw ERROR_SINGLE.create((Object)$$1.iterator().next().getName());
            }
            throw ERROR_MULTIPLE.create((Object)$$1.size());
        }
        int $$6 = $$4;
        if ($$3 == 0) {
            if ($$1.size() == 1) {
                $$0.sendSuccess(() -> Component.translatable("commands.clear.test.single", $$6, ((ServerPlayer)$$1.iterator().next()).getDisplayName()), true);
            } else {
                $$0.sendSuccess(() -> Component.translatable("commands.clear.test.multiple", $$6, $$1.size()), true);
            }
        } else if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.clear.success.single", $$6, ((ServerPlayer)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.clear.success.multiple", $$6, $$1.size()), true);
        }
        return $$4;
    }
}

