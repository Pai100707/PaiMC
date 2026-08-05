/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jspecify.annotations.Nullable;

public class DebugConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$12) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("debugconfig").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(Commands.literal("config").then(Commands.argument("target", EntityArgument.player()).executes($$0 -> DebugConfigCommand.config((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayer((CommandContext<CommandSourceStack>)$$0, "target")))))).then(Commands.literal("unconfig").then(Commands.argument("target", UuidArgument.uuid()).suggests(($$0, $$1) -> SharedSuggestionProvider.suggest(DebugConfigCommand.getUuidsInConfig(((CommandSourceStack)$$0.getSource()).getServer()), $$1)).executes($$0 -> DebugConfigCommand.unconfig((CommandSourceStack)$$0.getSource(), UuidArgument.getUuid((CommandContext<CommandSourceStack>)$$0, "target")))))).then(Commands.literal("dialog").then(Commands.argument("target", UuidArgument.uuid()).suggests(($$0, $$1) -> SharedSuggestionProvider.suggest(DebugConfigCommand.getUuidsInConfig(((CommandSourceStack)$$0.getSource()).getServer()), $$1)).then(Commands.argument("dialog", ResourceOrIdArgument.dialog($$12)).executes($$0 -> DebugConfigCommand.showDialog((CommandSourceStack)$$0.getSource(), UuidArgument.getUuid((CommandContext<CommandSourceStack>)$$0, "target"), ResourceOrIdArgument.getDialog((CommandContext<CommandSourceStack>)$$0, "dialog")))))));
    }

    private static Iterable<String> getUuidsInConfig(MinecraftServer $$0) {
        HashSet<String> $$1 = new HashSet<String>();
        for (Connection $$2 : $$0.getConnection().getConnections()) {
            PacketListener packetListener = $$2.getPacketListener();
            if (!(packetListener instanceof ServerConfigurationPacketListenerImpl)) continue;
            ServerConfigurationPacketListenerImpl $$3 = (ServerConfigurationPacketListenerImpl)packetListener;
            $$1.add($$3.getOwner().id().toString());
        }
        return $$1;
    }

    private static int config(CommandSourceStack $$0, ServerPlayer $$1) {
        GameProfile $$2 = $$1.getGameProfile();
        $$1.connection.switchToConfig();
        $$0.sendSuccess(() -> Component.literal("Switched player " + $$2.name() + "(" + String.valueOf($$2.id()) + ") to config mode"), false);
        return 1;
    }

    private static @Nullable ServerConfigurationPacketListenerImpl findConfigPlayer(MinecraftServer $$0, UUID $$1) {
        for (Connection $$2 : $$0.getConnection().getConnections()) {
            ServerConfigurationPacketListenerImpl $$3;
            PacketListener packetListener = $$2.getPacketListener();
            if (!(packetListener instanceof ServerConfigurationPacketListenerImpl) || !($$3 = (ServerConfigurationPacketListenerImpl)packetListener).getOwner().id().equals($$1)) continue;
            return $$3;
        }
        return null;
    }

    private static int unconfig(CommandSourceStack $$0, UUID $$1) {
        ServerConfigurationPacketListenerImpl $$2 = DebugConfigCommand.findConfigPlayer($$0.getServer(), $$1);
        if ($$2 != null) {
            $$2.returnToWorld();
            return 1;
        }
        $$0.sendFailure(Component.literal("Can't find player to unconfig"));
        return 0;
    }

    private static int showDialog(CommandSourceStack $$0, UUID $$1, Holder<Dialog> $$2) {
        ServerConfigurationPacketListenerImpl $$3 = DebugConfigCommand.findConfigPlayer($$0.getServer(), $$1);
        if ($$3 != null) {
            $$3.send(new ClientboundShowDialogPacket($$2));
            return 1;
        }
        $$0.sendFailure(Component.literal("Can't find player to talk to"));
        return 0;
    }
}

