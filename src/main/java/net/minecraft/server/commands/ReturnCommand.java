package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.ContextChain;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.CustomCommandExecutor.CommandAdapter;
import net.minecraft.commands.execution.CustomModifierExecutor.ModifierAdapter;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.execution.tasks.BuildContexts.Continuation;

public class ReturnCommand {
   public static <T extends ExecutionCommandSource<T>> void register(CommandDispatcher<T> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("return")
                     .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                  .then(RequiredArgumentBuilder.argument("value", IntegerArgumentType.integer()).executes(new ReturnCommand.ReturnValueCustomExecutor())))
               .then(LiteralArgumentBuilder.literal("fail").executes(new ReturnCommand.ReturnFailCustomExecutor())))
            .then(LiteralArgumentBuilder.literal("run").forward($$0.getRoot(), new ReturnCommand.ReturnFromCommandCustomModifier(), false))
      );
   }

   static class ReturnFailCustomExecutor<T extends ExecutionCommandSource<T>> implements CommandAdapter<T> {
      public void run(T $$0, ContextChain<T> $$1, ChainModifiers $$2, ExecutionControl<T> $$3) {
         $$0.callback().onFailure();
         Frame $$4 = $$3.currentFrame();
         $$4.returnFailure();
         $$4.discard();
      }
   }

   static class ReturnFromCommandCustomModifier<T extends ExecutionCommandSource<T>> implements ModifierAdapter<T> {
      public void apply(T $$0, List<T> $$1, ContextChain<T> $$2, ChainModifiers $$3, ExecutionControl<T> $$4) {
         if ($$1.isEmpty()) {
            if ($$3.isReturn()) {
               $$4.queueNext(FallthroughTask.instance());
            }
         } else {
            $$4.currentFrame().discard();
            ContextChain<T> $$5 = $$2.nextStage();
            String $$6 = $$5.getTopContext().getInput();
            $$4.queueNext(new Continuation($$6, $$5, $$3.setReturn(), $$0, $$1));
         }
      }
   }

   static class ReturnValueCustomExecutor<T extends ExecutionCommandSource<T>> implements CommandAdapter<T> {
      public void run(T $$0, ContextChain<T> $$1, ChainModifiers $$2, ExecutionControl<T> $$3) {
         int $$4 = IntegerArgumentType.getInteger($$1.getTopContext(), "value");
         $$0.callback().onSuccess($$4);
         Frame $$5 = $$3.currentFrame();
         $$5.returnSuccess($$4);
         $$5.discard();
      }
   }
}
