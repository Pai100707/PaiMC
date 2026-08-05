/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerPlayer;

public class DialogCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$1) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("dialog").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("show").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("dialog", ResourceOrIdArgument.dialog($$1)).executes($$0 -> DialogCommand.showDialog((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), ResourceOrIdArgument.getDialog((CommandContext<CommandSourceStack>)$$0, "dialog"))))))).then(Commands.literal("clear").then(Commands.argument("targets", EntityArgument.players()).executes($$0 -> DialogCommand.clearDialog((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"))))));
    }

    private static int showDialog(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Holder<Dialog> $$2) {
        for (ServerPlayer $$3 : $$1) {
            $$3.openDialog($$2);
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.dialog.show.single", ((ServerPlayer)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.dialog.show.multiple", $$1.size()), true);
        }
        return $$1.size();
    }

    private static int clearDialog(CommandSourceStack $$0, Collection<ServerPlayer> $$1) {
        for (ServerPlayer $$2 : $$1) {
            $$2.connection.send(ClientboundClearDialogPacket.INSTANCE);
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.dialog.clear.single", ((ServerPlayer)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.dialog.clear.multiple", $$1.size()), true);
        }
        return $$1.size();
    }
}

