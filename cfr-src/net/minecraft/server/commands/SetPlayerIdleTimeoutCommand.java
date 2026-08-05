/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetPlayerIdleTimeoutCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setidletimeout").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(Commands.argument("minutes", IntegerArgumentType.integer((int)0)).executes($$0 -> SetPlayerIdleTimeoutCommand.setIdleTimeout((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"minutes")))));
    }

    private static int setIdleTimeout(CommandSourceStack $$0, int $$1) {
        $$0.getServer().setPlayerIdleTimeout($$1);
        if ($$1 > 0) {
            $$0.sendSuccess(() -> Component.translatable("commands.setidletimeout.success", $$1), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.setidletimeout.success.disabled"), true);
        }
        return $$1;
    }
}

