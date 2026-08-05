/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;

public class ListPlayersCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list").executes($$0 -> ListPlayersCommand.listPlayers((CommandSourceStack)$$0.getSource()))).then(Commands.literal("uuids").executes($$0 -> ListPlayersCommand.listPlayersWithUuids((CommandSourceStack)$$0.getSource()))));
    }

    private static int listPlayers(CommandSourceStack $$0) {
        return ListPlayersCommand.format($$0, Player::getDisplayName);
    }

    private static int listPlayersWithUuids(CommandSourceStack $$02) {
        return ListPlayersCommand.format($$02, $$0 -> Component.translatable("commands.list.nameAndId", $$0.getName(), Component.translationArg($$0.getGameProfile().id())));
    }

    private static int format(CommandSourceStack $$0, Function<ServerPlayer, Component> $$1) {
        PlayerList $$2 = $$0.getServer().getPlayerList();
        List<ServerPlayer> $$3 = $$2.getPlayers();
        Component $$4 = ComponentUtils.formatList($$3, $$1);
        $$0.sendSuccess(() -> Component.translatable("commands.list.players", $$3.size(), $$2.getMaxPlayers(), $$4), false);
        return $$3.size();
    }
}

