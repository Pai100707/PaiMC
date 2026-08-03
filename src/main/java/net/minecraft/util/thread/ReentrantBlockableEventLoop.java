package net.minecraft.util.thread;

public abstract class ReentrantBlockableEventLoop<R extends Runnable> extends BlockableEventLoop<R> {
   private int reentrantCount;

   public ReentrantBlockableEventLoop(String $$0) {
      super($$0);
   }

   @Override
   public boolean scheduleExecutables() {
      return this.runningTask() || super.scheduleExecutables();
   }

   protected boolean runningTask() {
      return this.reentrantCount != 0;
   }

   @Override
   public void doRunTask(R $$0) {
      this.reentrantCount++;

      try {
         super.doRunTask($$0);
      } finally {
         this.reentrantCount--;
      }
   }
}
