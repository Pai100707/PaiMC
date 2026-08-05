/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.tree.CommandNode
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.tree.CommandNode;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.FallthroughTask;

public class ReturnCommand {
    public static <T extends ExecutionCommandSource<T>> void register(CommandDispatcher<T> $$0) {
        $$0.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal((String)"return").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(RequiredArgumentBuilder.argument((String)"value", (ArgumentType)IntegerArgumentType.integer()).executes(new ReturnValueCustomExecutor()))).then(LiteralArgumentBuilder.literal((String)"fail").executes(new ReturnFailCustomExecutor()))).then(LiteralArgumentBuilder.literal((String)"run").forward((CommandNode)$$0.getRoot(), new ReturnFromCommandCustomModifier(), false)));
    }

    static class ReturnValueCustomExecutor<T extends ExecutionCommandSource<T>>
    implements CustomCommandExecutor.CommandAdapter<T> {
        ReturnValueCustomExecutor() {
        }

        @Override
        public void run(T $$0, ContextChain<T> $$1, ChainModifiers $$2, ExecutionControl<T> $$3) {
            int $$4 = IntegerArgumentType.getInteger((CommandContext)$$1.getTopContext(), (String)"value");
            $$0.callback().onSuccess($$4);
            Frame $$5 = $$3.currentFrame();
            $$5.returnSuccess($$4);
            $$5.discard();
        }
    }

    static class ReturnFailCustomExecutor<T extends ExecutionCommandSource<T>>
    implements CustomCommandExecutor.CommandAdapter<T> {
        ReturnFailCustomExecutor() {
        }

        @Override
        public void run(T $$0, ContextChain<T> $$1, ChainModifiers $$2, ExecutionControl<T> $$3) {
            $$0.callback().onFailure();
            Frame $$4 = $$3.currentFrame();
            $$4.returnFailure();
            $$4.discard();
        }
    }

    static class ReturnFromCommandCustomModifier<T extends ExecutionCommandSource<T>>
    implements CustomModifierExecutor.ModifierAdapter<T> {
        ReturnFromCommandCustomModifier() {
        }

        @Override
        public void apply(T $$0, List<T> $$1, ContextChain<T> $$2, ChainModifiers $$3, ExecutionControl<T> $$4) {
            if ($$1.isEmpty()) {
                if ($$3.isReturn()) {
                    $$4.queueNext(FallthroughTask.instance());
                }
                return;
            }
            $$4.currentFrame().discard();
            ContextChain $$5 = $$2.nextStage();
            String $$6 = $$5.getTopContext().getInput();
            $$4.queueNext(new BuildContexts.Continuation<T>($$6, $$5, $$3.setReturn(), $$0, $$1));
        }
    }
}

