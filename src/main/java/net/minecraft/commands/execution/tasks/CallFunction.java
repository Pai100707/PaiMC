package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.InstantiatedFunction;

public class CallFunction<T extends net.minecraft.commands.ExecutionCommandSource<T>> implements UnboundEntryAction<T> {
   private final InstantiatedFunction<T> function;
   private final net.minecraft.commands.CommandResultCallback resultCallback;
   private final boolean returnParentFrame;

   public CallFunction(InstantiatedFunction<T> $$0, net.minecraft.commands.CommandResultCallback $$1, boolean $$2) {
      this.function = $$0;
      this.resultCallback = $$1;
      this.returnParentFrame = $$2;
   }

   public void execute(T $$0, ExecutionContext<T> $$1, Frame $$2) {
      $$1.incrementCost();
      List<UnboundEntryAction<T>> $$3 = this.function.entries();
      TraceCallbacks $$4 = $$1.tracer();
      if ($$4 != null) {
         $$4.onCall($$2.depth(), this.function.id(), this.function.entries().size());
      }

      int $$5 = $$2.depth() + 1;
      Frame.FrameControl $$6 = this.returnParentFrame ? $$2.frameControl() : $$1.frameControlForDepth($$5);
      Frame $$7 = new Frame($$5, this.resultCallback, $$6);
      ContinuationTask.schedule($$1, $$7, $$3, ($$1x, $$2x) -> new CommandQueueEntry<>($$1x, $$2x.bind($$0)));
   }
}
