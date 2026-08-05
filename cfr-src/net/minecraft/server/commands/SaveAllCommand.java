/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class SaveAllCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.save.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("save-all").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))).executes($$0 -> SaveAllCommand.saveAll((CommandSourceStack)$$0.getSource(), false))).then(Commands.literal("flush").executes($$0 -> SaveAllCommand.saveAll((CommandSourceStack)$$0.getSource(), true))));
    }

    private static int saveAll(CommandSourceStack $$0, boolean $$1) throws CommandSyntaxException {
        $$0.sendSuccess(() -> Component.translatable("commands.save.saving"), false);
        MinecraftServer $$2 = $$0.getServer();
        boolean $$3 = $$2.saveEverything(true, $$1, true);
        if (!$$3) {
            throw ERROR_FAILED.create();
        }
        $$0.sendSuccess(() -> Component.translatable("commands.save.success"), true);
        return 1;
    }
}

