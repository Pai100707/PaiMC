package net.minecraft.util.thread;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class ConsecutiveExecutor extends AbstractConsecutiveExecutor<Runnable> {
   public ConsecutiveExecutor(Executor $$0, String $$1) {
      super(new StrictQueue.QueueStrictQueue(new ConcurrentLinkedQueue<>()), $$0, $$1);
   }

   @Override
   public Runnable wrapRunnable(Runnable $$0) {
      return $$0;
   }
}
