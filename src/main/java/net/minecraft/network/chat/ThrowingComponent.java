package net.minecraft.network.chat;

public class ThrowingComponent extends Exception {
   private final Component component;

   public ThrowingComponent(Component $$0) {
      super($$0.getString());
      this.component = $$0;
   }

   public ThrowingComponent(Component $$0, Throwable $$1) {
      super($$0.getString(), $$1);
      this.component = $$0;
   }

   public Component getComponent() {
      return this.component;
   }
}
