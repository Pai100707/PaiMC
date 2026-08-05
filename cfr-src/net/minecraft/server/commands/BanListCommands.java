/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 */
package net.minecraft.server.commands;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.BanListEntry;
import net.minecraft.server.players.PlayerList;

public class BanListCommands {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("banlist").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).executes($$0 -> {
            PlayerList $$1 = ((CommandSourceStack)$$0.getSource()).getServer().getPlayerList();
            return BanListCommands.showList((CommandSourceStack)$$0.getSource(), Lists.newArrayList((Iterable)Iterables.concat($$1.getBans().getEntries(), $$1.getIpBans().getEntries())));
        })).then(Commands.literal("ips").executes($$0 -> BanListCommands.showList((CommandSourceStack)$$0.getSource(), ((CommandSourceStack)$$0.getSource()).getServer().getPlayerList().getIpBans().getEntries())))).then(Commands.literal("players").executes($$0 -> BanListCommands.showList((CommandSourceStack)$$0.getSource(), ((CommandSourceStack)$$0.getSource()).getServer().getPlayerList().getBans().getEntries()))));
    }

    private static int showList(CommandSourceStack $$0, Collection<? extends BanListEntry<?>> $$1) {
        if ($$1.isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.banlist.none"), false);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.banlist.list", $$1.size()), false);
            for (BanListEntry<?> $$2 : $$1) {
                $$0.sendSuccess(() -> Component.translatable("commands.banlist.entry", $$2.getDisplayName(), $$2.getSource(), $$2.getReasonMessage()), false);
            }
        }
        return $$1.size();
    }
}

