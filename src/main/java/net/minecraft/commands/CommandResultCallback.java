package net.minecraft.commands;

@FunctionalInterface
public interface CommandResultCallback {
   net.minecraft.commands.CommandResultCallback EMPTY = new net.minecraft.commands.CommandResultCallback() {
      @Override
      public void onResult(boolean $$0, int $$1) {
      }

      @Override
      public String toString() {
         return "<empty>";
      }
   };

   void onResult(boolean var1, int var2);

   default void onSuccess(int $$0) {
      this.onResult(true, $$0);
   }

   default void onFailure() {
      this.onResult(false, 0);
   }

   static net.minecraft.commands.CommandResultCallback chain(net.minecraft.commands.CommandResultCallback $$0, net.minecraft.commands.CommandResultCallback $$1) {
      if ($$0 == EMPTY) {
         return $$1;
      } else {
         return $$1 == EMPTY ? $$0 : ($$2, $$3) -> {
            $$0.onResult($$2, $$3);
            $$1.onResult($$2, $$3);
         };
      }
   }
}
