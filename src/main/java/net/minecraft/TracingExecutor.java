package net.minecraft;

import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public record TracingExecutor(ExecutorService service) implements Executor {
   public Executor forName(String $$0) {
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         return $$1 -> this.service.execute(() -> {
            Thread $$2 = Thread.currentThread();
            String $$3 = $$2.getName();
            $$2.setName($$0);

            try {
               Zone $$4 = TracyClient.beginZone($$0, SharedConstants.IS_RUNNING_IN_IDE);

               try {
                  $$1.run();
               } catch (Throwable var12) {
                  if ($$4 != null) {
                     try {
                        $$4.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                     }
                  }

                  throw var12;
               }

               if ($$4 != null) {
                  $$4.close();
               }
            } finally {
               $$2.setName($$3);
            }
         });
      } else {
         return (Executor)(TracyClient.isAvailable() ? $$1 -> this.service.execute(() -> {
            Zone $$2 = TracyClient.beginZone($$0, SharedConstants.IS_RUNNING_IN_IDE);

            try {
               $$1.run();
            } catch (Throwable var6) {
               if ($$2 != null) {
                  try {
                     $$2.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if ($$2 != null) {
               $$2.close();
            }
         }) : this.service);
      }
   }

   @Override
   public void execute(Runnable $$0) {
      this.service.execute(wrapUnnamed($$0));
   }

   public void shutdownAndAwait(long $$0, TimeUnit $$1) {
      this.service.shutdown();

      boolean $$2;
      try {
         $$2 = this.service.awaitTermination($$0, $$1);
      } catch (InterruptedException var6) {
         $$2 = false;
      }

      if (!$$2) {
         this.service.shutdownNow();
      }
   }

   private static Runnable wrapUnnamed(Runnable $$0) {
      return !TracyClient.isAvailable() ? $$0 : () -> {
         Zone $$1 = TracyClient.beginZone("task", SharedConstants.IS_RUNNING_IN_IDE);

         try {
            $$0.run();
         } catch (Throwable var5) {
            if ($$1 != null) {
               try {
                  $$1.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if ($$1 != null) {
            $$1.close();
         }
      };
   }
}
