/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

public class StopSoundCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        RequiredArgumentBuilder $$12 = (RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes($$0 -> StopSoundCommand.stopSound((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), null, null))).then(Commands.literal("*").then(Commands.argument("sound", IdentifierArgument.id()).suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS)).executes($$0 -> StopSoundCommand.stopSound((CommandSourceStack)$$0.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "targets"), null, IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "sound")))));
        for (SoundSource $$2 : SoundSource.values()) {
            $$12.then(((LiteralArgumentBuilder)Commands.literal($$2.getName()).executes($$1 -> StopSoundCommand.stopSound((CommandSourceStack)$$1.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$1, "targets"), $$2, null))).then(Commands.argument("sound", IdentifierArgument.id()).suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS)).executes($$1 -> StopSoundCommand.stopSound((CommandSourceStack)$$1.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$1, "targets"), $$2, IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$1, "sound")))));
        }
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("stopsound").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then((ArgumentBuilder)$$12));
    }

    private static int stopSound(CommandSourceStack $$0, Collection<ServerPlayer> $$1, @Nullable SoundSource $$2, @Nullable Identifier $$3) {
        ClientboundStopSoundPacket $$4 = new ClientboundStopSoundPacket($$3, $$2);
        for (ServerPlayer $$5 : $$1) {
            $$5.connection.send($$4);
        }
        if ($$2 != null) {
            if ($$3 != null) {
                $$0.sendSuccess(() -> Component.translatable("commands.stopsound.success.source.sound", Component.translationArg($$3), $$2.getName()), true);
            } else {
                $$0.sendSuccess(() -> Component.translatable("commands.stopsound.success.source.any", $$2.getName()), true);
            }
        } else if ($$3 != null) {
            $$0.sendSuccess(() -> Component.translatable("commands.stopsound.success.sourceless.sound", Component.translationArg($$3)), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.stopsound.success.sourceless.any"), true);
        }
        return $$1.size();
    }
}

