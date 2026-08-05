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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;

public class GameRuleCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
        final LiteralArgumentBuilder $$2 = (LiteralArgumentBuilder)Commands.literal("gamerule").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        new GameRules($$1.enabledFeatures()).visitGameRuleTypes(new GameRuleTypeVisitor(){

            @Override
            public <T> void visit(GameRule<T> $$0) {
                LiteralArgumentBuilder<CommandSourceStack> $$1 = Commands.literal($$0.id());
                LiteralArgumentBuilder<CommandSourceStack> $$22 = Commands.literal($$0.getIdentifier().toString());
                ((LiteralArgumentBuilder)$$2.then(GameRuleCommand.buildRuleArguments($$0, $$1))).then(GameRuleCommand.buildRuleArguments($$0, $$22));
            }
        });
        $$0.register($$2);
    }

    static <T> LiteralArgumentBuilder<CommandSourceStack> buildRuleArguments(GameRule<T> $$0, LiteralArgumentBuilder<CommandSourceStack> $$12) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)$$12.executes($$1 -> GameRuleCommand.queryRule((CommandSourceStack)$$1.getSource(), $$0))).then(Commands.argument("value", $$0.argument()).executes($$1 -> GameRuleCommand.setRule((CommandContext<CommandSourceStack>)$$1, $$0)));
    }

    private static <T> int setRule(CommandContext<CommandSourceStack> $$0, GameRule<T> $$1) {
        CommandSourceStack $$2 = (CommandSourceStack)$$0.getSource();
        Object $$3 = $$0.getArgument("value", $$1.valueClass());
        $$2.getLevel().getGameRules().set($$1, $$3, ((CommandSourceStack)$$0.getSource()).getServer());
        $$2.sendSuccess(() -> Component.translatable("commands.gamerule.set", $$1.id(), $$1.serialize($$3)), true);
        return $$1.getCommandResult($$3);
    }

    private static <T> int queryRule(CommandSourceStack $$0, GameRule<T> $$1) {
        Object $$2 = $$0.getLevel().getGameRules().get($$1);
        $$0.sendSuccess(() -> Component.translatable("commands.gamerule.query", $$1.id(), $$1.serialize($$2)), false);
        return $$1.getCommandResult($$2);
    }
}

