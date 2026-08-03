package net.minecraft.commands.execution.tasks;

import java.util.function.Consumer;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;

public class IsolatedCall<T extends net.minecraft.commands.ExecutionCommandSource<T>> implements EntryAction<T> {
   private final Consumer<ExecutionControl<T>> taskProducer;
   private final net.minecraft.commands.CommandResultCallback output;

   public IsolatedCall(Consumer<ExecutionControl<T>> $$0, net.minecraft.commands.CommandResultCallback $$1) {
      this.taskProducer = $$0;
      this.output = $$1;
   }

   @Override
   public void execute(ExecutionContext<T> $$0, Frame $$1) {
      int $$2 = $$1.depth() + 1;
      Frame $$3 = new Frame($$2, this.output, $$0.frameControlForDepth($$2));
      this.taskProducer.accept(ExecutionControl.create($$0, $$3));
   }
}
