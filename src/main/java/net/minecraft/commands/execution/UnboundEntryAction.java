package net.minecraft.commands.execution;

@FunctionalInterface
public interface UnboundEntryAction<T> {
   void execute(T var1, ExecutionContext<T> var2, Frame var3);

   default EntryAction<T> bind(T $$0) {
      return ($$1, $$2) -> this.execute($$0, $$1, $$2);
   }
}
