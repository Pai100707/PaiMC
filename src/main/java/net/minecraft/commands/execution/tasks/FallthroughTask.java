package net.minecraft.commands.execution.tasks;

import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

public class FallthroughTask<T extends net.minecraft.commands.ExecutionCommandSource<T>> implements EntryAction<T> {
   private static final FallthroughTask<? extends net.minecraft.commands.ExecutionCommandSource<?>> INSTANCE = (FallthroughTask<? extends net.minecraft.commands.ExecutionCommandSource<?>>)(new FallthroughTask<>());

   public static <T extends net.minecraft.commands.ExecutionCommandSource<T>> EntryAction<T> instance() {
      return (EntryAction<T>)INSTANCE;
   }

   @Override
   public void execute(ExecutionContext<T> $$0, Frame $$1) {
      $$1.returnFailure();
      $$1.discard();
   }
}
