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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;

public class SeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02, boolean $$1) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("seed").requires(Commands.hasPermission($$1 ? Commands.LEVEL_GAMEMASTERS : Commands.LEVEL_ALL))).executes($$0 -> {
            long $$1 = ((CommandSourceStack)$$0.getSource()).getLevel().getSeed();
            MutableComponent $$2 = ComponentUtils.copyOnClickText(String.valueOf($$1));
            ((CommandSourceStack)$$0.getSource()).sendSuccess(() -> Component.translatable("commands.seed.success", $$2), false);
            return (int)$$1;
        }));
    }
}

