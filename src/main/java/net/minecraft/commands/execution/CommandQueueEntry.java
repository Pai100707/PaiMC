package net.minecraft.commands.execution;

public record CommandQueueEntry<T>(Frame frame, EntryAction<T> action) {
   public void execute(ExecutionContext<T> $$0) {
      this.action.execute($$0, this.frame);
   }
}
