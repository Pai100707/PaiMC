package net.minecraft.commands.execution.tasks;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.context.ContextChain.Stage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.network.chat.Component;

public class BuildContexts<T extends net.minecraft.commands.ExecutionCommandSource<T>> {
   @VisibleForTesting
   public static final DynamicCommandExceptionType ERROR_FORK_LIMIT_REACHED = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("command.forkLimit", new Object[]{$$0})
   );
   private final String commandInput;
   private final ContextChain<T> command;

   public BuildContexts(String $$0, ContextChain<T> $$1) {
      this.commandInput = $$0;
      this.command = $$1;
   }

   protected void execute(T $$0, List<T> $$1, ExecutionContext<T> $$2, Frame $$3, ChainModifiers $$4) {
      ContextChain<T> $$5 = this.command;
      ChainModifiers $$6 = $$4;
      List<T> $$7 = $$1;
      if ($$5.getStage() != Stage.EXECUTE) {
         $$2.profiler().push(() -> "prepare " + this.commandInput);

         try {
            for (int $$8 = $$2.forkLimit(); $$5.getStage() != Stage.EXECUTE; $$5 = $$5.nextStage()) {
               CommandContext<T> $$9 = $$5.getTopContext();
               if ($$9.isForked()) {
                  $$6 = $$6.setForked();
               }

               RedirectModifier<T> $$10 = $$9.getRedirectModifier();
               if ($$10 instanceof CustomModifierExecutor<T> $$11) {
                  $$11.apply($$0, $$7, $$5, $$6, ExecutionControl.create($$2, $$3));
                  return;
               }

               if ($$10 != null) {
                  $$2.incrementCost();
                  boolean $$12 = $$6.isForked();
                  List<T> $$13 = new ObjectArrayList();

                  for (T $$14 : $$7) {
                     try {
                        Collection<T> $$15 = ContextChain.runModifier($$9, $$14, ($$0x, $$1x, $$2x) -> {}, $$12);
                        if ($$13.size() + $$15.size() >= $$8) {
                           $$0.handleError(ERROR_FORK_LIMIT_REACHED.create($$8), $$12, $$2.tracer());
                           return;
                        }

                        $$13.addAll($$15);
                     } catch (CommandSyntaxException var20) {
                        $$14.handleError(var20, $$12, $$2.tracer());
                        if (!$$12) {
                           return;
                        }
                     }
                  }

                  $$7 = $$13;
               }
            }
         } finally {
            $$2.profiler().pop();
         }
      }

      if ($$7.isEmpty()) {
         if ($$6.isReturn()) {
            $$2.queueNext(new CommandQueueEntry<>($$3, FallthroughTask.instance()));
         }
      } else {
         CommandContext<T> $$17 = $$5.getTopContext();
         if ($$17.getCommand() instanceof CustomCommandExecutor<T> $$19) {
            ExecutionControl<T> $$20 = ExecutionControl.create($$2, $$3);

            for (T $$21 : $$7) {
               $$19.run($$21, $$5, $$6, $$20);
            }
         } else {
            if ($$6.isReturn()) {
               T $$22 = $$7.get(0);
               $$22 = $$22.withCallback(net.minecraft.commands.CommandResultCallback.chain($$22.callback(), $$3.returnValueConsumer()));
               $$7 = List.of($$22);
            }

            ExecuteCommand<T> $$23 = new ExecuteCommand<>(this.commandInput, $$6, $$17);
            ContinuationTask.schedule($$2, $$3, $$7, ($$1x, $$2x) -> new CommandQueueEntry<>($$1x, $$23.bind((T)$$2x)));
         }
      }
   }

   protected void traceCommandStart(ExecutionContext<T> $$0, Frame $$1) {
      TraceCallbacks $$2 = $$0.tracer();
      if ($$2 != null) {
         $$2.onCommand($$1.depth(), this.commandInput);
      }
   }

   @Override
   public String toString() {
      return this.commandInput;
   }

   public static class Continuation<T extends net.minecraft.commands.ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
      private final ChainModifiers modifiers;
      private final T originalSource;
      private final List<T> sources;

      public Continuation(String $$0, ContextChain<T> $$1, ChainModifiers $$2, T $$3, List<T> $$4) {
         super($$0, $$1);
         this.originalSource = $$3;
         this.sources = $$4;
         this.modifiers = $$2;
      }

      @Override
      public void execute(ExecutionContext<T> $$0, Frame $$1) {
         this.execute(this.originalSource, this.sources, $$0, $$1, this.modifiers);
      }
   }

   public static class TopLevel<T extends net.minecraft.commands.ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
      private final T source;

      public TopLevel(String $$0, ContextChain<T> $$1, T $$2) {
         super($$0, $$1);
         this.source = $$2;
      }

      @Override
      public void execute(ExecutionContext<T> $$0, Frame $$1) {
         this.traceCommandStart($$0, $$1);
         this.execute(this.source, List.of(this.source), $$0, $$1, ChainModifiers.DEFAULT);
      }
   }

   public static class Unbound<T extends net.minecraft.commands.ExecutionCommandSource<T>> extends BuildContexts<T> implements UnboundEntryAction<T> {
      public Unbound(String $$0, ContextChain<T> $$1) {
         super($$0, $$1);
      }

      public void execute(T $$0, ExecutionContext<T> $$1, Frame $$2) {
         this.traceCommandStart($$1, $$2);
         this.execute($$0, List.of($$0), $$1, $$2, ChainModifiers.DEFAULT);
      }
   }
}
