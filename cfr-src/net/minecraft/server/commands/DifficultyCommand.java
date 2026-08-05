/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;

public class DifficultyCommand {
    private static final DynamicCommandExceptionType ERROR_ALREADY_DIFFICULT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.difficulty.failure", $$0));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        LiteralArgumentBuilder<CommandSourceStack> $$12 = Commands.literal("difficulty");
        for (Difficulty $$2 : Difficulty.values()) {
            $$12.then(Commands.literal($$2.getKey()).executes($$1 -> DifficultyCommand.setDifficulty((CommandSourceStack)$$1.getSource(), $$2)));
        }
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)$$12.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes($$0 -> {
            Difficulty $$1 = ((CommandSourceStack)$$0.getSource()).getLevel().getDifficulty();
            ((CommandSourceStack)$$0.getSource()).sendSuccess(() -> Component.translatable("commands.difficulty.query", $$1.getDisplayName()), false);
            return $$1.getId();
        }));
    }

    public static int setDifficulty(CommandSourceStack $$0, Difficulty $$1) throws CommandSyntaxException {
        MinecraftServer $$2 = $$0.getServer();
        if ($$2.getWorldData().getDifficulty() == $$1) {
            throw ERROR_ALREADY_DIFFICULT.create((Object)$$1.getKey());
        }
        $$2.setDifficulty($$1, true);
        $$0.sendSuccess(() -> Component.translatable("commands.difficulty.success", $$1.getDisplayName()), true);
        return 0;
    }
}

