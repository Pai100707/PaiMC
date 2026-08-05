/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

public class PublishCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.publish.failed"));
    private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.publish.alreadyPublished", $$0));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("publish").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))).executes($$0 -> PublishCommand.publish((CommandSourceStack)$$0.getSource(), HttpUtil.getAvailablePort(), false, null))).then(((RequiredArgumentBuilder)Commands.argument("allowCommands", BoolArgumentType.bool()).executes($$0 -> PublishCommand.publish((CommandSourceStack)$$0.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool((CommandContext)$$0, (String)"allowCommands"), null))).then(((RequiredArgumentBuilder)Commands.argument("gamemode", GameModeArgument.gameMode()).executes($$0 -> PublishCommand.publish((CommandSourceStack)$$0.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool((CommandContext)$$0, (String)"allowCommands"), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)$$0, "gamemode")))).then(Commands.argument("port", IntegerArgumentType.integer((int)0, (int)65535)).executes($$0 -> PublishCommand.publish((CommandSourceStack)$$0.getSource(), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"port"), BoolArgumentType.getBool((CommandContext)$$0, (String)"allowCommands"), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)$$0, "gamemode")))))));
    }

    private static int publish(CommandSourceStack $$0, int $$1, boolean $$2, @Nullable GameType $$3) throws CommandSyntaxException {
        if ($$0.getServer().isPublished()) {
            throw ERROR_ALREADY_PUBLISHED.create((Object)$$0.getServer().getPort());
        }
        if (!$$0.getServer().publishServer($$3, $$2, $$1)) {
            throw ERROR_FAILED.create();
        }
        $$0.sendSuccess(() -> PublishCommand.getSuccessMessage($$1), true);
        return $$1;
    }

    public static MutableComponent getSuccessMessage(int $$0) {
        MutableComponent $$1 = ComponentUtils.copyOnClickText(String.valueOf($$0));
        return Component.translatable("commands.publish.started", $$1);
    }
}

