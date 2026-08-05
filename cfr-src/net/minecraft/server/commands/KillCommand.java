/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class KillCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("kill").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes($$0 -> KillCommand.kill((CommandSourceStack)$$0.getSource(), (Collection<? extends Entity>)ImmutableList.of((Object)((CommandSourceStack)$$0.getSource()).getEntityOrException())))).then(Commands.argument("targets", EntityArgument.entities()).executes($$0 -> KillCommand.kill((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets")))));
    }

    private static int kill(CommandSourceStack $$0, Collection<? extends Entity> $$1) {
        for (Entity entity : $$1) {
            entity.kill($$0.getLevel());
        }
        if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.kill.success.single", ((Entity)$$1.iterator().next()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.kill.success.multiple", $$1.size()), true);
        }
        return $$1.size();
    }
}

