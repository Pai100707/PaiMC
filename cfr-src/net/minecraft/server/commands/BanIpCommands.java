/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.net.InetAddresses
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import org.jspecify.annotations.Nullable;

public class BanIpCommands {
    private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType((Message)Component.translatable("commands.banip.invalid"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType((Message)Component.translatable("commands.banip.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("ban-ip").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(((RequiredArgumentBuilder)Commands.argument("target", StringArgumentType.word()).executes($$0 -> BanIpCommands.banIpOrName((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"target"), null))).then(Commands.argument("reason", MessageArgument.message()).executes($$0 -> BanIpCommands.banIpOrName((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"target"), MessageArgument.getMessage((CommandContext<CommandSourceStack>)$$0, "reason"))))));
    }

    private static int banIpOrName(CommandSourceStack $$0, String $$1, @Nullable Component $$2) throws CommandSyntaxException {
        if (InetAddresses.isInetAddress((String)$$1)) {
            return BanIpCommands.banIp($$0, $$1, $$2);
        }
        ServerPlayer $$3 = $$0.getServer().getPlayerList().getPlayerByName($$1);
        if ($$3 != null) {
            return BanIpCommands.banIp($$0, $$3.getIpAddress(), $$2);
        }
        throw ERROR_INVALID_IP.create();
    }

    private static int banIp(CommandSourceStack $$0, String $$1, @Nullable Component $$2) throws CommandSyntaxException {
        IpBanList $$3 = $$0.getServer().getPlayerList().getIpBans();
        if ($$3.isBanned($$1)) {
            throw ERROR_ALREADY_BANNED.create();
        }
        List<ServerPlayer> $$4 = $$0.getServer().getPlayerList().getPlayersWithAddress($$1);
        IpBanListEntry $$5 = new IpBanListEntry($$1, null, $$0.getTextName(), null, $$2 == null ? null : $$2.getString());
        $$3.add($$5);
        $$0.sendSuccess(() -> Component.translatable("commands.banip.success", $$1, $$5.getReasonMessage()), true);
        if (!$$4.isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.banip.info", $$4.size(), EntitySelector.joinNames($$4)), true);
        }
        for (ServerPlayer $$6 : $$4) {
            $$6.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned"));
        }
        return $$4.size();
    }
}

