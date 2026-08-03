package net.minecraft.server;

public class TickTask implements Runnable {
   private final int tick;
   private final Runnable runnable;

   public TickTask(int $$0, Runnable $$1) {
      this.tick = $$0;
      this.runnable = $$1;
   }

   public int getTick() {
      return this.tick;
   }

   @Override
   public void run() {
      this.runnable.run();
   }
}
